package com.odhiambopaul.musicr.ui.nowplaying;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.motion.MotionLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.helper.menu.MediaMenuHelper;
import com.odhiambopaul.musicr.loader.SongLoader;
import com.odhiambopaul.musicr.service.MusicPlayerRemote;
import com.odhiambopaul.musicr.service.MusicServiceEventListener;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.BaseLayerFragment;

import com.odhiambopaul.musicr.model.Song;

import com.odhiambopaul.musicr.ui.BaseActivity;
import com.odhiambopaul.musicr.ui.LayerController;

import com.odhiambopaul.musicr.ui.MainActivity;
import com.odhiambopaul.musicr.ui.bottomsheet.OptionBottomSheet;
import com.odhiambopaul.musicr.ui.widget.view.AudioVisualSeekBar;
import com.odhiambopaul.musicr.util.BitmapEditor;
import com.odhiambopaul.musicr.util.SortOrder;
import com.odhiambopaul.musicr.util.Tool;
import com.odhiambopaul.musicr.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.odhiambopaul.musicr.util.BitmapEditor.updateSat;

public class NowPlayingController extends BaseLayerFragment implements MusicServiceEventListener, AudioVisualSeekBar.OnSeekBarChangeListener, ColorPickerAdapter.OnColorChangedListener {
    private static final String TAG ="NowPlayingController";
    @BindView(R.id.root) CardView mRoot;
    @BindView(R.id.dim_view) View mDimView;
    private float mMaxRadius= 18;

