package tower.sphia.auto_pager_recycler.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.TreeMap;

/**
 * A loader which loads the target page and maintains all loaded data.
 */
public abstract class AutoPagerLoader<P extends Page<?>> extends AsyncTaskLoaderImpl<TreeMap<Integer, P>> {
    private static final String TAG = "AutoPagerLoader";
    /**
     * tell the loader to target page to load
     * the loaded target page will be stored in the {@link TreeMap} container
     *
     * NOTE: the default index of first page is 1, if your page begins with 0, just make a offset in {@link #newPage(int)}
     */
    private int mTargetPage = 1;

    public AutoPagerLoader(Context ctx) {
        super(ctx);
    }

    @Override
    protected void releaseResources(TreeMap<Integer, P> data) {
    }

    /**
     * Implement this method to get a page object of a certain index (from network, database etc.)
     * The index of first page should be moved to 1
     *
     * @param index the index to be loaded
     * @return the object instance for the index
     */
    @NonNull
    protected abstract P newPage(int index) throws DataNotLoadedException;

    @Override
    public TreeMap<Integer, P> loadInBackground() {
        // This method is called on a background thread and should generate a
        // new set of pages to be delivered back to the client.

        // MUST create new TreeMap here, cuz LoaderManager will use `oldData!=newReturned`
        // to decide whether to call `onLoadFinished()` or not.

        TreeMap<Integer, P> pages = new TreeMap<>();

        TreeMap<Integer, P> oldData = getData();
        if (oldData != null) {
            pages.putAll(oldData);
        }

        try {
            P page = newPage(mTargetPage);

            if (mTargetPage == 1) {
                // case for reloading all
                if (pages.containsKey(mTargetPage)) {
                    pages.clear();
                    if (AutoPagerManager.DEBUG) Log.d(TAG, "RELOADING");
                }
            }
            pages.put(page.index(), page);
            if (AutoPagerManager.DEBUG) Log.d(TAG, "loadInBackground mTargetPage " + mTargetPage);
        } catch (DataNotLoadedException e) {
            if (AutoPagerManager.DEBUG) Log.e(TAG, "loadInBackground Page " + mTargetPage + "not found");
        }
        if (AutoPagerManager.DEBUG) Log.d(TAG, "loadInBackground() returned pages.size() " + pages.size());
        return pages;
    }

    public AutoPagerLoader<P> setTargetPage(int page) {
        mTargetPage = page;
        return this;
    }
}
