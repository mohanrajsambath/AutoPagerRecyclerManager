package tower.sphia.auto_pager_recycler.lib;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * This class manages the RecyclerView and its Adapter to make them cooperate for the auto-pager feature,
 * setAdapter() must be called.
 * <p/>
 * Using composition and delegation to support any implementation or wrapper of a {@link RecyclerView}
 */
public class AutoPagerManager<P extends Page<E>, E> implements AutoPagerAdapter.AdapterCallbacks {
    /**
     * Next page loading is started when the number of remaining invisible items equals AUTO_PAGER_ZONE_SIZE
     */
    public static final int AUTO_PAGER_ZONE_SIZE = 3;
    private static final String TAG = "AutoPagerManager";
    static boolean DEBUG = false;
    /**
     * A flag whether next page loading has been started
     */
    private boolean mPagerTriggered;
    /**
     * The adapter for recyclerView
     */
    private AutoPagerAdapter<P, E> mAdapter;
    /**
     * The OnScrollListener which interpret scrolling events into paging event
     */
    private RecyclerView.OnScrollListener mOnScrollListener;
    /**
     * The LayoutManager for RecyclerView, used to get display information
     */
    private LinearLayoutManager mLayoutManager;
    /**
     * A handler to post {@code mAdapter.notifyDataSetChanged()} into message queue
     */
    private Handler mHandler;
    /**
     * The displaying RecyclerView
     */
    private RecyclerView mRecyclerView;
    /**
     * The index of the last page
     */
    private int mLastPageIndex = -1;
    /**
     * The current page index
     */
    private int mIndex;
    private List<OnDataAttachedListener> mOnDataAttachedListeners = new ArrayList<>();
    private LoadPageMethod mLoadPageMethod;
    private EndViewManager mEndViewManager;
    private int mFirstPageIndex;

    /**
     * The constructor.
     *
     * @param recyclerView   the RecyclerView to be wrapped
     * @param loadPageMethod the function interface which provide a function to load a certain page sync/async
     */
    public AutoPagerManager(RecyclerView recyclerView, LoadPageMethod loadPageMethod) {
        mRecyclerView = recyclerView;
        mPagerTriggered = false;
        mHandler = new Handler(Looper.getMainLooper());
        mLayoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        mLoadPageMethod = loadPageMethod;
    }

    public AutoPagerAdapter<P, E> getAdapter() {
        return mAdapter;
    }

