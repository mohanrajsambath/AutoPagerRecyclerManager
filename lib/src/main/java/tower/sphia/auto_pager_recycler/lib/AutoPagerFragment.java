package tower.sphia.auto_pager_recycler.lib;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.TreeMap;

/**
 * A implementation of {@link BaseAutoPagerFragment} with the Loader pattern.
 * This class is very easy to use, you only need to implement {@link #onCreateLoader(int, Bundle)}
 * and {@link #onCreateAdapter()} to make it work.
 */
public abstract class AutoPagerFragment<P extends Page<E>, E>
        extends BaseAutoPagerFragment<P, E>
        implements LoaderManager.LoaderCallbacks<TreeMap<Integer, P>> {

    private static final int LOADER_ID = 1;
    private static final String TAG = "AutoPagerFragment";

    @Override
    public void loadPage(int index) {
        if (AutoPagerManager.DEBUG) Log.d(TAG, "loadPage() called with " + "index = [" + index + "]");
        try {
            getAutoPagerLoader().load(index);
        } catch (FragmentNotAttachedException e) {
            if (AutoPagerManager.DEBUG)
                Log.d(TAG, "loadPage " + index + " failed, unable to get lm, fragment not attached");
        }
    }

    @SuppressWarnings("unchecked")
    protected AutoPagerLoader<P> getAutoPagerLoader() throws FragmentNotAttachedException {
        if (getActivity() != null) {
            LoaderManager loaderManager = getLoaderManager();  // IllegalStateException: Fragment BarFragment{eb7ad12} not attached to Activity
            return (AutoPagerLoader) loaderManager.getLoader(LOADER_ID);
        } else {
            throw new FragmentNotAttachedException();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public abstract Loader<TreeMap<Integer, P>> onCreateLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader<TreeMap<Integer, P>> loader, TreeMap<Integer, P> data) {
        if (AutoPagerManager.DEBUG)
            Log.d(TAG, "onLoadFinished() called with " + "data.size() = [" + data.size() + "]");
        ((AutoPagerLoader) loader).releaseLock();
        if (data.size() == 0) return;
        setData(data);
        // why the lock has been released when loading more?
        // maybe it's because that loader won't call load method when restoring data
    }

    @Override
    public void onLoaderReset(Loader<TreeMap<Integer, P>> loader) {
        if (AutoPagerManager.DEBUG) Log.d(TAG, "onLoaderReset() called with " + "loader = [" + loader + "]");
        setData(new TreeMap<Integer, P>());
    }

    public static class FragmentNotAttachedException extends Exception {

    }
}
