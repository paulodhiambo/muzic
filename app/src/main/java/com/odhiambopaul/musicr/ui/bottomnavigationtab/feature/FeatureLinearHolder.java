package com.odhiambopaul.musicr.ui.bottomnavigationtab.feature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.model.Playlist;
import com.odhiambopaul.musicr.model.Song;
import com.odhiambopaul.musicr.ui.MainActivity;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.BackStackController;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.library.LibraryTabFragment;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.library.playlist.PlaylistChildTab;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.library.song.SongChildTab;
import com.odhiambopaul.musicr.util.Animation;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class FeatureLinearHolder {

    private Context mContext;

    @BindView(R.id.playlist_frame)
    ViewGroup mPlayListFrame;

    @BindView(R.id.song_frame)
    ViewGroup mSongFrame;

    public PlaylistMiniAdapter getPlaylistMiniAdapter() {
        return mPlaylistMiniAdapter;
    }

    PlaylistMiniAdapter mPlaylistMiniAdapter;
    SongMiniAdapter mSongMiniAdapter;

    public void setPlaylistItemClick(FeaturePlaylistAdapter.PlaylistClickListener listener) {
        if(mPlaylistMiniAdapter!=null) mPlaylistMiniAdapter.setItemClickListener(listener);
    }

    public FeatureLinearHolder(Context context, ViewGroup linearLayout) {
        this.mContext = context;
        View v = LayoutInflater.from(context).inflate(R.layout.feature_tab_body,linearLayout,false);
        ButterKnife.bind(this,v);
        mPlayListFrame.setVisibility(View.GONE);

        linearLayout.removeAllViews();
        linearLayout.addView(v);

        mPlaylistMiniAdapter = new PlaylistMiniAdapter(mPlayListFrame);
        mSongMiniAdapter = new SongMiniAdapter(mSongFrame);
    }

    public void setSuggestedPlaylists(List<Playlist> list) {
        mPlaylistMiniAdapter.bind(list);
        if(mPlaylistMiniAdapter.getItemCount()!=0) mPlayListFrame.setVisibility(View.VISIBLE);
        else mPlayListFrame.setVisibility(View.GONE);
    }

    public void setSuggestedSongs(List<Song> song) {
        mSongMiniAdapter.bind(song);
        if(mSongMiniAdapter.getItemCount()!=0) mSongFrame.setVisibility(View.VISIBLE);
        else mSongFrame.setVisibility(View.GONE);
    }

    public class PlaylistMiniAdapter {
        private View mItemView;
        @BindView(R.id.back_top_header) View mHeaderPanel;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
        @BindView(R.id.number) TextView mCount;

        FeaturePlaylistAdapter mPlaylistAdapter;

        @OnClick(R.id.back_top_header)
        void goToPlaylistChildTab() {
            if(mContext instanceof MainActivity) {
                BackStackController controller = ((MainActivity) mContext).getBackStackController();
                if (controller != null) {
                    LibraryTabFragment libraryTabFragment = controller.navigateToLibraryTab();
                    if(libraryTabFragment!=null) libraryTabFragment.navigateToTab(PlaylistChildTab.TAG);
                }
            }
        }

        private void setItemClickListener(FeaturePlaylistAdapter.PlaylistClickListener listener) {
            if(mPlaylistAdapter!=null) mPlaylistAdapter.setListener(listener);
        }

        PlaylistMiniAdapter(View v) {
            this.mItemView = v;
            ButterKnife.bind(this,v);
            mPlaylistAdapter = new FeaturePlaylistAdapter(mContext,true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false));

            mRecyclerView.setAdapter(mPlaylistAdapter);
            OverScrollDecoratorHelper.setUpOverScroll(mRecyclerView,OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);

        }

        @SuppressLint("DefaultLocale")
        public void bind(List<Playlist> playlists) {

            mPlaylistAdapter.setData(playlists);
            mCount.setText(String.format("%d", mPlaylistAdapter.getItemCount()));
        }
        public void notifyDataSetChanged() {
            mPlaylistAdapter.notifyDataSetChanged();
        }
        public int getItemCount() {
            return mPlaylistAdapter.getItemCount();
        }
    }
    public class SongMiniAdapter {
        private View mItemView;
        @BindView(R.id.back_top_header) View mHeaderPanel;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
        @BindView(R.id.number) TextView mCount;
        @BindView(R.id.refresh)
        ImageView mRefreshButton;

        @OnClick({R.id.see_all, R.id.back_top_header})
        void seeAll() {
            if(mContext instanceof MainActivity) {
                BackStackController controller = ((MainActivity) mContext).getBackStackController();
                if (controller != null) {
                  LibraryTabFragment libraryTabFragment = controller.navigateToLibraryTab();
                          if(libraryTabFragment!=null) libraryTabFragment.navigateToTab(SongChildTab.TAG);
                }
            }
        }

        @OnClick(R.id.refresh_front)
        void refresh() {
            mRefreshButton.animate().rotationBy(360).setInterpolator(Animation.getInterpolator(6)).setDuration(650);
            mAdapter.initializeSong();
        }

        FeatureSongAdapter mAdapter;

        SongMiniAdapter(View v) {
            this.mItemView = v;
            ButterKnife.bind(this,v);
            mAdapter = new FeatureSongAdapter(mContext);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));

            mRecyclerView.setAdapter(mAdapter);

        }

        @SuppressLint("DefaultLocale")
        public void bind(List<Song> playlists) {

            mAdapter.setData(playlists);
            mCount.setText(String.format("%d", mAdapter.getAllItemCount()));
        }
        public void notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged();
        }
        public int getItemCount() {
            return mAdapter.getItemCount();
        }
    }


}
