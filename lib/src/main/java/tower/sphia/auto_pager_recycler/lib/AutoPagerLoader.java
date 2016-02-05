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
    private final Lock mLock = Lock.locked();


    /**
     * tell the loader to target page to load
     * the loaded target page will be stored in the {@link TreeMap} container
     * <p/>
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
    public synchronized TreeMap<Integer, P> loadInBackground() {
        // This method is called on a background thread and should generate a
        // new set of pages to be delivered back to the client.
        if (AutoPagerManager.DEBUG) Log.d(TAG, "loadInBackground called with mTargetPage=" + mTargetPage);

        if (!mLock.check()) {
            throw new IllegalStateException(mLock.TAG + " is unlocked!");
//            mLock.lock();
        }

        // MUST create new TreeMap here, cuz LoaderManager will use `oldData!=newReturned`
        // to decide whether to call `onLoadFinished()` or not.
        TreeMap<Integer, P> pages = new TreeMap<>();

        TreeMap<Integer, P> oldData = getData();
        if (oldData != null) {
            pages.putAll(oldData);
        }

        try {
            P page = newPage(mTargetPage);

            if (mTargetPage == 1 && pages.containsKey(mTargetPage)) {
                pages.clear();
                if (AutoPagerManager.DEBUG) Log.d(TAG, "RELOADING");
            }
            pages.put(page.index(), page);
            if (AutoPagerManager.DEBUG) Log.d(TAG, "mTargetPage " + mTargetPage + " loaded");
        } catch (DataNotLoadedException e) {
            if (AutoPagerManager.DEBUG) Log.e(TAG, "loadInBackground Page " + mTargetPage + "not found");
        }
        if (AutoPagerManager.DEBUG) Log.d(TAG, "loadInBackground() returned pages.size() " + pages.size());
        return pages;
    }

    public synchronized void load(int page) {
        if (AutoPagerManager.DEBUG) Log.d(TAG, "load() called with " + "page = [" + page + "]");
        if (mLock.check()) {
            if (AutoPagerManager.DEBUG) mLock.log();
        } else {
            mLock.lock();
            setTargetPage(page);
            onContentChanged();
        }
    }

    private void setTargetPage(int page) {
        if (AutoPagerManager.DEBUG) {
            Log.d(TAG, "*******************************");
            Log.d(TAG, "setTargetPage() called with " + "page = [" + page + "]");
        }
        mTargetPage = page;
    }

    public synchronized void releaseLock() {
        if (mLock.check()) {
            mLock.release();
        }
    }

    private static class Lock {
        public final String TAG = "Lock#" + this.hashCode();
        private boolean locked = false;

        private Lock() {

        }

        public static Lock unlocked() {
            return new Lock();
        }

        public static Lock locked() {
            Lock lock = new Lock();
            lock.lock();
            return lock;
        }

        public synchronized boolean check() {
            return locked;
        }

        public void log() {
            Log.d(TAG, "access refused, lock being locked, ");
        }

        public synchronized void release() {
            if (!locked) {
                throw new IllegalStateException(TAG + " has been released");
            } else {
                if (AutoPagerManager.DEBUG) Log.d(TAG, "lock released");
                locked = false;
            }
        }

        public synchronized void lock() {
            if (locked) {
                throw new IllegalStateException(TAG + " has been locked");
            } else {
                if (AutoPagerManager.DEBUG) Log.d(TAG, "lock locked");
                locked = true;
            }
        }
    }
}