    @BindView(R.id.minimize_bar) View mMinimizeBar;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.visual_seek_bar)
    AudioVisualSeekBar mVisualSeekBar;
    @BindView(R.id.time_text_view) TextView mTimeTextView;
    @BindView(R.id.big_title) TextView mBigTitle;
    @BindView(R.id.big_artist) TextView mBigArtist;
    private NowPlayingAdapter mAdapter;
    @BindView(R.id.color_picker_recycler_view) RecyclerView mColorPickerRecyclerView;
    private ColorPickerAdapter mColorPickerAdapter;

    @OnClick(R.id.menu_button)
    void more() {
        if(getActivity() !=null)
            OptionBottomSheet
                    .newInstance(MediaMenuHelper.NOW_PLAYING_OPTION,MusicPlayerRemote.getCurrentSong())
                    .show(getActivity().getSupportFragmentManager(), "song_popup_menu");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return inflater.inflate(R.layout.now_playing_controller,container,false);
    }

    SnapHelper snapHelper = new PagerSnapHelper();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mMaxRadius = getResources().getDimension(R.dimen.max_radius_layer);
        mTitle.setSelected(true);

        mAdapter = new NowPlayingAdapter(getActivity());
      //  mRecyclerView.setPageTransformer(false, new SliderTransformer());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));

        snapHelper.attachToRecyclerView(mRecyclerView);

        mColorPickerAdapter = new ColorPickerAdapter(this);
        mColorPickerRecyclerView.setAdapter(mColorPickerAdapter);
        mColorPickerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));


        mRecyclerView.setOnTouchListener((v, event) -> mLayerController.streamOnTouchEvent(mRoot,event));
        mVisualSeekBar.setOnTouchListener((v, event) -> mLayerController.streamOnTouchEvent(mRoot, event) &&  event.getAction()!=MotionEvent.ACTION_DOWN);

        mVisualSeekBar.setOnSeekBarChangeListener(this);
        Log.d(TAG, "onViewCreated");
       if(getActivity() instanceof BaseActivity) ((MainActivity)getActivity()).addMusicServiceEventListener(this,true);
       setUp();
    }

    @Override
    public void onDestroyView() {
        if(mThemeGeneratorTask !=null) mThemeGeneratorTask.cancel();
        if(getActivity() instanceof BaseActivity) ((MainActivity)getActivity()).removeMusicServiceEventListener(this);
        super.onDestroyView();
    }

    /*
     *
     *  Implement MusicServiceEventListener
     *
     */

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
      setUp();
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "onServiceDisconnected");
    }

    @Override
    public void onQueueChanged() {
        Log.d(TAG, "onQueueChanged");
        updateQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        Log.d(TAG, "onPlayingMetaChanged");
        updatePlayingSongInfo();
        updateQueuePosition();
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
        updatePlayPauseState();
        mVisualSeekBar.postDelayed(mUpdateProgress,10);
    }

    @Override
    public void onRepeatModeChanged() {
        Log.d(TAG, "onRepeatModeChanged");
        /*
        Unused
         */
    }

    @Override
    public void onShuffleModeChanged() {
        Log.d(TAG, "onShuffleModeChanged");
        /*
        Unused
         */
    }

    @Override
    public void onMediaStoreChanged() {
        Log.d(TAG, "onMediaStoreChanged");
        updateQueue();
    }

    /*
     *
     *   End of Implementing MusicServiceEventListener
    *
    */

    private void updateQueue() {
        mAdapter.setData(MusicPlayerRemote.getPlayingQueue());
    }

    private void updateQueuePosition() {
        try {
            int pos = MusicPlayerRemote.getPosition();
            if(pos>=0)
            mRecyclerView.smoothScrollToPosition(MusicPlayerRemote.getPosition());
        } catch (Exception ignore) {}
    }
    public void setUp() {
        updatePlayingSongInfo();
        updatePlayPauseState();
        updateQueue();
        updateQueuePosition();
    }

    private void setRadius(float value) {
        if(mRoot!=null) {
            float valueTemp;
            if(value>1) valueTemp=1;
            else if(value<=0.1f) valueTemp = 0;
            else valueTemp = value;
            mRoot.setRadius(mMaxRadius * valueTemp);
        }
    }

    @Override
    public void onUpdateLayer( ArrayList<LayerController.Attr> attrs, ArrayList<Integer> actives, int me) {

        if(mRoot==null) return;
        if(me ==1) {
            mDimView.setAlpha(0.3f*(attrs.get(actives.get(0)).getRuntimePercent()));
          //  mRoot.setRoundNumber( attrs.get(actives.get(0)).getRuntimePercent(),true);
            setRadius( attrs.get(actives.get(0)).getRuntimePercent());
        } else
        {
            //  other, active_i >1
            // min = 0.3
            // max = 0.45
            float min = 0.3f, max =0.65f;
            float hieu = max - min;
            float heSo_sau = (me-1.0f)/(me-0.75f); // 1/2, 2/3,3/4, 4/5, 5/6 ...
            float heSo_truoc =  (me-2.0f)/(me-0.75f); // 0/1, 1/2, 2/3, ...
            float darken = min + hieu*heSo_truoc + hieu*(heSo_sau - heSo_truoc)*attrs.get(actives.get(0)).getRuntimePercent();
            // Log.d(TAG, "darken = " + darken);
            mDimView.setAlpha(darken);
            setRadius(1);
        }
      //  checkStatusStyle();
    }

    @Override
    public void onTranslateChanged(LayerController.Attr attr) {
        //Log.d(TAG, "onTranslateChanged : pc = "+attr.getRuntimePercent()+", recycler_width = "+mRecyclerView.getWidth());
        if(getMaxPositionType())
        setRadius(0);
        else setRadius(attr.getRuntimePercent());

        mConstraintRoot.setProgress(attr.getRuntimePercent());
        // sync time text view
        if(mConstraintRoot.getProgress()!=0&&!mTimeTextIsSync) {
            mTimeTextView.setText(timeTextViewTemp);
            Log.d(TAG, "onTranslateChanged: timeTextView : "+timeTextViewTemp);
        }
        if(mConstraintRoot.getProgress()==0||mConstraintRoot.getProgress()==1)
            try {
                mRecyclerView.scrollToPosition(MusicPlayerRemote.getPosition());
            } catch (Exception ignore) {}
        //checkStatusStyle();
    }
    public void checkStatusStyle(){
        if(mConstraintRoot.getProgress()>=0.9&&mDimView.getAlpha()<=0.1
        ) {
            if(getActivity() instanceof MainActivity)
            ((MainActivity)getActivity()).setTheme(true);
        } else {
            if(getActivity() instanceof MainActivity)
            ((MainActivity)getActivity()).setTheme(false);
        }
    }

    @Override
    public int minPosition(Context context,int h) {
        return (int) (context.getResources().getDimension(R.dimen.bottom_navigation_height)+ context.getResources().getDimension(R.dimen.now_laying_height_in_minimize_mode));
    }

    @Override
    public String tag() {
        return TAG;
    }

    @OnClick({R.id.play_pause_button,R.id.button_right})
    void playOrPause() {
        MusicPlayerRemote.playOrPause();
/*        Handler handler = new Handler();
        handler.postDelayed(MusicPlayerRemote::playOrPause,50);*/
    }
    @OnClick(R.id.prev_button)
    void goToPrevSong() {
        MusicPlayerRemote.back();
   /*     Handler handler = new Handler();
        handler.postDelayed(MusicPlayerRemote::back,50);*/
    }
    @OnClick(R.id.next_button)
    void goToNextSong() {
        MusicPlayerRemote.playNextSong();
/*        Handler handler = new Handler();
        handler.postDelayed(MusicPlayer::next,100);*/
    }
    void updatePlayPauseState(){
        if(MusicPlayerRemote.isPlaying()) {
            mButtonRight.setImageResource(R.drawable.ic_pause_black_24dp);
            mPlayPauseButton.setImageResource(R.drawable.pause_round);
        } else {
            mButtonRight.setImageResource(R.drawable.ic_play_white_36dp);
            mPlayPauseButton.setImageResource(R.drawable.play_round);
        }
    }

    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.playlist_title) TextView mPlaylistTitle;
    @BindView(R.id.button_right)
    ImageView mButtonRight;
    @BindView(R.id.prev_button) ImageView mPrevButton;
    @BindView(R.id.next_button) ImageView mNextButton;

    @BindView(R.id.play_pause_button)
    ImageView mPlayPauseButton;
    PaletteGeneratorTask mThemeGeneratorTask;

    @OnClick(R.id.playlist_title)
    void popUpPlayingList() {
        Activity activity = getActivity();
        if(activity instanceof MainActivity) {
            ((MainActivity)getActivity()).popUpPlaylistTab();
        }
    }
    private void updatePlayingSongInfo() {
       Song song = MusicPlayerRemote.getCurrentSong();
       if(song==null||song.id==-1) {
           ArrayList<Song> list = SongLoader.getAllSongs(mPlayPauseButton.getContext(), SortOrder.SongSortOrder.SONG_DATE);
           if(list.isEmpty()) return;
           MusicPlayerRemote.openQueue(list,0,false);
           return;
       }
        mTitle.setText(String.format("%s %s %s", song.title, getString(R.string.middle_dot), song.artistName));
        mBigTitle.setText(song.title);
        mBigArtist.setText(song.artistName);

        String path = song.data;
        long duration = song.duration;
        if(duration>0&&path!=null&&!path.isEmpty()&&!mVisualSeekBar.getCurrentFileName().equals(path)) {
            Log.d(TAG, "start visualize "+ path +"dur = "+ duration+", pos = "+ MusicPlayerRemote.getSongProgressMillis());
            mVisualSeekBar.visualize(path, duration, MusicPlayerRemote.getSongProgressMillis());
        } else {
            Log.d(TAG, "ignore visualize "+path);
        }

        mVisualSeekBar.postDelayed(mUpdateProgress,10);

        if(mThemeGeneratorTask !=null) mThemeGeneratorTask.cancel();
        mThemeGeneratorTask = new PaletteGeneratorTask(this);
        mThemeGeneratorTask.execute();
    }

  /*  @Override
    public void onMetaChanged() {
        mTitle.setText(String.format("%s %s %s", MusicPlayer.getTrackName(), getString(R.string.middle_dot), MusicPlayer.getArtistName()));
        mBigTitle.setText(MusicPlayer.getTrackName());
        mBigArtist.setText(MusicPlayer.getArtistName());

      if(mThemeGeneratorTask!=null) mThemeGeneratorTask.cancel();
      mThemeGeneratorTask = new PaletteGeneratorTask(this);
      mThemeGeneratorTask.execute();
    }*/

   /*
        private void onQueueReady(List<Song> songs2) {
        long start = System.currentTimeMillis();

        long time3 = System.currentTimeMillis() - start;
        long time4 = System.currentTimeMillis() - start;

        if(!songs2.isEmpty()) {
            mAdapter.setData(songs2);
            mRecyclerView.smoothScrollToPosition(MusicPlayer.getQueuePosition());

            if(getActivity() instanceof MainActivity)
                ((MainActivity)getActivity()).setDataForPlayingQueue(songs2);
        }
        long time5 = System.currentTimeMillis() - start;

        String path = MusicPlayer.getPath();
        long duration = MusicPlayer.duration();
        if(duration>0&&path!=null&&!path.isEmpty()&&!mVisualSeekBar.getCurrentFileName().equals(path))
            mVisualSeekBar.visualize(path,duration, (int) MusicPlayer.position());
        mVisualSeekBar.postDelayed(mUpdateProgress,10);
        // Log.d(TAG, "onMetaChanged: time1 = "+time1+", time2 = "+time2+", time3 = "+time3+", time4 = "+time4+", time5 = "+time5);
        Log.d(TAG, "onMetaChanged: time3 = "+time3+", time4 = "+time4+", time5 = "+time5);
        updatePlayPauseState();
    }
    */
    private void onColorPaletteReady(int color1, int color2, float alpha1, float alpha2) {
        Log.d(TAG, "onColorPaletteReady :"+color1+", "+color2+", "+alpha1+", "+alpha2);
        mPlayPauseButton.setColorFilter(Tool.getBaseColor());
        mPrevButton.setColorFilter(color2);
        mNextButton.setColorFilter(color2);

      //  mTimeTextView.setTextColor(color1);
     //   (mTimeTextView.getBackground()).setColorFilter(color1, PorterDuff.Mode.SRC_IN);

        mBigTitle.setTextColor(Tool.lighter(color1,0.5f));
       // mBigArtist.setAlpha(alpha2);
        mBigArtist.setTextColor(color2);
        mVisualSeekBar.updateProperties();
        if(getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).setPlaylistColorPalette(color1,color2,alpha1,alpha2);
        }
    }

    @BindView(R.id.constraint_root)
    MotionLayout mConstraintRoot;
