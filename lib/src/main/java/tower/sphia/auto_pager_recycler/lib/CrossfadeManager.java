package tower.sphia.auto_pager_recycler.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Voyager on 9/24/2015.
 */
public class CrossfadeManager {
    View mEmptyView;
    View mContentView;

    private CrossfadeManager(View fadeOut, View fadeIn) {
        mEmptyView = fadeOut;
        mContentView = fadeIn;
    }


    public static CrossfadeManager setup(ViewGroup parent, View fadeOut, View fadeIn) {
        CrossfadeManager manager = new CrossfadeManager(fadeOut, fadeIn);
        fadeIn.setVisibility(View.GONE);
        parent.addView(fadeOut);
        return manager;
    }

    /**
     * Crossfade views on data loaded
     */
    public void startAnimation() {
        int shortAnimationDuration = mContentView.getResources().getInteger(android.R.integer.config_shortAnimTime);
        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);
        mContentView.animate().alpha(1f).setDuration(shortAnimationDuration).setListener(null);
        mEmptyView.animate().alpha(0f).setDuration(shortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mEmptyView.setVisibility(View.GONE);
            }
        });
    }
}