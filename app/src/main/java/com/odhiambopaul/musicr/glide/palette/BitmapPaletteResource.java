package com.odhiambopaul.musicr.glide.palette;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.Util;


public class BitmapPaletteResource implements Resource<BitmapPaletteWrapper> {

    private final BitmapPaletteWrapper bitmapPaletteWrapper;

    public BitmapPaletteResource(BitmapPaletteWrapper bitmapPaletteWrapper) {
        this.bitmapPaletteWrapper = bitmapPaletteWrapper;
    }

    @NonNull
    @Override
    public Class<BitmapPaletteWrapper> getResourceClass() {
        return BitmapPaletteWrapper.class;
    }

    @Override
    public BitmapPaletteWrapper get() {
        return bitmapPaletteWrapper;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(bitmapPaletteWrapper.getBitmap());
    }

    @Override
    public void recycle() {
        bitmapPaletteWrapper.getBitmap().recycle();
    }
}
