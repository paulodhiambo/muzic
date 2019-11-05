package com.odhiambopaul.musicr.ui.widget.fragmentnavigationcontroller;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.odhiambopaul.musicr.ui.MainActivity;
import com.odhiambopaul.musicr.util.Tool;

import java.lang.ref.WeakReference;

/**
 * Created by burt on 2016. 5. 24..
 */
public abstract class SupportFragment extends Fragment {
    private static final String TAG ="SupportFragment";
    public static int PRESENT_STYLE_DEFAULT = PresentStyle.ACCORDION_LEFT;

    private WeakReference<FragmentNavigationController> weakFragmentNaviagationController = null;
    protected boolean animatable = true;
    private AndroidFragmentFrameLayout innerRootLayout = null;
    private View contentView = null;
    private PresentStyle presentStyle = null;
    public boolean isWhiteTheme(boolean current) {
        saveTheme(current);
        return true;
    }
    public boolean getSavedTheme() {
        return savedTheme;
    }

    protected boolean savedTheme = true;
    public void saveTheme(boolean b) {
        Log.d(TAG, "saveTheme: b = " +b);
        savedTheme = b;
    }
    ;
    public boolean isWhiteTheme() {
        return true;
    }
    public void onSetStatusBarMargin(int value) {

    }


    public FragmentNavigationController getNavigationController() {
        if(weakFragmentNaviagationController == null)
            return null;
        return weakFragmentNaviagationController.get();
    }

    protected void setNavigationController(FragmentNavigationController fragmentNavigationController) {
        weakFragmentNaviagationController = new WeakReference<>(fragmentNavigationController);
    }

    public MainActivity getMainActivity() {
     return (MainActivity) getActivity();
    }


    protected void setAnimatable(boolean animatable) {
        this.animatable = animatable;
    }

   protected void setPresentStyle(PresentStyle presentStyle) {
        this.presentStyle = presentStyle;
    }

    public int getPresentTransition() {
        return PRESENT_STYLE_DEFAULT;
    }
    public boolean isReadyToDismiss(){
        return true;
    }

    @Nullable
    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(getClass().getName(), "onCreateView");
        View v = onCreateView(inflater, container);
        if(v == null) return v;
        contentView = v;
        innerRootLayout = new AndroidFragmentFrameLayout(getActivity());
        innerRootLayout.addView(contentView);
        return innerRootLayout;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onSetStatusBarMargin(Tool.getStatusHeight(getResources()));

    }

    @Nullable
    abstract protected View onCreateView(LayoutInflater inflater, ViewGroup container);

    /**
     * This is the layout for wrapping contentView
     * @return AndroidFragmentFrameLayout
     */
    public AndroidFragmentFrameLayout getRootLayout() {
        return innerRootLayout;
    }

    /**
     * This is the layout-view which is definded by user.
     * @return content view
     */
    public View getContentView() {
        return contentView;
    }

    @Override
    public Animator onCreateAnimator(final int transit, final boolean enter, int nextAnim) {
        if(animatable == false) {
            animatable = true;
            return null;
        }

        FragmentNavigationController nav =  getNavigationController();
        if(nav == null) {
            return null; //no animatable
        }

        if(presentStyle == null) {
            return null; //no animatable
        }

        Animator animator = null;
        if(transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {

            if (enter) {
                int id = presentStyle.getOpenEnterAnimatorId();
                if(id != -1) animator = AnimatorInflater.loadAnimator(getActivity(), id);
            } else {
                int id = presentStyle.getOpenExitAnimatorId();
                if(id != -1) animator = AnimatorInflater.loadAnimator(getActivity(), id);
            }

        } else {

            if (enter) {
                int id = presentStyle.getCloseEnterAnimatorId();
                if(id != -1) animator = AnimatorInflater.loadAnimator(getActivity(), id);
            } else {
                int id = presentStyle.getCloseExitAnimatorId();
                if(id != -1) animator = AnimatorInflater.loadAnimator(getActivity(), id);
            }
        }
        if(animator != null) {
            animator.setInterpolator(nav.getInterpolator());
            animator.setDuration(nav.getDuration());
        }

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

                if(transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {

                    if(enter) {
                        onShowFragment();
                    } else {
                        //onHideFragment();
                    }
                } else {

                    if(enter) {
                        onShowFragment();
                    } else {
                        onHideFragment();
                    }
                }


            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }


    public void onShowFragment() {}
    public void onHideFragment() {}

}
