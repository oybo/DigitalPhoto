package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.manager.ImageLoadManager;

/**
 * Created by O on 2017/4/5.
 */

public class DeviceImageAdapter extends BaseRecyclerAdapter<String> {

    public DeviceImageAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_device_photo_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, String item) {

        ImageLoadManager.setImage(item, holder.getImageView(R.id.item_device_photo_image),
                R.drawable.defult_audio_icon);

    }

}