/*
   private void addAnimationOperations() {
        final boolean[] set = {false};
        ConstraintSet constraint1 = new ConstraintSet();
        constraint1.clone(mConstraintRoot);

        ConstraintSet constraint2 = new ConstraintSet();
        constraint2.clone(getContext(),R.layout.now_playing_controller_alt);

        mButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(mConstraintRoot);
                ConstraintSet constraintSet = (set[0]) ? constraint1 : constraint2;
                constraintSet.applyTo(mConstraintRoot);
                set[0] =!set[0];
            }
        });

    }
    */

    @Override
    public boolean onGestureDetected(int gesture) {
        if(gesture==LayerController.SINGLE_TAP_UP) {
            LayerController.Attr a = getLayerController().getMyAttr(this);
            if(a!=null) {
                if(a.getState()== LayerController.Attr.MINIMIZED)
                    a.animateToMax();
                else
                    a.animateToMin();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getMaxPositionType() {
        return true;
    }

    @Override
    public void onColorChanged(int position, int newColor) {
        mBigTitle.setTextColor(newColor);
    }


    private class CustomDecoration extends RecyclerView.ItemDecoration {
        public CustomDecoration() {
            super();
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        fragmentPaused = false;
        if(mVisualSeekBar!=null) {
            mVisualSeekBar.postDelayed(mUpdateProgress,10);
        }
    }

    ////////////////////////////////
    /// VISUAL SEEK BAR IMPLEMENTED
    ////////////////////////////////

    private boolean isTouchedVisualSeekbar = false;
    private int overflowcounter=0;
    boolean fragmentPaused = false;
    // seekbar
    public Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long position = MusicPlayerRemote.getSongProgressMillis();

            if(!isTouchedVisualSeekbar)
                setTextTime(position,MusicPlayerRemote.getSongDurationMillis());

            if(mVisualSeekBar!=null) {
                mVisualSeekBar.setProgress((int) position);
                //TODO: Set elapsedTime
            }
            overflowcounter--;
            if(MusicPlayerRemote.isPlaying()) {
                //TODO: ???
                int delay = (int) (150 -(position)%100);
                if(overflowcounter<0 && !fragmentPaused) {
                    overflowcounter ++;
                    mVisualSeekBar.postDelayed(mUpdateProgress,delay);
                }
            }
        }
    };
    @Override
    public void onSeekBarSeekTo(AudioVisualSeekBar seekBar, int i, boolean b) {
        if(b) MusicPlayerRemote.seekTo(i);
    }

    @Override
    public void onSeekBarTouchDown(AudioVisualSeekBar seekBar) {
        isTouchedVisualSeekbar = true;
    }

    @Override
    public void onSeekBarTouchUp(AudioVisualSeekBar seekBar) {
        isTouchedVisualSeekbar = false;
    }

    @Override
    public void onSeekBarSeeking(int seekingValue) {
        setTextTime(seekingValue,MusicPlayerRemote.getSongDurationMillis());
    }
    private void setTextTime(long pos, long duration) {
        int minute = (int) (pos/1000/60);
        int second = (int) (pos/1000-  minute*60);
        int dur_minute = (int) (duration/1000/60);
        int dur_second = (int) (duration/1000 - dur_minute*60);

        String text ="";
        if(minute<10) text+="0";
        text+=minute+":";
        if(second<10) text+="0";
        text+= second+" | ";
        if(dur_minute<10) text+="0";
        text+= dur_minute+":";
        if(dur_second<10) text+="0";
        text+=dur_second;
        if(mConstraintRoot.getProgress()!=0) {
            mTimeTextView.setText(text);
            Log.d(TAG, "setTextTime: "+text);
            mTimeTextIsSync = true;
        }
        else {
            mTimeTextIsSync = false;
            timeTextViewTemp = text;
        }
    }

    private boolean mTimeTextIsSync = false;

    private String timeTextViewTemp = "00:00";

    Handler mHandler = new Handler();

    private static class PaletteGeneratorTask extends AsyncTask<Void,Void,Boolean> {
        private NowPlayingController mFragment;
        PaletteGeneratorTask(NowPlayingController fragment) {
            mFragment = fragment;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean c;
            long start = System.currentTimeMillis();
            try {
                c = getColor();
            } catch (Exception ignore) {
                c = false;
            }
            long time1 = System.currentTimeMillis() - start;


            Log.d(TAG, "doInBackground: time1 = "+time1);

            return c;
        }
      /*  private boolean getQueue() {
            List<Song> songs2;
            if(mFragment!=null)
                songs2 =  QueueLoader.getQueueSongs(mFragment.getContext());
            else return false;
            if(songs2==null||songs2.isEmpty()) return false;
            if(mFragment!=null&&mFragment.getActivity()!=null)
                mFragment.getActivity().runOnUiThread(() -> {
                    if(mFragment!=null)
                        mFragment.onQueueReady(songs2);
                });
            else return false;
            return true;
        }*/
        private boolean getColor() {
            Bitmap bitmap;
            try {
                bitmap = Picasso.get().load(Util.getAlbumArtUri(MusicPlayerRemote.getCurrentSong().albumId)).error(R.drawable.speaker2).get();
            } catch (Exception e) {
                bitmap = ((BitmapDrawable)mFragment.getResources().getDrawable(R.drawable.speaker2)).getBitmap();
            }
            if(bitmap==null) return false;
            int color = getMostColor(bitmap);
            Tool.setMostCommonColor(color);
            Tool.setSurfaceColor(color);

            Palette palette = Palette.from(bitmap).generate();
            return !onGeneratedPalette(palette);
        }

        private boolean onGeneratedPalette(@NonNull Palette p) {
            int[] palette = new int[6];
            // access palette colors here
            Palette.Swatch psVibrant = p.getVibrantSwatch();
            Palette.Swatch psVibrantLight = p.getLightVibrantSwatch();
            Palette.Swatch psVibrantDark = p.getDarkVibrantSwatch();
            Palette.Swatch psMuted = p.getMutedSwatch();
            Palette.Swatch psMutedLight = p.getLightMutedSwatch();
            Palette.Swatch psMutedDark = p.getDarkMutedSwatch();

            for (int i = 0; i < 6; i++)
                palette[i] = 0;
            if (psVibrant != null) {
                palette[0] = psVibrant.getRgb();
            }
            if (psVibrantLight != null) {
                palette[1] = psVibrantLight.getRgb();
            }
            if (psVibrantDark != null) {
                palette[2] = psVibrantDark.getRgb();
            }
            if (psMuted != null) {
                palette[3] = psMuted.getRgb();
            }
            if (psMutedLight != null) {
                palette[4] = psMutedLight.getRgb();
            }
            if (psMutedDark != null) {
                palette[5] = psMutedDark.getRgb();
            }



            float[] hsv = new float[3];
            Color.colorToHSV(Tool.getMostCommonColor(), hsv);
            //     Log.d(hsv[0] + "|" + hsv[1] + "|" + hsv[2], "ColorMe");
            float alpha_7basic = hsv[1];

           final int color1,color2;
            float alpha1,alpha2;

            if(alpha_7basic<0.5f) //  Đủ đậm thì màu mostCommon sẽ là màu song name, màu basic là màu artist
            {
                color1= Tool.getMostCommonColor();
                alpha1 =1;
                color2 = Tool.getBaseColor();
                alpha2 =alpha_7basic;
            }
            else // ngược lại thì màu basic sẽ là màu song name
            {
                int tempColor1 = getBestColorFromPalette(palette);
                if(tempColor1==0) color1 = Tool.getBaseColor();
                else color1 = tempColor1;
                alpha1 = 1;
                color2 =Color.WHITE;
                alpha2 = 0.7f;
            }
            if(mFragment!=null&&mFragment.getActivity()!=null)
            mFragment.getActivity().runOnUiThread(() -> {
                if(mFragment!=null) {
                    mFragment.onColorPaletteReady(color1, color2, alpha1, alpha2);
                    if(mFragment!=null) {
                        mFragment.mColorPickerAdapter.setData(color1, color2);
                        mFragment.mColorPickerAdapter.addData(palette[0], palette[1], palette[2], palette[3], palette[4], palette[5]);
                    }
                }
            });
            else return false;
            return true;
        }
        private int getBestColorFromPalette(int[] palette) {
            int c = 0;
            float[] hsv = new float[3];
            int[] list = new int[]{palette[2], palette[0], palette[5], palette[1], palette[3], palette[4]};
            // theo thứ tự : 2 - 0 - 5 -1 - 3 - 4
            for (int i = 0; i < 6; i++) {
                Color.colorToHSV(list[i], hsv);
                if(hsv[1]>=0.5f) {
                    c = list[i];
                    return c;
                }
            }
            return 0;
        }
        private int getMostColor(Bitmap origin) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 38;
            Bitmap sample;
                sample = BitmapEditor.getResizedBitmap(origin,origin.getHeight()/38,origin.getWidth()/38);

            sample = updateSat(sample, 4);
            sample = BitmapEditor.fastblur(sample, 1, 4);
            int[] averageColorRGB = BitmapEditor.getAverageColorRGB(sample);
           // black_theme = BitmapEditor.PerceivedBrightness(95, averageColorRGB);

          int  mColor24Bit = Color.argb(255,averageColorRGB[0],averageColorRGB[1],averageColorRGB[2]) ;


           Bitmap mBlurArtWork = sample;
           return mColor24Bit;
        }
        public void cancel() {
            cancel(true);
            if(mFragment!=null) {
                mFragment.mThemeGeneratorTask = null;
                mFragment = null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }

    }
}
