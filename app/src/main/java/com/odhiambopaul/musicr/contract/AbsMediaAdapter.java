package com.odhiambopaul.musicr.contract;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.odhiambopaul.musicr.model.Media;
import com.odhiambopaul.musicr.util.Tool;

/**
 * Implements these features:
 * <br> Menu button click
 * <br> Long press
 */
public abstract class AbsMediaAdapter<VH extends AbsBindAbleHolder, I extends Media> extends AbsDataAdapter<VH, I> {
    private static final String TAG = "AbsMediaAdapter";

    protected Context mContext;
    protected int mMediaPlayDataItem = -1;

    public AbsMediaAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public void destroy() {
        mContext = null;
        super.destroy();
    }

    protected abstract void onMenuItemClick(final int positionInData);

    protected boolean onLongPressedItem(AbsBindAbleHolder holder, final int position) {
        Tool.vibrate(mContext);
        return true;
    }

    public void notifyOnMediaStateChanged() {
    }

    boolean isMediaPlayItemAvailable() {
        return -1 < mMediaPlayDataItem && mMediaPlayDataItem < getData().size();
    }

    public class AbsMediaHolder<I extends Media> extends AbsBindAbleHolder<I> implements View.OnClickListener, View.OnLongClickListener {
        public AbsMediaHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public boolean onLongClick(View view) {
            return onLongPressedItem(this,getAdapterPosition());
        }
    }
}
