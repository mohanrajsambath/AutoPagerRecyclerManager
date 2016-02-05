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
    static int AUTO_PAGER_ZONE_SIZE = 3;
    static boolean DEBUG = false;
    private final String TAG = "AutoPagerManager#" + this.hashCode();
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

    public static void enalbleDebug(boolean debug) {
        AutoPagerManager.DEBUG = debug;
    }

    public static void setAutoPagerZoneSize(int autoPagerZoneSize) {
        if (autoPagerZoneSize <= 0) {
            throw new IllegalArgumentException();
        }
        AUTO_PAGER_ZONE_SIZE = autoPagerZoneSize;
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
        if (DEBUG) Log.d(TAG, "onClickEnding() called " + "reloading the last page");
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
                for (OnDataAttachedListener onDataAttachedListener : mOnDataAttachedListeners) {
                    onDataAttachedListener.onDataAttached();
                }
            }
            mLastPageIndex = last.last();
            mAdapter.setInLastPage(inLastPage());
            mAdapter.setItems(pages);
            if (mEndViewManager != null) {
                mEndViewManager.stopAnimator();
            }
            // check if scroll has been enabled
            if (mOnScrollListener != null) { // FIXME: 2/5/2016 it's null after vp destroyed the frag
                // if enabled, just change the flag
                mPagerTriggered = false;
            } else {
                // if not, load more data until screen is filled
                checkIsScreenFilled();
            }
        }
    }

    /**
     * Called when the list items are not enough to fill the screen.
     * The task must be posted to the message queue of UI thread to avoid a recursive invoking.
     */
    private void checkIsScreenFilled() {
        if (DEBUG) Log.d(TAG, "checkIsScreenFilled() called");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "execute runnable#" + hashCode());
                // check if items have filled the screen height, if not, continue loading
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                if (DEBUG) Log.d(TAG, "visibleItemCount = " + visibleItemCount);
                if (DEBUG) Log.d(TAG, "totalItemCount = " + totalItemCount);

                if (visibleItemCount != 0 && totalItemCount > visibleItemCount + 3) {
                    // if data have filled screen, enable scrolling features
                    mOnScrollListener = new AutoPagerOnScrollListener();
                    mRecyclerView.addOnScrollListener(mOnScrollListener);
                } else {
                    if (!inLastPage()) {
                        // if there are still data that could be loaded to fill the screen, go on loading
                        loadPage(mIndex + 1);
                    }
                }
            }
        };
        if (DEBUG) Log.d(TAG, "enqueue runnable#" + runnable.hashCode());
        mHandler.post(runnable);
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
        /**
         * @param page The first page is always 1. So you should add a offset if your page doesn't starts with 1.
         */
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
//            if (DEBUG) Log.d(TAG, "EndViewDelegate() called with " + "view = [" + view.getId() + "]");
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
