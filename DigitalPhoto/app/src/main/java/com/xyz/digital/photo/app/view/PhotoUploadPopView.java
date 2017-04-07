package com.xyz.digital.photo.app.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2016/11/8.
 */

public class PhotoUploadPopView extends PopupWindow implements View.OnClickListener {

    private Context mContext;
    private View.OnClickListener mMoreCallBack;


    public PhotoUploadPopView(Context context, View.OnClickListener callBack) {
        mContext = context;
        mMoreCallBack = callBack;

        View contentView = View.inflate(mContext, R.layout.view_photo_upload_layout, null);
        this.setContentView(contentView);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(false);
        this.setOutsideTouchable(false);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(new ColorDrawable(00000000));

        // 上传到设备
        contentView.findViewById(R.id.pop_upload_to_device_bt).setOnClickListener(this);
        // 删除
        contentView.findViewById(R.id.pop_delete_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_upload_to_device_bt:
                // 上传到设备
                mMoreCallBack.onClick(v);
                break;
            case R.id.pop_delete_bt:
                // 删除
                mMoreCallBack.onClick(v);
                break;
        }
    }
}
