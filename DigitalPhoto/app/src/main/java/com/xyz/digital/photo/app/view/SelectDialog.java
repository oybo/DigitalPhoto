package com.xyz.digital.photo.app.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DialogSelectAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.ItemSelect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by O on 2017/4/27.
 */

public class SelectDialog extends Dialog implements android.view.View.OnClickListener {

    private List<ItemSelect> mList = new ArrayList<>();
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
                    mOnSelectListener.select(mTag, pos);
                }
            }
        });

        findViewById(R.id.select_dialog_cancel_bt).setOnClickListener(this);
    }

    private OnSelectListener mOnSelectListener;
    private String mTag;

    public void show(String tag, List<ItemSelect> list, OnSelectListener listener) {
        mTag = tag;
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
        void select(String tag, int position);
    }

}