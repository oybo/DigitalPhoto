package com.xyz.digital.photo.app.adapter;

import android.content.Context;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.manager.ImageLoadManager;
import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;

/**
 * Created by O on 2017/3/18.
 */

public class ChildImageAdapter extends BaseRecyclerAdapter<MediaFileBean> {

    public ChildImageAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_grid_child_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, MediaFileBean item) {

        holder.setText(R.id.child_title, item.getFileName());

        if(item.getFileType() == PhotoContract.MEDIA_FILE_TYPE.IMAGE) {

            ImageLoadManager.setImage(mContext, item.getFilePath(), holder.getImageView(R.id.child_image));
        } else if(item.getFileType() == PhotoContract.MEDIA_FILE_TYPE.AUDIO) {

            holder.getImageView(R.id.child_image).setImageResource(R.drawable.audio_icon);
        } else if(item.getFileType() == PhotoContract.MEDIA_FILE_TYPE.VIDEO) {

            ImageLoadManager.setImage(mContext, item.getFilePath(), holder.getImageView(R.id.child_image));
//            MultiMediaUtils.getVideoThumbnail(item.getFilePath(), MediaStore..MICRO_KIND);
        } else {

            ImageLoadManager.setImage(mContext, item.getFilePath(), holder.getImageView(R.id.child_image));
        }
    }
}
