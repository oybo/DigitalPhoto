package com.xyz.digital.photo.app.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.view.AppInfoDialog;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.SelectDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.xyz.digital.photo.app.util.SysConfigHelper.mSelectLanguage;
import static com.xyz.digital.photo.app.util.SysConfigHelper.mSelectLanguage_key;

/**
 * Created by O on 2017/3/18.
 * 相框设置
 */

public class SetFragment extends BaseFragment implements View.OnClickListener {

    @Bind(R.id.view_loading) LoadingView mLoadingView;
    @Bind(R.id.set_version_txt) TextView mVersionTxt;
    @Bind(R.id.set_language_txt) TextView mLanguageTxt;

    private List<String> mItemSelects = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_set, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        getView().findViewById(R.id.set_language_layout).setOnClickListener(this);
        getView().findViewById(R.id.set_info_layout).setOnClickListener(this);
    }

    private void initData() {
        mVersionTxt.setText(PubUtils.getSoftVersion(getActivity()));
        mLanguageTxt.setText(mSelectLanguage[PreferenceUtils.getInstance().getInt(mSelectLanguage_key, 0)]);
    }

    @Override
    public void onClick(View v) {
        mItemSelects.clear();
        switch (v.getId()) {
            case R.id.set_language_layout:
                // 选择语言
                selectLanguage();
                return;
            case R.id.set_info_layout:
                // 功能介绍
                new AppInfoDialog(getActivity()).show();
                return;
        }
    }

    private void selectLanguage() {
        SelectDialog selectLanguageDialog = new SelectDialog(getActivity());
        List<String> mItemSelects = new ArrayList<>();
        for(String str : mSelectLanguage) {
            mItemSelects.add(str);
        }
        selectLanguageDialog.show(8, mItemSelects, new SelectDialog.OnSelectListener() {
            @Override
            public void select(int position) {
                PreferenceUtils.getInstance().putInt(mSelectLanguage_key, position);
                mLanguageTxt.setText(mSelectLanguage[PreferenceUtils.getInstance().getInt(mSelectLanguage_key, 0)]);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        mLoadingView.show();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        getActivity().recreate();
                        mLoadingView.hide();
                    }
                }.execute();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
