package tower.sphia.auto_pager_recycler.lib;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A base default RecyclerFragment. This fragment could be substituted by any other implementation of {@link Fragment}
 * as long as it could supply a {@link RecyclerView}. Or you could simply override the methods in its implementations.
 */
public class BaseRecyclerFragment extends Fragment {
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This is the View which is created by RecyclerFragment
        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setItemDecoration();
        return mRecyclerView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Add default divider to RecyclerView
     * You can override this method to modify/delete the divider
     */
    protected void setItemDecoration() {
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
    }

}
