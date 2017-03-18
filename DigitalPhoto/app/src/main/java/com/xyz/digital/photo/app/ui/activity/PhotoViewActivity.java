/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.xyz.digital.photo.app.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.manager.ImageLoadManager;
import com.xyz.digital.photo.app.ui.BaseActivity;
import com.xyz.digital.photo.app.view.photoview.PhotoView;
import com.xyz.digital.photo.app.view.photoview.PhotoViewAttacher;

import java.util.List;

/**
 * 图片详情页
 * 
 * @author ouyangbo
 * 
 */
public class PhotoViewActivity extends BaseActivity implements OnClickListener {

	private Context mContext;
	private ViewPager mViewPager;
	private SamplePagerAdapter pagerAdapter;
	private int mIndex, mLenPage, mCount;
	private List<String> mPaths;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_view);

		mContext = this;

		initView();
		initData();
	}

	private void initData() {
		Bundle bundle = getIntent().getExtras();
		mPaths = (List<String>) bundle.getSerializable("paths");
		mIndex = bundle.getInt("curIndex", 0);
		mCount = mPaths.size();

		mLenPage = mPaths.size();

		pagerAdapter = new SamplePagerAdapter(mPaths);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(mIndex);
		mViewPager.setOnPageChangeListener(new onPageChangeListener());
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.main_photo_viewpager);
	}

	class onPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			mIndex = arg0;

//			mTitleTxt.setText((mIndex + 1) + "/" + mLenPage);

		}

	}

	class SamplePagerAdapter extends PagerAdapter {
		List<String> items = null;

		public SamplePagerAdapter(List<String> resources) {
			this.items = resources;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			final PhotoView photoView = new PhotoView(container.getContext());
			String path = items.get(position);

			ImageLoadManager.setImage(mContext, path, photoView);

			photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
				@Override
				public void onPhotoTap(View view, float x, float y) {
					finish();
				}
			});

			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			PhotoView photoView = (PhotoView) object;
			// photoView.gc();
			container.removeView(photoView);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

	@Override
	public void onClick(View v) {

	}
	@Override
	protected boolean translucentStatusBar() {
		return true;
	}

	@Override
	protected int getToolBarMenuView() {
		return 0;
	}

	@Override
	protected void onItemMenuSelected(MenuItem item) {

	}

}
