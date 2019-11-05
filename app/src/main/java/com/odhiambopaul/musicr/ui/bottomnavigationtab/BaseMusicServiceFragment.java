package com.odhiambopaul.musicr.ui.bottomnavigationtab;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.odhiambopaul.musicr.service.MusicServiceEventListener;
import com.odhiambopaul.musicr.ui.BaseActivity;

public abstract class BaseMusicServiceFragment extends Fragment implements MusicServiceEventListener {
    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if(activity instanceof BaseActivity) ((BaseActivity)activity).addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        Activity activity = getActivity();
        if(activity instanceof BaseActivity)((BaseActivity)activity).removeMusicServiceEventListener(this);
        super.onDestroyView();
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
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {

    }
}
