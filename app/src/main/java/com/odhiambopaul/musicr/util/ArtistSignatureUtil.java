package com.odhiambopaul.musicr.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.signature.ObjectKey;
import com.odhiambopaul.musicr.App;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistSignatureUtil {
    private static final String TAG = "ArtistSignatureUtil";

    private static final String ARTIST_SIGNATURE_PREFS = "artist_signatures";

    private static ArtistSignatureUtil sInstance;

    private final SharedPreferences mPreferences;

    private ArtistSignatureUtil(@NonNull final Context context) {
        mPreferences = context.getSharedPreferences(ARTIST_SIGNATURE_PREFS, Context.MODE_PRIVATE);
    }

    public static ArtistSignatureUtil getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new ArtistSignatureUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public static ArtistSignatureUtil getInstance() {
        if (sInstance == null) {
            sInstance = new ArtistSignatureUtil(App.getInstance().getApplicationContext());
        }
        return sInstance;
    }

    @SuppressLint("CommitPrefEdits")
    public void updateArtistSignature(String artistName) {
        mPreferences.edit().putLong(artistName, System.currentTimeMillis()).commit();
    }

    public long getArtistSignatureRaw(String artistName) {
        return mPreferences.getLong(artistName, 0);
    }

    public ObjectKey getArtistSignature(String artistName, boolean isLoadOriginal) {
        String value = String.valueOf(getArtistSignatureRaw(artistName));
        Log.d(TAG, "getArtistSignature: "+value);
        return new ObjectKey(artistName+"_"+"original="+isLoadOriginal+"_"+String.valueOf(getArtistSignatureRaw(artistName)));
    }
}
