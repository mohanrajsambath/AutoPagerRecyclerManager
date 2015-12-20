package tower.sphia.auto_pager_recycler.lib;

import android.os.Bundle;
import android.view.View;

import java.util.TreeMap;

/**
 * A fragment which have implemented basic functions of an auto-pager RecyclerView, leaving
 * the adapter and the function to load a certain page still abstract.
 */
public abstract class BaseAutoPagerFragment<P extends Page<E>, E> extends BaseRecyclerFragment implements AutoPagerManager.LoadPageMethod {

    private AutoPagerManager<P, E> mAutoPagerManager;

    public AutoPagerManager<P, E> getAutoPagerManager() {
        return mAutoPagerManager;
    }


    public void setData(TreeMap<Integer, P> pages) {
        mAutoPagerManager.setData(pages);
    }

    @Override
    public abstract void loadPage(int page);

    protected abstract AutoPagerAdapter<P, E> onCreateAdapter();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAutoPagerManager = new AutoPagerManager<>(getRecyclerView(), this);
        AutoPagerAdapter<P, E> adapter = onCreateAdapter();
        mAutoPagerManager.setAdapter(adapter);
    }
}
