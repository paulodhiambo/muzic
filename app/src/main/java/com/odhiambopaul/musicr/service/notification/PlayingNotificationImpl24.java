package com.odhiambopaul.musicr.service.notification;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.glide.SongGlideRequest;
import com.odhiambopaul.musicr.model.Song;
import com.odhiambopaul.musicr.service.MusicService;
import com.odhiambopaul.musicr.ui.MainActivity;
import com.odhiambopaul.musicr.util.MusicUtil;
import com.odhiambopaul.musicr.util.PreferenceUtil;

import static com.odhiambopaul.musicr.service.MusicService.ACTION_REWIND;
import static com.odhiambopaul.musicr.service.MusicService.ACTION_SKIP;
import static com.odhiambopaul.musicr.service.MusicService.ACTION_TOGGLE_PAUSE;


public class PlayingNotificationImpl24 extends PlayingNotification {
    private static final String TAG = "NotificationImpl24";
    private int playButtonResId  = R.drawable.ic_pause_white;
    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = service.getCurrentSong();

        final boolean isPlaying = service.isPlaying();
        final String text = MusicUtil.getSongInfoString(song);


        Log.d(TAG, "update: isPlaying = "+ isPlaying+", playRes = "+ playButtonResId);
        playButtonResId = isPlaying
                ? R.drawable.ic_pause_white : R.drawable.ic_play_white;

        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, 0);

        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(MusicService.ACTION_QUIT);
        intent.setComponent(serviceName);
        final PendingIntent deleteIntent = PendingIntent.getService(service, 0, intent, 0);

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnUiThread(() ->
                SongGlideRequest.Builder.from(Glide.with(service), song)
                .checkIgnoreMediaStore(service)
                .generatePalette(service).build()
                .into(new SimpleTarget<Bitmap>(bigNotificationImageSize, bigNotificationImageSize) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        update(resource, Color.TRANSPARENT);
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        update(null, Color.TRANSPARENT);
                    }

                    void update(Bitmap bitmap, int color) {
                        if (bitmap == null)
                            bitmap = BitmapFactory.decodeResource(service.getResources(), R.drawable.default_album_art);

                        Log.d(TAG, "update after glide : playRes = "+ playButtonResId);
                        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                service.getString(R.string.action_play_pause),
                                retrievePlaybackAction(ACTION_TOGGLE_PAUSE));
                        NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
                                service.getString(R.string.action_previous),
                                retrievePlaybackAction(ACTION_REWIND));
                        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
                                service.getString(R.string.action_next),
                                retrievePlaybackAction(ACTION_SKIP));
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(bitmap)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(song.title)
                                .setContentText(text)
                                .setOngoing(isPlaying)
                                .setShowWhen(false)
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder.setStyle(new MediaStyle().setMediaSession(service.getMediaSession().getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && PreferenceUtil.getInstance(service).coloredNotification())
                                builder.setColor(color);
                        }

                        if (stopped)
                            return; // notification has been stopped before loading was finished
                        updateNotifyModeAndPostNotification(builder.build());
                    }
                }));
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(service, 0, intent, 0);
    }
}
