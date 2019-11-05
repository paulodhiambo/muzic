package com.odhiambopaul.musicr.ui.bottomsheet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.model.Song;
import com.odhiambopaul.musicr.service.MusicPlayerRemote;
import com.odhiambopaul.musicr.service.MusicServiceEventListener;
import com.odhiambopaul.musicr.ui.BaseActivity;
import com.odhiambopaul.musicr.ui.dialog.WriteTagDialog;
import com.odhiambopaul.musicr.util.MusicUtil;
import com.odhiambopaul.musicr.util.Tool;
import com.odhiambopaul.musicr.util.Util;
import com.squareup.picasso.Picasso;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;

public class LyricBottomSheet extends BottomSheetDialogFragment  implements MusicServiceEventListener, WriteTagDialog.WriteTagResultListener {
    private static final String TAG = "LyricBottomSheet";
    private static final String SONG_KEY = "song";
    private static final String SHOULD_AUTO_UPDATE_KEY = "should_auto_update";
    @BindView(R.id.lyric_content)
    TextView mLyricContent;
    @BindView(R.id.root) View mRoot;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView mScrollView;

    @BindView(R.id.align_view) View mAlignView;

    @BindView(R.id.edit) View mEdit;

    String mLyricString = "";

    @BindView(R.id.menu) ImageView mMenuButton;

    @OnClick(R.id.menu)
    void playOrPause() {
        MusicPlayerRemote.playOrPause();
    }

    void updateMenuButton() {
        boolean isPlaying = MusicPlayerRemote.isPlaying();
        if(isPlaying) mMenuButton.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
        else mMenuButton.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
    }

    @OnClick({R.id.back,R.id.parent})
    void back() {
        dismiss();
    }

    public static LyricBottomSheet newInstance(Song song) {

        Bundle args = new Bundle();
        args.putParcelable(SONG_KEY, song);
        args.putBoolean(SHOULD_AUTO_UPDATE_KEY,false);

        LyricBottomSheet fragment = new LyricBottomSheet();
        fragment.setArguments(args);

        return fragment;
    }
    private boolean mShouldAutoUpdate = false;

/*    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }*/

    public static LyricBottomSheet newInstance() {

        Bundle args = new Bundle();
        args.putParcelable(SONG_KEY, MusicPlayerRemote.getCurrentSong());
        args.putBoolean(SHOULD_AUTO_UPDATE_KEY,true);
        LyricBottomSheet fragment = new LyricBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }
    Song mSong;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lyric_bottom_sheet,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        ButterKnife.bind(this,view);
        mAlignView.getLayoutParams ().height = (Tool.getScreenSize(getContext())[1]);
        mAlignView.requestLayout();

        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

            Window window = dialog.getWindow();

