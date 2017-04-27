package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.widget.TextView;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.ItemSelect;

/**
 * Created by O on 2017/4/27.
 */

public class DialogSelectAdapter extends BaseRecyclerAdapter<ItemSelect> {

    public DialogSelectAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_select_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, ItemSelect item) {

        TextView txt = (TextView) holder.getView(R.id.item_select_title_txt);
        txt.setText(item.getTitle());
        txt.setSelected(item.isSelect());

    }
}
