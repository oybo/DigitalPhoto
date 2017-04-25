package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.actions.actfilemanager.ActFileInfo;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;

/**
 * Created by O on 2017/3/18.
 */

public class DeviceMediaAdapter extends BaseRecyclerAdapter<ActFileInfo> {

    public DeviceMediaAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_device_media_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, ActFileInfo item) {

        holder.setText(R.id.item_device_media_title_txt, item.getFileName());

//        holder.setText(R.id.item_device_media_date_txt, item.getDate());
//
//        holder.setText(R.id.item_device_media_size_txt, item.getSize());

        ImageView imageView = holder.getImageView(R.id.item_device_media_imange);

        // 图片
//        if(item.getFileType() == MEDIA_FILE_TYPE.AUDIO) {
            imageView.setImageResource(R.drawable.defult_audio_icon);
//        } else {
//            ImageLoadManager.setImage(item.getFilePath(), imageView);
//        }

    }

}
