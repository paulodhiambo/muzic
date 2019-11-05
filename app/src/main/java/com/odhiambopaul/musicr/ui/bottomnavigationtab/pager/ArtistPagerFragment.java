package com.odhiambopaul.musicr.ui.bottomnavigationtab.pager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.glide.ArtistGlideRequest;
import com.odhiambopaul.musicr.glide.GlideApp;
import com.odhiambopaul.musicr.model.Artist;
import com.odhiambopaul.musicr.service.MusicServiceEventListener;
import com.odhiambopaul.musicr.ui.widget.fragmentnavigationcontroller.SupportFragment;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class ArtistPagerFragment extends SupportFragment implements MusicServiceEventListener {
    private static final String TAG = "ArtistPagerFragment";
    private static final String ARTIST = "artist";
    public static ArtistPagerFragment newInstance(Artist artist) {

        Bundle args = new Bundle();
        if(artist!=null)
        args.putParcelable(ARTIST,artist);

        ArtistPagerFragment fragment = new ArtistPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @BindView(R.id.status_bar) View mStatusBar;

    @BindView(R.id.root) View mRoot;

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
        mStatusBar.requestLayout();
    }

    private Artist mArtist;

    @BindView(R.id.title)
    TextView mArtistText;


    @BindView(R.id.big_image)
    PhotoView mBigImage;

    @BindView(R.id.group)
    Group mGroup;

    @BindView(R.id.description) TextView mWiki;

    private boolean mBlockPhotoView = true;

/*   @OnTouch(R.id.big_image)
    boolean onTouchPhotoView(View view, MotionEvent event) {
        if(!mBlockPhotoView) {
            return view.onTouchEvent(event);
        }
        return mRoot.onTouchEvent(event);
    }*/

    @OnTouch(R.id.big_behind)
    boolean onTouchBigBehind(View view, MotionEvent event) {
        if(!mBlockPhotoView) {
            return false;
        } else {
            mRoot.onTouchEvent(event);
            return true;
        }
    }

    @BindView(R.id.fullscreen) ImageView mFullScreenButton;

    @OnClick(R.id.fullscreen)
    void fullScreen() {
        mBlockPhotoView = !mBlockPhotoView;
        if(mBlockPhotoView) {
            mGroup.setVisibility(View.VISIBLE);
            mBigImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mBigImage.setBackgroundResource(android.R.color.transparent);
            mFullScreenButton.setImageResource(R.drawable.fullscreen);
        }
        else {
            mGroup.setVisibility(View.GONE);
            mBigImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mBigImage.setBackgroundResource(android.R.color.black);
            mFullScreenButton.setImageResource(R.drawable.minimize);
        }
    }

    @OnClick(R.id.preview_button)
    void previewAllSong() {
        mAdapter.previewAll(true);
    }

    @OnClick(R.id.back)
    void goBack() {
        getMainActivity().onBackPressed();
    }

    @OnClick(R.id.play)
    void shuffle() {
        mAdapter.shuffle();
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private SongInArtistPagerAdapter mAdapter;

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.artist_pager_middle,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        ButterKnife.bind(this,view);

        Bundle bundle = getArguments();
        if(bundle!=null) {
            mArtist = bundle.getParcelable(ARTIST);
        }
        mAdapter = new SongInArtistPagerAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        refreshData();
    }
    private void updateSongs() {
        if(mArtist==null) return;
        mAdapter.setData(mArtist.getSongs());
    }

    public void refreshData() {
        if(mArtist==null) return;
        mArtistText.setText(mArtist.getName());
        String bio ="";
        if(!bio.isEmpty()) bio = ' '+getResources().getString(R.string.middle_dot)+' '+bio;
        mWiki.setText(mArtist.getSongCount() +" "+getResources().getString(R.string.songs)+bio);

        ArtistGlideRequest.Builder.from(GlideApp.with(getContext()), mArtist)
                .tryToLoadOriginal(true)
                .generateBuilder(getContext())
                .build()
            /*    .error(
                        ArtistGlideRequest
                                .Builder
                                .from(GlideApp.with(getContext()),mArtist)
                                .tryToLoadOriginal(false)
                                .generateBuilder(getContext())
                                .build())*/
                .thumbnail(
                        ArtistGlideRequest
                                .Builder
                                .from(GlideApp.with(getContext()),mArtist)
                                .tryToLoadOriginal(false)
                                .generateBuilder(getContext())
                                .build())
                .into(mBigImage);

                updateSongs();
    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {
        refreshData();
    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged();
    }

    @Override
    public void onPlayStateChanged() {
        mAdapter.notifyOnMediaStateChanged();
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

    private static class ArtistInfoTask extends AsyncTask<Void,Void,Void> {
        private WeakReference<ResultCallback> mCallback;
        ArtistInfoTask(ResultCallback callback) {
            mCallback = new WeakReference<>(callback);
        }

        void cancel() {
            cancel(true);
            mCallback.clear();
        }

        @Override
        protected  Void doInBackground(Void... voids) {

            return null;
        }
    }
}
