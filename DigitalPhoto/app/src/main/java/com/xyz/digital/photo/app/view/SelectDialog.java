package com.xyz.digital.photo.app.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.SysConfigHelper;

import java.util.ArrayList;
import java.util.List;

import static com.xyz.digital.photo.app.util.SysConfigHelper.sys_lang_codes;

/**
 * Created by O on 2017/4/27.
 */

public class SelectDialog extends Dialog implements android.view.View.OnClickListener {

    private int mType;
    private List<String> mList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DialogSelectAdapter mAdapter;

    public SelectDialog(Context context) {
        super(context, R.style.signin_dialog_style);
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_select_layout);
        this.getWindow().getAttributes().width = LayoutParams.MATCH_PARENT;
        this.getWindow().setGravity(Gravity.BOTTOM);

        mRecyclerView = (RecyclerView) findViewById(R.id.view_select_dialog_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new DialogSelectAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                dismiss();
                if(mOnSelectListener != null) {
                    mOnSelectListener.select(pos);
                }
            }
        });

        findViewById(R.id.select_dialog_cancel_bt).setOnClickListener(this);
    }

    private OnSelectListener mOnSelectListener;

    public void show(int type, List<String> list, OnSelectListener listener) {
        mType = type;
        if(list != null) {
            mList.addAll(list);
        }
        mOnSelectListener = listener;
        mAdapter.clear();
        mAdapter.appendToList(list);
        mAdapter.notifyDataSetChanged();

        show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_dialog_cancel_bt:
                dismiss();
                break;

        }
    }

    public interface OnSelectListener {
        void select(int position);
    }

    public class DialogSelectAdapter extends BaseRecyclerAdapter<String> {

        public DialogSelectAdapter(Context ctx) {
            super(ctx);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_select_layout;
        }

        @Override
        public void bindData(RecyclerViewHolder holder, int position, String item) {

            TextView txt = (TextView) holder.getView(R.id.item_select_title_txt);
            if(mType == 20) {
                // 语言
                txt.setText(DeviceManager.getInstance().getLanguageStr(item));
            } else {
                txt.setText(item);
            }

            ImageView tagView = (ImageView) holder.getView(R.id.item_select_tag_txt);
            tagView.setVisibility(View.INVISIBLE);

            int id = 0;
            switch (mType) {
                case 1:
                    // 图片显示比例
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mImageShowScale_key, 0);
                    break;
                case 2:
                    // 幻灯片放映时间
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mPlayTime_key, 0);
                    break;
                case 3:
                    // 幻灯片播放顺序
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mPlayOrder_key, 0);
                    break;
                case 4:
                    // 视频显示比例
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mVideoShowScale_key, 0);
                    break;
                case 5:
                    // 视频播放模式
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mVideoPlayModel_key, 0);
                    break;
                case 6:
                    // 音乐播放模式
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mAudioPlayModel_key, 0);
                    break;
                case 7:
                    // 开机播放模式
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mStartPlayModel_key, 0);
                    break;
                case 8:
                    // 选择语言
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.mSelectLanguage_key, 0);
                    break;
                case 18:
                    // 闹钟频率
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.calendar_alarm_freq, 0);
                    break;
                case 19:
                    // 定时开关机频率
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.sys_auto_power_freq, 0);
                    break;
                case 20:
                    // 当前语言
                    final String language = DeviceManager.getInstance().getpropertiesValue(sys_lang_codes);
                    txt.setSelected(language.equals(item));
                    if(txt.isSelected()) {
                        tagView.setVisibility(View.VISIBLE);
                    }
                    return;
                case 21:
                    // 音量
                    id = PreferenceUtils.getInstance().getInt(SysConfigHelper.sys_volume, 0);
                    break;
            }

            txt.setSelected(id == position ? true : false);
            if(txt.isSelected()) {
                tagView.setVisibility(View.VISIBLE);
            }
        }
    }


}