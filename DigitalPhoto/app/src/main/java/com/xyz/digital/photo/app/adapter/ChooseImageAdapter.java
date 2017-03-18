package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.ImageBean;
import com.xyz.digital.photo.app.manager.ImageLoadManager;

/**
 * Created by O on 2017/3/18.
 */

public class ChooseImageAdapter extends BaseRecyclerAdapter<ImageBean> {

    public ChooseImageAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_grid_group_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, ImageBean item) {
        holder.setText(R.id.group_title, item.getFolderName());
        holder.setText(R.id.group_count, String.valueOf(item.getImageCounts()));

        ImageLoadManager.setImage(mContext, item.getTopImagePath(), holder.getImageView(R.id.group_image));
    }

}
