package com.xyz.digital.photo.app.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;


/**
 * SwitchButton
 *
 * @author kyleduo
 * @since 2014-09-24
 */

public class SwitchButton extends ImageView {

    public SwitchButton(Context context) {
        super(context);
        init();
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setCheck(false);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isCheck = !isCheck;
                setCheck(isCheck);
                if(mOnSwitchListener != null) {
                    mOnSwitchListener.OnCheckListenr(isCheck);
                }
            }
        });
    }

    public void setCheck(boolean check) {
        isCheck = check;
        if(check) {
            setImageResource(R.drawable.set_switch_pressed);
        } else {
            setImageResource(R.drawable.set_switch_normal);
        }
    }

    private boolean isCheck;
    private OnSwitchListener mOnSwitchListener;

    public void setOnSwitchListener(OnSwitchListener listener) {
        mOnSwitchListener = listener;
    }

    public interface OnSwitchListener {
        void OnCheckListenr(boolean isCheck);
    }

}