package tower.sphia.auto_pager_recycler.lib;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ScrollYDelegate;

/**
 * Created by Voyager on 10/4/2015.
 * A RecyclerFragment with support of PullToRefresh
 */
public abstract class RefreshableRecyclerFragment extends Fragment {
    public CrossfadeManager mCrossfadeManager;
    private RecyclerView mRecyclerView;
    private PullToRefreshLayout mPullToRefreshLayout;

    public void stopRefreshAnimation() {
        if (mPullToRefreshLayout.isRefreshing()) {
            mPullToRefreshLayout.setRefreshComplete();
        }
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This is the View which is created by RecyclerFragment
        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setItemDecoration();
        mRecyclerView.setVisibility(View.GONE);
        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(getActivity());
        // trying to solve the bug of progressbar
        mPullToRefreshLayout.addView(mRecyclerView);
        // When the fragment is restored by a ViewPager, this method is called while the instance is NOT recreated.
        // if the crossfade animation is setup again but not played, it's difficult to find another timing to play it and show the content,
        // leaving an empty view on screen while data have already been loaded
        setupEmptyView(inflater, mPullToRefreshLayout);
        if (!mEmpty) {
            startCrossfade();
        }
    // todo remember the scroll position
        return mPullToRefreshLayout;
    }

    protected void setupEmptyView(LayoutInflater inflater, ViewGroup pullToRefreshLayout) {
        View textView = inflater.inflate(R.layout.empty_text, pullToRefreshLayout, false);
        mCrossfadeManager =  CrossfadeManager.setup(pullToRefreshLayout, textView, mRecyclerView);
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0.3f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new CycleInterpolator(2));
        animator.setDuration(5200);
        animator.start();
    }

    private boolean mEmpty = true;
    public void startCrossfade() {
        mCrossfadeManager.startAnimation();
        mEmpty = false;
    }

    /**
     * Setup the PullToRefreshLayout
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())

                /* use ScrollYDelegate to provide PullToRefresh support for RecyclerView,
                 which implements ScrollingView */
                .useViewDelegate(RecyclerView.class, new ScrollYDelegate() {

                    /* Override this method to fix the bug of staring refresh on scroll down at any position.
                     * A check of first child item top position of RecyclerView is added to help decide whether to
                     * start refreshing. */
                    @Override
                    public boolean isReadyForPull(View view, float x, float y) {
                        RecyclerView recyclerView = (RecyclerView) view;
                        /* get the position of the top of the 1st child */
                        View first = recyclerView.getChildAt(0);
                        if (first != null) {
                            int top = first.getTop();
                            /* MUST check if the 1st child is at top, or the PullToRefresh will be triggered
                             * regardless of the actual position of RecyclerView
                             * TODO: if the item has a layout_margin, top == 0 is no longer satisfied */
                            return super.isReadyForPull(view, x, y) && top == 0;
                        } else {
                            return super.isReadyForPull(view, x, y);
                        }
                    }
                })
                        // We need to mark the ListView and it's Empty View as pullable
                        // This is because they are not direct children of the ViewGroup
                .theseChildrenArePullable(mRecyclerView)
                        // We can now complete the setup as desired
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View v) {
                        RefreshableRecyclerFragment.this.onRefreshStarted(v);
                    }
                })
                .setup(mPullToRefreshLayout);
    }

    protected abstract void onRefreshStarted(View v);

    /**
     * Add default divider to RecyclerView
     * You can override this method to modify/delete the divider
     */
    protected void setItemDecoration() {
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
    }


}