    /**
     * @param adapter the implementation of {@link AutoPagerAdapter}
     */
    public void setAdapter(AutoPagerAdapter<P, E> adapter) {
        mAdapter = adapter;
        mAdapter.setCallbacks(this);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickFooter(View view) {

    }

    @Override
    public void onClickLoadMore(View view) {
        int childLayoutPosition = getRecyclerView().getChildLayoutPosition(view);
        int page = mAdapter.getLoadMorePositionPageMap().get(childLayoutPosition);
        loadPage(page);
    }

    @Override
    public void onClickEnding(View view) {
//        getRecyclerView().scrollToPosition(0);
        Log.d(TAG, "onClickEnding() called " + "reloading the last page");
        if (mEndViewManager == null) {
            View refresh = view.findViewById(R.id.ll_end);
            mEndViewManager = new EndViewManager(refresh);
        }
        mEndViewManager.startAnimator();
        loadPage(mLastPageIndex);
    }

    public void addOnDataAttachedListener(OnDataAttachedListener onDataAttachedListener) {
        mOnDataAttachedListeners.add(onDataAttachedListener);
    }

    public E findItemByView(View view) {
        int childLayoutPosition = getRecyclerView().getChildLayoutPosition(view);
        return getAdapter().getItemWithOffset(childLayoutPosition);
    }

    public void setLoadPageMethod(LoadPageMethod loadPageMethod) {
        mLoadPageMethod = loadPageMethod;
    }

    /**
     * Delegate the {@link AutoPagerAdapter#setItems(TreeMap)} method to
     * retrieve data info and/or do some initialization work
     *
     * @param pages the data container
     */
    public void setData(@NonNull TreeMap<Integer, P> pages) {
        if (DEBUG) Log.d(TAG, "setData() called with " + "pages.size() = [" + pages.size() + "]");
        int size = pages.size();
        // update mIndex
        if (size != 0) {
            P last = pages.lastEntry().getValue();
            mIndex = last.index();
            // check if first page have been loaded, if not, do some initialization work
            if (mLastPageIndex == -1) {
                mFirstPageIndex = last.first();
                mLastPageIndex = last.last();
                for (OnDataAttachedListener onDataAttachedListener : mOnDataAttachedListeners) {
                    onDataAttachedListener.onDataAttached();
                }
            }
        }
        mAdapter.setInLastPage(inLastPage());
        mAdapter.setItems(pages);
        if (mEndViewManager != null) {
            mEndViewManager.stopAnimator();
        }
        // check if scroll has been enabled
        if (mOnScrollListener != null) {
            // if enabled, just change the flag
            mPagerTriggered = false;
        } else {
            // if not, load more data until screen is filled
            loadMoreToFillScreen();
        }
    }

    /**
     * Called when the list items are not enough to fill the screen.
     * The task must be posted to the message queue of UI thread to avoid a recursive invoking.
     */
    private void loadMoreToFillScreen() {
        if (DEBUG) Log.d(TAG, "loadMoreToFillScreen() called with " + "");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // check if items have filled the screen height, if not, continue loading
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                if (DEBUG) Log.d(TAG, "visibleItemCount = " + visibleItemCount);
                if (DEBUG) Log.d(TAG, "totalItemCount = " + totalItemCount);

                if (visibleItemCount != 0 && totalItemCount > visibleItemCount + 1) {
                    // if data have filled screen, enable scrolling features
                    mOnScrollListener = new AutoPagerOnScrollListener();
                    mRecyclerView.addOnScrollListener(mOnScrollListener);
                    return;
                }

                if (inLastPage()) {
                    // if all data have been loaded, return
                    return;
                }
                // if there are still data that could be loaded to fill the screen, go on loading
                loadPage(mIndex + 1);
            }
        });
    }

    /**
     * Start loading data at {@param page},
     * the result must be delivered to the {@link #mRecyclerView} by calling {@link #setData(TreeMap)}
     * either sync or async
     *
     * @param page
     */
    protected void loadPage(int page) {
        mLoadPageMethod.loadPage(page);
    }

    /**
     * @return if the current page is the last page
     */
    protected boolean inLastPage() {
        return mLastPageIndex == mIndex;
    }

    /**
     * @return {@link #mLastPageIndex}
     */
    public int getLastPageIndex() {
        return mLastPageIndex;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void removeOnScrollListener() {
        // the listener must be removed from mRecyclerView and added back later, or it will cause the bug of not listening
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        // set listener to null and instantiate it later, because it's used as a flag
        mOnScrollListener = null;
    }

    public int getFirstPageIndex() {
        return mFirstPageIndex;
    }

    /**
     * A listener for the event of getting first group of data, which could be used to handle the animation of the empty view
     */
    public interface OnDataAttachedListener {
        void onDataAttached();
    }

    /**
     * The function interface which provide a function to load a certain page sync/async
     */
    public interface LoadPageMethod {
        void loadPage(int page);
    }

    /**
     * A enclosure manages items of the ending view and its animation
     */
    private static class EndViewManager {
        public View refresh;
        public TextView text;
        public ObjectAnimator animator;
        public int duration;

        public EndViewManager(View view) {
            if (DEBUG) Log.d(TAG, "EndViewDelegate() called with " + "view = [" + view.getId() + "]");
            this.refresh = view.findViewById(R.id.iv_end_refresh);
            this.text = (TextView) view.findViewById(R.id.tv_end);
            this.duration = view.getResources().getInteger(android.R.integer.config_mediumAnimTime);
            this.animator = ObjectAnimator.ofFloat(refresh, "rotation", 0f, 360f);
            animator.setDuration(duration);
            animator.setRepeatCount(ValueAnimator.INFINITE);
        }

        public void startAnimator() {
            animator.start();
            text.setText(R.string.loading);
        }

        public void stopAnimator() {
            animator.cancel();
            text.setText(R.string.update);
        }
    }

    /**
     * An OnScrollListener interpreting scrolling events into paging events
     */
    private class AutoPagerOnScrollListener extends RecyclerView.OnScrollListener {

        public AutoPagerOnScrollListener() {
            if (DEBUG) Log.d(TAG, "AutoPagerOnScrollListener() called with " + "");
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // if pager's been started, return
            if (mPagerTriggered) {
                return;
            }

            // get displaying info about items
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            // if scroll reaches the AUTO_PAGER_ZONE, pager starts
            if (firstVisibleItem >= totalItemCount - AUTO_PAGER_ZONE_SIZE - visibleItemCount) {
                mPagerTriggered = true;
                // if not at the end page, start load data for next page
                if (!inLastPage()) {
                    loadPage(mIndex + 1);
                }
            }
        }
    }
}
