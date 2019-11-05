package com.odhiambopaul.musicr.glide.audiocover;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class AudioFileCoverLoader implements ModelLoader<AudioFileCover, InputStream> {

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull AudioFileCover audioFileCover, int width, int height, @NonNull Options options) {
        return new LoadData<InputStream>(new ObjectKey(audioFileCover.filePath),new AudioFileCoverFetcher(audioFileCover));
    }

    @Override
    public boolean handles(@NonNull AudioFileCover audioFileCover) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<AudioFileCover, InputStream> {

        @NonNull
        @Override
        public ModelLoader<AudioFileCover, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new AudioFileCoverLoader();
        }

        @Override
        public void teardown() {
        }
    }
}

