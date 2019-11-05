package com.odhiambopaul.musicr.helper.songpreview;

public interface SongPreviewListener {
        void onSongPreviewStart(PreviewSong song);
        void onSongPreviewFinish(PreviewSong song);
    }