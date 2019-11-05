package com.odhiambopaul.musicr.helper.menu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.odhiambopaul.musicr.App;
import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.helper.songpreview.SongPreviewController;
import com.odhiambopaul.musicr.model.Playlist;
import com.odhiambopaul.musicr.model.Song;
import com.odhiambopaul.musicr.service.MusicPlayerRemote;
import com.odhiambopaul.musicr.ui.MainActivity;
import com.odhiambopaul.musicr.ui.bottomnavigationtab.pager.PlaylistPagerFragment;
import com.odhiambopaul.musicr.ui.dialog.AddToPlaylistDialog;
import com.odhiambopaul.musicr.ui.dialog.DeletePlaylistDialog;
import com.odhiambopaul.musicr.ui.dialog.RenamePlaylistDialog;
import com.odhiambopaul.musicr.ui.widget.WeakContextAsyncTask;
import com.odhiambopaul.musicr.util.PlaylistsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * modified by Le Dinh Trung (dtrung98)
 */
public class PlaylistMenuHelper {

    @StringRes
    public static final int[] AUTO_PLAYLIST_OPTION = new int[]{
            R.string.play_next,
            R.string.play_preview,
            R.string.add_to_queue,
            R.string.add_playlist_to_playlist,
    };
    @StringRes
    public static final int[] PLAYLIST_OPTION = new int[]{
            R.string.play_next,
            R.string.play_preview,
            R.string.add_to_queue,
            R.string.add_playlist_to_playlist,
            R.string.rename,
            R.string.divider,
            R.string.delete_from_playlist
    };

    public static boolean handleMenuClick(@NonNull AppCompatActivity activity, @NonNull Playlist playlist, int string_res_option) {
        switch (string_res_option) {
            case R.string.play_next:
                MusicPlayerRemote.playNext(new ArrayList<>(PlaylistPagerFragment.getPlaylistWithListId(activity, playlist,"")));
                return true;
            case R.string.play_preview:
                if(activity instanceof MainActivity) {
                    SongPreviewController preview =((MainActivity) activity).getSongPreviewController();
                    if(preview!=null) {
                        if (preview.isPlayingPreview())
                            preview.cancelPreview();
                        else {

                                ArrayList<Song> list = new ArrayList<>(PlaylistPagerFragment.getPlaylistWithListId(activity, playlist,""));
                                Collections.shuffle(list);
                                preview.previewSongs(list);

                        }
                    }
                }
                return true;
            case R.string.add_to_queue:
                MusicPlayerRemote.enqueue(new ArrayList<>(PlaylistPagerFragment.getPlaylistWithListId(activity, playlist,"")));
                return true;
            case R.string.add_playlist_to_playlist:
                AddToPlaylistDialog.create(new ArrayList<>(PlaylistPagerFragment.getPlaylistWithListId(activity, playlist,""))).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.string.rename:
                RenamePlaylistDialog.create(playlist.id).show(activity.getSupportFragmentManager(), "RENAME_PLAYLIST");
                return true;
            case R.string.delete_from_playlist:
                DeletePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                return true;
            case R.string.save_as:
                new SavePlaylistAsyncTask(activity).execute(playlist);
                return true;
        }
        return false;
    }


    private static class SavePlaylistAsyncTask extends WeakContextAsyncTask<Playlist, String, String> {
        public SavePlaylistAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackground(Playlist... params) {
            try {
                return String.format(App.getInstance().getApplicationContext().getString(R.string.saved_playlist_to), PlaylistsUtil.savePlaylist(App.getInstance().getApplicationContext(), params[0]));
            } catch (IOException e) {
                e.printStackTrace();
                return String.format(App.getInstance().getApplicationContext().getString(R.string.failed_to_save_playlist), e);
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        }
    }
}
