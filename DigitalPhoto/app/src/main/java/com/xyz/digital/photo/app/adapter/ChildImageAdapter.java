package com.xyz.digital.photo.app.adapter;

import android.content.Context;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.manager.ImageLoadManager;

/**
 * Created by O on 2017/3/18.
 */

public class ChildImageAdapter extends BaseRecyclerAdapter<String> {

    public ChildImageAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_grid_child_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, String item) {

        ImageLoadManager.setImage(mContext, item, holder.getImageView(R.id.child_image));

    }
}
