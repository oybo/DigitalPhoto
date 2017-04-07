package com.xyz.digital.photo.app.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2016/11/8.
 */

public class ChooseModePopView extends PopupWindow implements View.OnClickListener {

    private View.OnClickListener mMoreCallBack;

    public ChooseModePopView(final Activity activity, View.OnClickListener callBack) {
        this.mMoreCallBack = callBack;

        View contentView = View.inflate(activity, R.layout.view_choose_model_pop, null);
        this.setContentView(contentView);
        this.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(new ColorDrawable(50000000));

        // 目录模式
        contentView.findViewById(R.id.view_chart_mode).setOnClickListener(this);
        // 浏览模式
        contentView.findViewById(R.id.view_list_mode).setOnClickListener(this);

        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.7f;
        activity.getWindow().setAttributes(lp);
        setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1f;
                activity.getWindow().setAttributes(lp);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_chart_mode:
                // 图表模式
                mMoreCallBack.onClick(v);
                break;
            case R.id.view_list_mode:
                // 列表模式
                mMoreCallBack.onClick(v);
                break;
        }
        dismiss();
    }
}
