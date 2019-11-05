package com.odhiambopaul.musicr.ui.bottomnavigationtab.setting;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odhiambopaul.musicr.R;
import com.odhiambopaul.musicr.ui.widget.fragmentnavigationcontroller.SupportFragment;

public class SettingTabFragment extends SupportFragment {
    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.setting_tab_fragment,container,false);



    }
}
