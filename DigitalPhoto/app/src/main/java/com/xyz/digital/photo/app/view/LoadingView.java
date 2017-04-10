package com.xyz.digital.photo.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2017/4/10.
 */

public class LoadingView extends RelativeLayout {

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        addView(View.inflate(getContext(), R.layout.view_loading, null));

        setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getVisibility() == View.VISIBLE) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.INVISIBLE);
    }

}
