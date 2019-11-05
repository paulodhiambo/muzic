package com.odhiambopaul.musicr.ui.bottomnavigationtab.pager;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.glide.GlideApp;
import com.odhiambopaul.musicr.helper.menu.MediaMenuHelper;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.library.song.SongChildAdapter;
import com.odhiambopaul.musicr.ui.bottomsheet.OptionBottomSheet;

public class SongInArtistPagerAdapter extends SongChildAdapter {
    private static final String TAG = "SongInArtistPagerAdapter";

    public SongInArtistPagerAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0) return R.layout.item_sort_song_child;
        return R.layout.item_song_bigger;
    }

    @Override
    protected void onMenuItemClick(int positionInData) {
        OptionBottomSheet
                .newInstance(MediaMenuHelper.SONG_ARTIST_OPTION,getData().get(positionInData))
                .show(((AppCompatActivity)mContext).getSupportFragmentManager(), "song_popup_menu");
    }
}
