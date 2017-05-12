package com.xyz.digital.photo.app.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.fragment.SetFragment;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;

/**
 * Created by O on 2017/3/18.
 */

public class WelcomeActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button button = (Button) findViewById(R.id.welcome_begin_employ_bt);

        int id = PreferenceUtils.getInstance().getInt(SetFragment.mSelectLanguage_key, 0);
        button.setText(id == 0 ? "立即体验" : "Experience Immediately");
        button.setOnClickListener(this);

        PubUtils.deleteTempFile();
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }
}
