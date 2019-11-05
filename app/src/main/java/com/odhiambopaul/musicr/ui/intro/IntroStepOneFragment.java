package com.odhiambopaul.musicr.ui.intro;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.ui.MainActivity;
import com.odhiambopaul.musicr.ui.widget.fragmentnavigationcontroller.SupportFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroStepOneFragment extends SupportFragment implements MainActivity.PermissionListener {
    private static final String TAG ="IntroStepOneFragment";


    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.allow_button) View mAllowButton;

    @OnClick(R.id.allow_button)
    void allowAccess() {
        getMainActivity().requestPermission();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        getMainActivity().setPermissionListener(this);
        return inflater.inflate(R.layout.grant_permission_screen,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mSwipeRefresh.setColorSchemeResources(R.color.FlatBlue);
        mSwipeRefresh.setOnRefreshListener(this::refreshData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removePermissionListener();

    }

    private void refreshData() {
        if(getMainActivity().checkSelfPermission()) onPermissionGranted();
        else mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onPermissionGranted() {
        mSwipeRefresh.setRefreshing(false);
        Log.d(TAG, "onPermissionGranted");
        getMainActivity().startGUI();
    }

    @Override
    public void onPermissionDenied() {
        mSwipeRefresh.setRefreshing(false);
        Log.d(TAG, "onPermissionDenied");
    }
}
