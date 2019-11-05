package com.odhiambopaul.musicr.ui.bottomnavigationtab.feature;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.service.MusicServiceEventListener;
import com.odhiambopaul.musicr.ui.BaseActivity;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.pager.PlaylistPagerFragment;
import com.odhiambopaul.musicr.loader.PlaylistLoader;
import com.odhiambopaul.musicr.loader.SongLoader;
import com.odhiambopaul.musicr.model.Playlist;
import com.odhiambopaul.musicr.ui.widget.fragmentnavigationcontroller.SupportFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeatureTabFragment extends SupportFragment implements FeaturePlaylistAdapter.PlaylistClickListener, MusicServiceEventListener {
    private static final String TAG ="FeatureTabFragment";

    @BindView(R.id.status_bar)
    View mStatusView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.scroll_view)
    NestedScrollView mNestedScrollView;

    FeatureLinearHolder mFeatureLinearHolder;

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.feature_tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.FlatOrange);
        mSwipeRefreshLayout.setOnRefreshListener(this::refreshData);

        mFeatureLinearHolder = new FeatureLinearHolder(getActivity(),mNestedScrollView);
        mFeatureLinearHolder.setPlaylistItemClick(this);

        if(getActivity() instanceof BaseActivity) {
            ((BaseActivity)getActivity()).addMusicServiceEventListener(this);
        }
        refreshData();
    }

    private void refreshData() {
        if(getActivity()!=null) {
            mFeatureLinearHolder.setSuggestedPlaylists(PlaylistLoader.getAllPlaylistsWithAuto(getActivity()));
            mFeatureLinearHolder.setSuggestedSongs(SongLoader.getAllSongs(getActivity()));
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSetStatusBarMargin(int value) {
        if(mStatusView!=null) {
            mStatusView.getLayoutParams().height = value;
            mStatusView.requestLayout();
        }
    }

    @Override
    public void onClickPlaylist(Playlist playlist, @org.jetbrains.annotations.Nullable Bitmap bitmap) {
        SupportFragment sf = PlaylistPagerFragment.newInstance(getContext(),playlist,bitmap);
        getNavigationController().presentFragment(sf);
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {

    }

    @Override
    public void onPlayStateChanged() {

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {
        refreshData();
    }

    @Override
    public void onDestroyView() {
        if(getActivity() instanceof BaseActivity) {
            ((BaseActivity)getActivity()).removeMusicServiceEventListener(this);
        }

        super.onDestroyView();
    }
}
