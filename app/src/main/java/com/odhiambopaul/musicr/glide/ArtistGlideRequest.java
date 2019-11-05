package com.odhiambopaul.musicr.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;


import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import com.odhiambopaul.musicr.App;
import com.odhiambopaul.musicr.glide.artistimage.ArtistImage;
import com.odhiambopaul.musicr.model.Artist;
import com.odhiambopaul.musicr.util.ArtistSignatureUtil;
import com.odhiambopaul.musicr.util.CustomArtistImageUtil;


public class ArtistGlideRequest {

    private static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.AUTOMATIC;
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;

    public static class Builder {
        final RequestManager requestManager;
        final Artist artist;
        boolean noCustomImage = false;
        boolean forceDownload;
        boolean mLoadOriginalImage = false;

        public static Builder from(@NonNull RequestManager requestManager, Artist artist) {
            return new Builder(requestManager, artist);
        }

        private Builder(@NonNull RequestManager requestManager, Artist artist) {
            this.requestManager = requestManager;
            this.artist = artist;
        }

        public PaletteBuilder generateBuilder(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public Builder noCustomImage(boolean noCustomImage) {
            this.noCustomImage = noCustomImage;
            return this;
        }

        public Builder forceDownload(boolean forceDownload) {
            this.forceDownload = forceDownload;
            return this;
        }
        public Builder tryToLoadOriginal(boolean b) {
            this.mLoadOriginalImage = b;
            return this;
        }

        public RequestBuilder<Bitmap> build() {
            return createBaseRequest(requestManager, artist, noCustomImage, forceDownload, mLoadOriginalImage)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .priority(Priority.LOW)
                    //.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(artist,mLoadOriginalImage));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequest(builder.requestManager, builder.artist, builder.noCustomImage, builder.forceDownload, builder.mLoadOriginalImage)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.artist,builder.mLoadOriginalImage));
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public RequestBuilder<Bitmap> build() {
            //noinspection unchecked
            return createBaseRequest(builder.requestManager, builder.artist, builder.noCustomImage, builder.forceDownload, builder.mLoadOriginalImage)
                    //.transcode(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)

                    .transition(GenericTransitionOptions.with(DEFAULT_ANIMATION))
                    .priority(Priority.LOW)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .signature(createSignature(builder.artist,builder.mLoadOriginalImage));
        }

    }

    public static RequestBuilder<Bitmap> createBaseRequest( RequestManager requestManager, Artist artist, boolean noCustomImage, boolean forceDownload, boolean loadOriginal) {
         RequestBuilder<Bitmap> builder;
        boolean hasCustomImage = CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist);
        if (noCustomImage || !hasCustomImage) {
            builder = requestManager.asBitmap().load(new ArtistImage(artist.getName(), forceDownload, loadOriginal));
        } else {
            builder =  requestManager.asBitmap().load(CustomArtistImageUtil.getFile(artist));
        }
        return builder;
    }

    public static Key createSignature(Artist artist, boolean isLoadOriginal) {
        return ArtistSignatureUtil.getInstance(App.getInstance()).getArtistSignature(artist.getName(), isLoadOriginal);
    }
}
