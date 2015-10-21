package tower.sphia.auto_pager_recycler.lib;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Voyager on 10/4/2015.
 * This class manages the Recycler and its Adapter to make them cooperate for the auto-pager feature,
 * setAdapter() must be called.
 *
 * Using composition and delegation to support any subclass or wrapper of a {@link RecyclerView}
 */
public class AutoPagerRecyclerViewManager<G extends ElementGroup<E>, E> {
    /**
     * Next page loading is started when the number of remaining invisible items equals AUTO_PAGER_ZONE_SIZE
     */
    public static final int AUTO_PAGER_ZONE_SIZE = 3;
    static boolean DEBUG = false;
    /**
     * A flag whether next page loading has been started
     */
    private boolean mNextPageTriggered;
    /**
     * The adapter for recyclerView
     */
    private AutoPagerRecyclerAdapter<G, E> mAdapter;
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
     * The count of all pages to be loaded
     */
    private int mPageAllCount = -1;
    /**
     * The current page index
     */
    private int mPageCurrentCount;
    private List<OnDataAttachedListener> mOnDataAttachedListeners = new ArrayList<>();
    private PageLoader mPageLoader;

    @Deprecated
    private AutoPagerRecyclerViewManager(RecyclerView recyclerView, PageLoader pageLoader) {
        mRecyclerView = recyclerView;
        mNextPageTriggered = false;
        mHandler = new Handler(Looper.getMainLooper());
        mLayoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        mPageLoader = pageLoader;
    }

    public AutoPagerRecyclerViewManager() {

    }

    /**
     * The fly weight static factory method. Only a handle of the class is created, because the time of
     * creating {@link #mRecyclerView} is uncontrollable sometimes, for example in a Fragment lifecycle.
     *
     * The fly weight factory allows you to configure the instance before actually initialized.
     *
     * @return the fly weight instance, {@link #setup(RecyclerView, AutoPagerRecyclerAdapter, PageLoader)} must be
     * called later
     */
    public static <G extends ElementGroup<E>, E> AutoPagerRecyclerViewManager<G, E> flyWeight() {
        return new AutoPagerRecyclerViewManager<>();
    }

    /**
     * The actual constructor.
     *
     * @param recyclerView the RecyclerView to be wrapped
     * @param adapter the adapter handling data
     * @param pageLoader the function interface which provide a function to load a certain page sync/async
     */
    public void setup(RecyclerView recyclerView, AutoPagerRecyclerAdapter<G, E> adapter, PageLoader pageLoader) {
        mRecyclerView = recyclerView;
        setAdapter(adapter);
        mNextPageTriggered = false;
        mHandler = new Handler(Looper.getMainLooper());
        mLayoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
        mPageLoader = pageLoader;
    }

    public AutoPagerRecyclerAdapter<G, E> getAdapter() {
        return mAdapter;
    }

    /**
     * @param adapter the implementation of RecyclerWithFooterAdapter
     */
    public void setAdapter(AutoPagerRecyclerAdapter<G, E> adapter) {
        mAdapter = adapter;
        mRecyclerView.setAdapter(adapter);
    }

    public void addOnDataAttachedListener(OnDataAttachedListener onDataAttachedListener) {
        mOnDataAttachedListeners.add(onDataAttachedListener);
    }

    public void setPageLoader(PageLoader pageLoader) {
        mPageLoader = pageLoader;
    }

    /**
     * Delegate the {@link AutoPagerRecyclerAdapter#setData(List)} method to
     * retrieve data info and/or do some initialization work
     *
     * @param groups
     */
    public void setData(@NonNull List<G> groups) {
        int size = groups.size();
        // update mPageCurrentCount
        if (size != 0) {
            G last = groups.get(size - 1);
            mPageCurrentCount = last.getPageCurrentCount();
            // check if first page have been loaded, if not, do some initialization work
            if (mPageAllCount == -1) {
                mPageAllCount = last.getPageAllCount();
                for (OnDataAttachedListener onDataAttachedListener : mOnDataAttachedListeners) {
                    onDataAttachedListener.onDataAttached();
                }
            }
        }
        mAdapter.setData(groups);
        // check if scroll has been enabled
        if (mOnScrollListener != null) {
            // if enabled, just change the flag
            mNextPageTriggered = false;
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // check if items have filled the screen height, if not, continue loading
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();

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
                loadPage(mPageCurrentCount + 1);
            }
        });
    }
    /**
     * Start loading data at {@param page},
     * the result must be delivered to the {@link #mRecyclerView} by calling {@link #setData(List)}
     * either sync or async
     *
     * @param page
     */
    protected void loadPage(int page) {
        mPageLoader.loadPage(page);
    }

    /**
     * @return if current page is the last page
     */
    protected boolean inLastPage() {
        return mPageAllCount == mPageCurrentCount;
    }

    /**
     * @return {@link #mPageAllCount}
     */
    public int getPageAllCount() {
        return mPageAllCount;
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
     * A listener for the event of getting first group of data, which could be used to handle the empty view
     */
    public interface OnDataAttachedListener {
        void onDataAttached();
    }

    /**
     * The function interface which provide a function to load a certain page sync/async
     */
    public interface PageLoader {
        void loadPage(int page);
    }

    /**
     * An OnScrollListener interpreting scrolling events into paging events
     */
    private class AutoPagerOnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // if pager's been started, return
            if (mNextPageTriggered) {
                return;
            }

            // get displaying info about items
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            // if scroll reaches the AUTO_PAGER_ZONE, pager starts
            if (firstVisibleItem >= totalItemCount - AUTO_PAGER_ZONE_SIZE - visibleItemCount) {
                mNextPageTriggered = true;
                // if not at the last page, start load data for next page
                if (!inLastPage()) {
                    loadPage(mPageCurrentCount + 1);
                }
            }
        }
    }
}
