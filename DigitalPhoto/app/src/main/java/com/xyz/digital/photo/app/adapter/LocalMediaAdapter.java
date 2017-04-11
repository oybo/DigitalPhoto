package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.manager.ImageLoadManager;
import com.xyz.digital.photo.app.view.ProgressPieView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by O on 2017/3/18.
 */

public class LocalMediaAdapter extends BaseRecyclerAdapter<MediaFileBean> {

    public LocalMediaAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_grid_child_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, MediaFileBean item) {

        holder.setText(R.id.item_child_title_txt, item.getFileName());

        holder.setText(R.id.item_child_date_txt, item.getDate());

        holder.setText(R.id.item_child_size_txt, item.getSize());

        ImageView imageView = holder.getImageView(R.id.item_child_image);

        // 图片
        if(item.getFileType() == MEDIA_FILE_TYPE.AUDIO) {
            imageView.setImageResource(R.drawable.defult_audio_icon);
        } else {
            ImageLoadManager.setImage(item.getFilePath(), imageView);
        }

        // 判断是否选中
        if(mSelectMaps.containsKey(item.getFilePath())) {
            imageView.setColorFilter(Color.parseColor("#77000000"));
            holder.getView(R.id.item_child_select_image).setVisibility(View.VISIBLE);
        } else {
            imageView.setColorFilter(null);
            holder.getView(R.id.item_child_select_image).setVisibility(View.GONE);
        }

        // 判断是否上传
        ProgressPieView pieView = (ProgressPieView) holder.getView(R.id.item_child_upload_progress);
        pieView.setTag("ProgressPieView" + item.getFilePath());
        if(mUploadMaps.containsKey(item.getFilePath())) {
            holder.getView(R.id.item_child_upload_cancel).setVisibility(View.VISIBLE);
            pieView.setVisibility(View.VISIBLE);
        } else {
            holder.getView(R.id.item_child_upload_cancel).setVisibility(View.GONE);
            pieView.setVisibility(View.GONE);
        }

    }

    private HashMap<String, MediaFileBean> mSelectMaps = new HashMap<>();

    public int select(int pos) {
        MediaFileBean bean = getItem(pos);
        bean.setPosition(pos);
        if(mSelectMaps.containsKey(bean.getFilePath())) {
            mSelectMaps.remove(bean.getFilePath());
        } else {
            mSelectMaps.put(bean.getFilePath(), bean);
        }
        notifyItemChanged(pos);
        return mSelectMaps.size();
    }

    public int selectAll() {
        for(MediaFileBean bean : getList()) {
            mSelectMaps.put(bean.getFilePath(), bean);
        }
        notifyDataSetChanged();
        return mSelectMaps.size();
    }

    public int clearSelect() {
        mSelectMaps.clear();
        notifyDataSetChanged();
        return 0;
    }

    public List<MediaFileBean> getSelectFiles() {
        List<MediaFileBean> lists = new ArrayList<>();
        for(Map.Entry<String, MediaFileBean> entry : mSelectMaps.entrySet()) {
            lists.add(entry.getValue());
        }
        return lists;
    }

    private HashMap<String, MediaFileBean> mUploadMaps = new HashMap<>();

    public void addUpload(int pos) {
        MediaFileBean bean = getItem(pos);
        if(mUploadMaps.containsKey(bean.getFilePath())) {
            mUploadMaps.remove(bean.getFilePath());
        } else {
            mUploadMaps.put(bean.getFilePath(), bean);
        }
        notifyItemChanged(pos);
    }

    public void clearUpload() {
        clearSelect();
        mUploadMaps.clear();
        for(Map.Entry<String, MediaFileBean> entry : mUploadMaps.entrySet()) {
            notifyItemChanged(entry.getValue().getPosition());
        }
    }

}
