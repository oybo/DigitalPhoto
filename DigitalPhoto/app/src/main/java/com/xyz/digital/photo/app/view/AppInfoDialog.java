package com.xyz.digital.photo.app.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.util.PubUtils;

/**
 * Created by O on 2017/4/27.
 */

public class AppInfoDialog extends Dialog {


    public AppInfoDialog(Context context) {
        super(context, R.style.signin_dialog_style);
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_app_info_layout);
        this.getWindow().getAttributes().width = LayoutParams.MATCH_PARENT;
        this.getWindow().setGravity(Gravity.BOTTOM);

        ((TextView) findViewById(R.id.dialog_app_info_version_txt)).setText(PubUtils.getSoftVersion(getContext()));

    }

}