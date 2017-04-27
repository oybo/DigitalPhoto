package com.xyz.digital.photo.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.bean.ItemSelect;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.SelectDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by O on 2017/3/18.
 * 相框设置
 */

public class SetFragment extends BaseFragment implements View.OnClickListener {

    private SelectDialog mSelectDialog;
    private List<ItemSelect> mItemSelects;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_set, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
    }

    private void initView() {
        getView().findViewById(R.id.set_image_show_ratio_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_image_play_time_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_image_play_order_layout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        showSelectDialog();

//        switch (v.getId()) {
//            case R.id.set_image_show_ratio_layout:
//                break;
//        }
    }

    private void showSelectDialog() {
        mItemSelects = new ArrayList<>();
        mItemSelects.add(new ItemSelect("全屏", true));
        mItemSelects.add(new ItemSelect("原生尺寸", false));
        mItemSelects.add(new ItemSelect("等比例缩放", false));
        mSelectDialog = new SelectDialog(getActivity());
        mSelectDialog.show("t", mItemSelects, new SelectDialog.OnSelectListener() {
            @Override
            public void select(String tag, int position) {
                ToastUtil.showToast(getActivity(), mItemSelects.get(position).getTitle());
            }
        });
    }

}
