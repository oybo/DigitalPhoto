package com.xyz.digital.photo.app.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;

/**
 * Created by O on 2017/4/27.
 */

public class AboutWeDialog extends Dialog implements View.OnClickListener {

    public AboutWeDialog(Context context) {
        super(context, R.style.signin_dialog_style);
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_about_we_layout);
        this.getWindow().getAttributes().width = LayoutParams.MATCH_PARENT;
        this.getWindow().setGravity(Gravity.BOTTOM);

        String phoneStr = AppContext.getInstance().getSString(R.string.set_about_we_phone_key_txt) +
                "<font color='#12B7F5'>" + AppContext.getInstance().getSString(R.string.set_about_we_phone_value_txt) + "</font>";
        TextView phoneTxt = (TextView) findViewById(R.id.dialog_about_phone);
        phoneTxt.setText(Html.fromHtml(phoneStr));
        phoneTxt.setOnClickListener(this);

        String emailStr = AppContext.getInstance().getSString(R.string.set_about_we_email_key_txt) +
                "<font color='#12B7F5'>" + AppContext.getInstance().getSString(R.string.set_about_we_email_value_txt) + "</font>";
        TextView emailTxt = (TextView) findViewById(R.id.dialog_about_email);
        emailTxt.setText(Html.fromHtml(emailStr));
        emailTxt.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_about_phone:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + AppContext.getInstance().getSString(R.string.set_about_we_phone_value_txt)));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                break;
            case R.id.dialog_about_email:
                try {
                    Intent data = new Intent(Intent.ACTION_SENDTO);
                    data.setData(Uri.parse("mailto:" + AppContext.getInstance().getSString(R.string.set_about_we_email_value_txt)));
                    data.putExtra(Intent.EXTRA_SUBJECT, "");
                    data.putExtra(Intent.EXTRA_TEXT, "");
                    getContext().startActivity(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}