            if(window!=null)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            FrameLayout bottomSheet = dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(-Tool.getNavigationHeight(requireActivity()));
            behavior.setHideable(false);
            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if(newState==STATE_COLLAPSED)
                        LyricBottomSheet.this.dismiss();
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        });

        // onViewCreated();
        Bundle bundle = getArguments();
        if(bundle !=null) {
            mSong = bundle.getParcelable(SONG_KEY);
            mShouldAutoUpdate = bundle.getBoolean(SHOULD_AUTO_UPDATE_KEY,false);
        }
        updateLyric();
        updateMenuButton();
        if(getActivity() instanceof BaseActivity)
            ((BaseActivity)getActivity()).addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        if(getActivity() instanceof BaseActivity)
            ((BaseActivity)getActivity()).removeMusicServiceEventListener(this);
        super.onDestroyView();
    }

    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.description) TextView mDescription;
    @BindView(R.id.image)
    ImageView mImageView;
    private void updateLyric() {
        if(mSong !=null) {
            mLyricString = MusicUtil.getLyrics(mSong);
            if(mLyricString==null||mLyricString.isEmpty())
                mLyricString = "This song has no lyric.";

            boolean isHtml = isHtml(mLyricString);
            if(isHtml) {
                Spanned spanned = Html.fromHtml(mLyricString);
                mLyricContent.setText(spanned);
            } else
                mLyricContent.setText(mLyricString);

            Log.d(TAG, "updateLyric: "+mLyricString);
            mTitle.setText(mSong.title);
            mDescription.setText(mSong.artistName);
            if(getContext() !=null)
                Glide.with(getContext()).load(MusicUtil.getMediaStoreAlbumCoverUri(mSong.albumId)).placeholder(R.drawable.music_style).error(R.drawable.music_style).into(mImageView);
            else Picasso.get().load(MusicUtil.getMediaStoreAlbumCoverUri(mSong.albumId)).placeholder(R.drawable.music_style).error(R.drawable.music_style).into(mImageView);
        }
        else Log.d(TAG, "updateLyric: Song is null");
    }
    private void setClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    @OnClick(R.id.copy)
    void copy() {
        if(getContext() !=null) {
            setClipboard(getContext(), MusicUtil.getLyrics(mSong));
            Toast.makeText(getContext(),"Copied",Toast.LENGTH_SHORT).show();
        }
    }
    @BindView(R.id.lyric_constraint_root) View mLyricConstraintRoot;
    @BindView(R.id.edit_constraint_root) View mEditConstraintRoot;
    @BindView(R.id.edit_text) EditText mEditText;

    private Song mEditingSong;
    @OnClick(R.id.edit)
    void edit() {
        mEditingSong = mSong;
        mEditText.setText(mLyricString);
        mLyricConstraintRoot.setVisibility(View.INVISIBLE);
        mEditConstraintRoot.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.save)
    void saveLyric() {
        if(getActivity()!=null)
            Util.hideSoftKeyboard(getActivity());

        if(mEditingSong !=null) {
            ArrayList<String> path = new ArrayList<>(1);
            path.add(mEditingSong.data);

            Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
            /*fieldKeyValueMap.put(FieldKey.TITLE, songTitle.getText().toString());
            fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
            fieldKeyValueMap.put(FieldKey.ARTIST, artist.getText().toString());
            fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
            fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
            fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());*/
            fieldKeyValueMap.put(FieldKey.LYRICS, mEditText.getText().toString());
            writeValuesToFiles(path, fieldKeyValueMap, null);
        }
    }

    protected void writeValuesToFiles(List<String> path, final Map<FieldKey, String> fieldKeyValueMap, @Nullable final WriteTagDialog.ArtworkInfo artworkInfo) {
        Util.hideSoftKeyboard(getActivity());
        WriteTagDialog.LoadingInfo loadingInfo = new WriteTagDialog.LoadingInfo(path, fieldKeyValueMap, artworkInfo);
        WriteTagDialog.newInstance(loadingInfo).setWritingTagResultListener(this).show(getChildFragmentManager(),WriteTagDialog.TAG);
    }

    @OnClick(R.id.cancel)
    void cancelEditLyric() {
        mEditText.clearFocus();
        if(getActivity()!=null)
            Util.hideSoftKeyboard(getActivity());
        mEditConstraintRoot.setVisibility(View.INVISIBLE);
        mLyricConstraintRoot.setVisibility(View.VISIBLE);
    }

    // adapted from re posted by Phil Haack and modified to match better
    public final static String tagStart=
            "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>";
    public final static String tagEnd=
            "\\</\\w+\\>";
    public final static String tagSelfClosing=
            "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>";
    public final static String htmlEntity=
            "&[a-zA-Z][a-zA-Z0-9]+;";
    public final static Pattern htmlPattern=Pattern.compile(
            "("+tagStart+".*"+tagEnd+")|("+tagSelfClosing+")|("+htmlEntity+")",
            Pattern.DOTALL
    );
    public static boolean isHtml(String s) {
        boolean ret=false;
        if (s != null) {
            ret=htmlPattern.matcher(s).find();
        }
        return ret;
    }

    @Override
    public void onServiceConnected() {
        if(mShouldAutoUpdate) autoUpdateLyric();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {
        if(mShouldAutoUpdate) autoUpdateLyric();
    }
    public void autoUpdateLyric() {
        mSong = MusicPlayerRemote.getCurrentSong();
        updateLyric();
    }

    @Override
    public void onPlayStateChanged() {
        updateMenuButton();
    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {
        Log.d(TAG, "onMediaStoreChanged");
    }

    @Override
    public void onWritingTagFinish(boolean result) {
        if(getActivity()!=null)
            Util.hideSoftKeyboard(getActivity());

        mEditConstraintRoot.setVisibility(View.INVISIBLE);
        mLyricConstraintRoot.setVisibility(View.VISIBLE);

        updateLyric();
    }
}
