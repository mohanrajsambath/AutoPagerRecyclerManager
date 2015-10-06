package tower.sphia.auto_pager_recycler.lib;

import android.os.Bundle;
import android.view.View;

import java.util.List;

/**
 * Created by Voyager on 10/4/2015.
 */
public abstract class AutoPagerRefreshableRecyclerFragment<G extends ElementGroup<E>, E> extends RefreshableRecyclerFragment {
    /**
     * Creating a fly weight instance in order to prevent from getting a null instance for external use,
     * such as adding a listener to the mgr
     */
    private AutoPagerRecyclerViewManager<G, E> mRecyclerViewManager = AutoPagerRecyclerViewManager.flyWeight();

    public AutoPagerRecyclerViewManager<G, E> getRecyclerViewManager() {
        return mRecyclerViewManager;
    }


    public void setData(List<G> groups) {
        mRecyclerViewManager.setData(groups);
    }

    public abstract void loadPage(int page);

    /**
     * OnScrollListener is removed to check whether reloaded items fill the screen
     * @param v
     */
    @Override
    protected void onRefreshStarted(View v) {
        mRecyclerViewManager.removeOnScrollListener();
    }

    protected abstract AutoPagerRecyclerAdapter<G, E> onCreateAdapter(AutoPagerRecyclerViewManager<G, E> viewManager);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerViewManager.setup(getRecyclerView(), onCreateAdapter(mRecyclerViewManager), new AutoPagerRecyclerViewManager.PageLoader() {
            @Override
            public void loadPage(int page) {
                AutoPagerRefreshableRecyclerFragment.this.loadPage(page);
            }
        });
        mRecyclerViewManager.addOnDataAttachedListener(new AutoPagerRecyclerViewManager.OnDataAttachedListener() {
            @Override
            public void onDataAttached() {
                startCrossfade();
            }
        });
    }
}
