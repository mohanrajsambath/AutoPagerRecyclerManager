package tower.sphia.auto_pager_recycler.lib;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Voyager on 10/4/2015.
 */
public abstract class LoaderAutoPagerRefreshableRecyclerFragment<G extends ElementGroup<E>, E>
        extends AutoPagerRefreshableRecyclerFragment<G, E>
        implements LoaderManager.LoaderCallbacks<List<G>> {


    public static final int LOADER_ID = 1;

    @Override
    public void loadPage(int page) {
        getAutoPagerLoader().setTargetPage(page).onContentChanged();
    }

    @SuppressWarnings("unchecked")
    protected AutoPagerLoader<G> getAutoPagerLoader() {
        return (AutoPagerLoader) getLoaderManager().getLoader(LOADER_ID);
    }

    @Override
    protected void onRefreshStarted(View v) {
        super.onRefreshStarted(v);
        getAutoPagerLoader().setTargetPage(1).onContentChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public abstract Loader<List<G>> onCreateLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader<List<G>> loader, List<G> data) {
        if (data.size() == 0) return;
        setData(data);
        stopRefreshAnimation();
    }

    @Override
    public void onLoaderReset(Loader<List<G>> loader) {
        setData(new ArrayList<G>());
    }
}
