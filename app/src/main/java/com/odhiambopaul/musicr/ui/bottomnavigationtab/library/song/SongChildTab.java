package com.odhiambopaul.musicr.ui.bottomnavigationtab.library.song;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.odhiambopaul.musicr.App;
import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.loader.SongLoader;
import com.odhiambopaul.musicr.model.Song;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.BaseMusicServiceFragment;
import com.odhiambopaul.musicr.ui.bottomsheet.SortOrderBottomSheet;
import com.odhiambopaul.musicr.util.Tool;
import com.odhiambopaul.musicr.util.Util;
import com.odhiambopaul.musicr.util.Animation;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SongChildTab extends BaseMusicServiceFragment implements SortOrderBottomSheet.SortOrderChangedListener, PreviewRandomPlayAdapter.FirstItemCallBack{
    public static final String TAG ="SongChildTab";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

//    @BindView(R.id.preview_shuffle_list)
//    RecyclerView mPreviewRecyclerView;

    @BindView(R.id.refresh)
    ImageView mRefresh;

    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.description)
    TextView mArtist;

    private int mCurrentSortOrder = 0;
    private void initSortOrder() {
         mCurrentSortOrder = App.getInstance().getPreferencesUtility().getSongChildSortOrder();
    }

//    @BindView(R.id.top_background) View mTopBackground;
//    @BindView(R.id.bottom_background) View mBottomBackground;
//    @BindView(R.id.random_header) View mRandomHeader;
//    @BindView(R.id.shuffle_button) View ShuffleButton;


    @OnClick({R.id.preview_random_panel})
     void shuffle() {
        mAdapter.shuffle();
    }

    SongChildAdapter mAdapter;
//    PreviewRandomPlayAdapter mPreviewAdapter;

    @OnClick(R.id.refresh)
    void refresh() {
        mRefresh.animate().rotationBy(360).setInterpolator(Animation.getInterpolator(6)).setDuration(650);
        mRefresh.postDelayed(mAdapter::randomize,300);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.song_child_tab,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        initSortOrder();

        mAdapter = new SongChildAdapter(getActivity());
        mAdapter.setCallBack(this);
        mAdapter.setSortOrderChangedListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setAdapter(mAdapter);

        refreshData();
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();
        super.onDestroyView();
    }

    private void refreshData() {
        ArrayList<Song> songs = SongLoader.getAllSongs(getActivity(),SortOrderBottomSheet.mSortOrderCodes[mCurrentSortOrder]);
        mAdapter.setData(songs);
        showOrHidePreview(!songs.isEmpty());

    }
    private void showOrHidePreview(boolean show) {
        int v = show ? View.VISIBLE : View.GONE;

            mImage.setVisibility(v);
            mRefresh.setVisibility(v);
            mTitle.setVisibility(v);
            mArtist.setVisibility(v);
    }

    @Override
    public void onFirstItemCreated(Song song) {
        mTitle.setText(song.title);
        mArtist.setText(song.artistName);

        Picasso.get()
                .load(Util.getAlbumArtUri(song.albumId))
                .placeholder(R.drawable.music_style)
                .error(R.drawable.music_empty)
                .into(mImage);

    }

    @Override
    public void onPlayingMetaChanged() {
        if(mRecyclerView instanceof FastScrollRecyclerView) {
            FastScrollRecyclerView recyclerView = ((FastScrollRecyclerView)mRecyclerView);
            recyclerView.setPopupBgColor(Tool.getHeavyColor());
            recyclerView.setThumbColor(Tool.getHeavyColor());
        }

        if(mAdapter!=null)mAdapter.notifyOnMediaStateChanged();
    }

    @Override
    public void onPlayStateChanged() {
        if(mAdapter!=null)mAdapter.notifyOnMediaStateChanged();
    }

    @Override
    public void onMediaStoreChanged() {
        ArrayList<Song> songs = SongLoader.getAllSongs(getActivity(),SortOrderBottomSheet.mSortOrderCodes[mCurrentSortOrder]);
        mAdapter.setData(songs);
        showOrHidePreview(!songs.isEmpty());
    }

    @Override
    public int getSavedOrder() {
        return mCurrentSortOrder;
    }

    @Override
    public void onOrderChanged(int newType, String name) {
        if(mCurrentSortOrder!=newType) {
            mCurrentSortOrder = newType;
            App.getInstance().getPreferencesUtility().setSongChildSortOrder(mCurrentSortOrder);
            refreshData();
        }
    }
}
