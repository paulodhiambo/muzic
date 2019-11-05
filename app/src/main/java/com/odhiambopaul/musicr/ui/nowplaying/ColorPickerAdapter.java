package com.odhiambopaul.musicr.ui.nowplaying;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.odhiambopaul.musicr.R;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>  {
    private static final String TAG ="ColorPickerAdapter";
    private ArrayList<Integer> mData = new ArrayList<>();
    private int mSelected = -1;

    public ColorPickerAdapter(OnColorChangedListener listener) {
        this.mListener = listener;
    }
    public interface OnColorChangedListener {
        void onColorChanged(int position,int newColor);
    }

    private OnColorChangedListener mListener;

    public void removeListener() {
        mListener = null;
    }

    public void setSelected(int s) {
        if(s<mData.size()&&s!=mSelected) {
            int old = mSelected;
            mSelected = s;
            notifyItemChanged(old);
            notifyItemChanged(s);
            if(mListener!=null) mListener.onColorChanged(mSelected,mData.get(mSelected));
        }
    }
    private int mColor = -1;
    public void setSelectedColor(Integer color) {
        mColor = color;
        findSelected();
    }
    public void findSelected() {
        int newOne = mData.indexOf(mColor);
        setSelected(newOne);
    }

    public void setData(Integer... color) {
        mData.clear();
        mData.addAll(Arrays.asList(color));
        mSelected = -1;
        notifyDataSetChanged();
        Log.d(TAG, "setData: size = "+color.length +", data size = "+mData.size());

    }

    public void addData(Integer... color) {
        if(color!=null) {
            int posBefore = mData.size();
            for (Integer c : color) {
                if (!mData.contains(c))
                    mData.add(c);
            }
            mSelected = mData.indexOf(mColor);
            findSelected();
            notifyItemRangeChanged(posBefore,mData.size()-posBefore);

            Log.d(TAG, "addData: size = "+color.length +", data size = "+mData.size());

        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view =LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_color_picker, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mData.get(i));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon) ImageView mIcon;
        @OnClick(R.id.icon)
        void onClickItem() {
            setSelected(getAdapterPosition());
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            mIcon.setBackground(new ColorCircleDrawable(0));
        }

        public void bind(int color) {
            ((ColorCircleDrawable)mIcon.getBackground()).setColor(color);
            if(getAdapterPosition()==mSelected)
                mIcon.setImageResource(R.drawable.ic_colorpicker_swatch_selected);
            else mIcon.setImageResource(android.R.color.transparent);
        }
    }
}