package com.odhiambopaul.musicr.ui.bottomnavigationtab.pager;

import com.odhiambopaul.musicr.addon.lastfm.rest.model.LastFmArtist;

import java.util.ArrayList;

public interface ResultCallback {
    void onSuccess(LastFmArtist lastFmArtist);
    void onFailure(Exception e);
    void onSuccess(ArrayList<String> mResult);
}