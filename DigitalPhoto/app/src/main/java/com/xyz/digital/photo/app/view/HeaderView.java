package com.xyz.digital.photo.app.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xyz.digital.photo.app.R;


/**
 * 自定义ActionBar ouyangbo
 */
public class HeaderView extends LinearLayout {

	private Toolbar mToolbar;
	private TextView mTitleTxt;

	public HeaderView(Context context) {
		super(context);

		initView(context);
	}

	public HeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
	}

	private void initView(Context context) {
		View headerView = View.inflate(context, R.layout.view_header_layout, null);
		headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		if (headerView != null) {
			addView(headerView);

			try {
				mToolbar = (Toolbar) headerView.findViewById(R.id.activity_public_toolbar);
				mTitleTxt = (TextView) headerView.findViewById(R.id.activity_public_title_txt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Toolbar getToolbar() {
		return mToolbar;
	}

	/**
	 * 设置标题
	 * 
	 * @param txt
	 */
	public void setTitile(String txt) {
		mTitleTxt.setText(txt);
	}

}
