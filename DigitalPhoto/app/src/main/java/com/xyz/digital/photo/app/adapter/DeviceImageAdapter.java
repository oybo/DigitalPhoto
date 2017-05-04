package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.actions.actfilemanager.ActFileInfo;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.util.PubUtils;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceImageAdapter extends BaseRecyclerAdapter<ActFileInfo> {

    public DeviceImageAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_device_photo_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, ActFileInfo item) {

        // 图片
        ImageView imageView = holder.getImageView(R.id.item_device_photo_image);

        MEDIA_FILE_TYPE type = PubUtils.getFileType(item.getFileName());
        
        if (type == MEDIA_FILE_TYPE.IMAGE) {
            imageView.setImageResource(R.drawable.defult_audio_icon);
        } else if (type == MEDIA_FILE_TYPE.VIDEO) {
            imageView.setImageResource(R.drawable.defult_video_icon);
        } else {
            imageView.setImageResource(R.drawable.defult_image_icon);
        }

    }

}
