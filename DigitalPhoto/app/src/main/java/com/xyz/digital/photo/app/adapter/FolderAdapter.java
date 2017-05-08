package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.UploadInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.manager.ImageLoadManager;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.view.ProgressPieView;
import com.xyz.digital.photo.app.view.RoundAngleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/3/18.
 */

public class FolderAdapter extends BaseRecyclerAdapter<FolderBean> {

    public FolderAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_local_grid_group_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, FolderBean item) {

        holder.setText(R.id.item_list_title_txt, item.getFolderName());

        holder.setText(R.id.item_list_size_txt, item.getSize());

        holder.setText(R.id.item_list_count_txt, String.valueOf(item.getImageCounts()) + "项");

        holder.setText(R.id.item_list_time_txt, item.getDate());

        RoundAngleImageView imageView = (RoundAngleImageView) holder.getView(R.id.item_list_image);

        ProgressPieView pieView = (ProgressPieView) holder.getView(R.id.item_child_upload_progress);
        pieView.setTag("ProgressPieView" + item.getTopImagePath());

        if (item.isFolder()) {
            // 文件夹
            imageView.setImageResource(R.drawable.folder);

            holder.getView(R.id.item_child_arrows_image).setVisibility(View.VISIBLE);
            holder.getView(R.id.item_child_isupload_txt).setVisibility(View.INVISIBLE);
            holder.getView(R.id.item_child_select_image).setVisibility(View.INVISIBLE);
            pieView.setVisibility(View.INVISIBLE);
        } else {
            holder.getView(R.id.item_child_arrows_image).setVisibility(View.INVISIBLE);
            if (item.getFileType() == MEDIA_FILE_TYPE.AUDIO) {
                imageView.setImageResource(R.drawable.defult_audio_icon);
            } else {
                ImageLoadManager.setImage(item.getTopImagePath(), imageView);
            }
            if (isShowSelect) {
                // 开始选择
                holder.getView(R.id.item_child_isupload_txt).setVisibility(View.INVISIBLE);
                holder.getView(R.id.item_child_select_image).setVisibility(View.INVISIBLE);
                pieView.setVisibility(View.INVISIBLE);

                if (mSelectMaps.containsKey(item.getTopImagePath())) {
                    holder.getView(R.id.item_child_select_image).setVisibility(View.VISIBLE);
                }
            } else {
                holder.getView(R.id.item_child_select_image).setVisibility(View.INVISIBLE);
                TextView isUoloadTxt = holder.getTextView(R.id.item_child_isupload_txt);

                // 判断是否处于上传
                if (DeviceManager.getInstance().isUpload(item.getTopImagePath())) {
                    isUoloadTxt.setVisibility(View.INVISIBLE);
                    pieView.setVisibility(View.VISIBLE);
                    UploadInfo uploadInfo = DeviceManager.getInstance().getUploadInfo(item.getTopImagePath());
                    switch (uploadInfo.getState()) {
                        case 0:
                            pieView.setText("等待");
                            break;
                    }
                } else {
                    pieView.setVisibility(View.INVISIBLE);
                }

                boolean isUpload = PreferenceUtils.getInstance().getBoolean(item.getTopImagePath(), false);
                if (isUpload) {
                    isUoloadTxt.setVisibility(View.VISIBLE);
                    pieView.setText("成功");
                    if(!DeviceManager.getInstance().isUpload(item.getTopImagePath())) {
                        pieView.setVisibility(View.INVISIBLE);
                    }

                    isUoloadTxt.setText("已上传");
                    isUoloadTxt.setSelected(false);
                } else {
                    if (DeviceManager.getInstance().isUpload(item.getTopImagePath())) {
                        isUoloadTxt.setVisibility(View.INVISIBLE);
                    } else {
                        isUoloadTxt.setVisibility(View.VISIBLE);
                    }
                    isUoloadTxt.setText("上传");
                    isUoloadTxt.setSelected(true);
                }
            }
        }
    }

    private HashMap<String, FolderBean> mSelectMaps = new HashMap<>();
    private boolean isShowSelect;

    public void showSelect(boolean isShowSelect) {
        this.isShowSelect = isShowSelect;
        notifyDataSetChanged();
    }

    public int select(int pos) {
        FolderBean bean = getItem(pos);
        bean.setPosition(pos);
        if (mSelectMaps.containsKey(bean.getTopImagePath())) {
            mSelectMaps.remove(bean.getTopImagePath());
        } else {
            mSelectMaps.put(bean.getTopImagePath(), bean);
        }
        notifyItemChanged(pos);
        return mSelectMaps.size();
    }

    public int selectAll() {
        for (FolderBean bean : getList()) {
            mSelectMaps.put(bean.getTopImagePath(), bean);
        }
        notifyDataSetChanged();
        return mSelectMaps.size();
    }

    public int clearSelectAll() {
        if (mSelectMaps.size() > 0) {
            mSelectMaps.clear();
            notifyDataSetChanged();
        }
        return 0;
    }

    public int clearSelect() {
        if (mSelectMaps.size() > 0) {
            for (Map.Entry<String, FolderBean> entry : mSelectMaps.entrySet()) {
                notifyItemChanged(entry.getValue().getPosition());
            }
            mSelectMaps.clear();
        }
        return 0;
    }

    public List<FolderBean> getSelectFiles() {
        List<FolderBean> lists = new ArrayList<>();
        if (mSelectMaps.size() > 0) {
            for (Map.Entry<String, FolderBean> entry : mSelectMaps.entrySet()) {
                lists.add(entry.getValue());
            }
            java.util.Collections.sort(lists, new java.util.Comparator() {

                @Override
                public int compare(Object o, Object t1) {
                    return new Integer(((FolderBean) o).getPosition()).compareTo(new Integer(((FolderBean) t1).getPosition()));
                }
            });
        }
        return lists;
    }

    public boolean isUpload(String key) {
        return DeviceManager.getInstance().isUpload(key);
    }

    public void addUpload(int pos) {
        FolderBean bean = getItem(pos);
        DeviceManager.getInstance().addUpload(bean.getTopImagePath(), bean.getFolderName());
        notifyItemChanged(pos);
    }

    public void removeUpload(int pos) {
        FolderBean bean = getItem(pos);
        mSelectMaps.remove(bean.getTopImagePath());
    }

}
