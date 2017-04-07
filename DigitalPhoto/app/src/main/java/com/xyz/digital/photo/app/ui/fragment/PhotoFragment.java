package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.listener.UploadListener;
import com.lzy.okserver.task.ExecutorWithListener;
import com.lzy.okserver.upload.UploadInfo;
import com.lzy.okserver.upload.UploadManager;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.ChildImageAdapter;
import com.xyz.digital.photo.app.adapter.FolderAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;
import com.xyz.digital.photo.app.mvp.Photo.PhotoPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.PhotoViewActivity;
import com.xyz.digital.photo.app.ui.activity.ShowImageListActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.FileUtil;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DialogTips;
import com.xyz.digital.photo.app.view.PhotoUploadPopView;
import com.xyz.digital.photo.app.view.ProgressPieView;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Response;

/**
 * Created by O on 2017/3/18.
 * 本地文件
 */

public class PhotoFragment extends BaseFragment implements PhotoContract.View, View.OnClickListener, ExecutorWithListener
        .OnAllTaskEndListener {

    @Bind(R.id.fragment_photo_model_type) ImageView mModelTypeImage;
    @Bind(R.id.fragment_photo_tablayout) TabLayout mTabLayout;
    @Bind(R.id.fragment_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.fragment_media_list_recyclerview) RecyclerView mListRecyclerView;
    @Bind(R.id.fragment_photo_tab_layout) RelativeLayout mTabBarLayout;
    @Bind(R.id.fragment_photo_select_layout) RelativeLayout mSelectLayout;
    @Bind(R.id.fragment_photo_select_num_txt) TextView mSelectNumTxt;
    @Bind(R.id.fragment_photo_select_all_txt) TextView mSelectAllTxt;
    private PhotoUploadPopView mPhotoUploadPopView;

    private PhotoContract.Presenter mPresenter;

    private ChildImageAdapter mChartAdapter;
    private FolderAdapter mListAdapter;

    /**     上传管理      */
    private UploadManager uploadManager;
    private boolean isUpload;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        mModelTypeImage.setOnClickListener(this);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                switch (pos) {
                    case 0:
                        // 图片
                        tab.select();
                        mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.IMAGE);
                        break;
                    case 1:
                        // 视频
                        tab.select();
                        mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.VIDEO);
                        break;
                    case 2:
                        // 音乐
                        tab.select();
                        mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.AUDIO);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mChartRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        getView().findViewById(R.id.fragment_photo_choose_tab).setOnClickListener(this);
        getView().findViewById(R.id.fragment_photo_select_cancel_txt).setOnClickListener(this);
        mSelectAllTxt.setOnClickListener(this);
    }

    private void initData() {
        mPhotoUploadPopView = new PhotoUploadPopView(getActivity(), this);
        mPresenter = new PhotoPresenter(this);
        // 默认图表模式
        mPresenter.showType(PhotoContract.MEDIA_SHOW_TYPE.CHART);

        // 上传管理
        uploadManager = UploadManager.getInstance();
        uploadManager.getThreadPool().setCorePoolSize(1);
        uploadManager.getThreadPool().getExecutor().addOnAllTaskEndListener(this);
    }

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackMediasByList(final HashMap<String, List<MediaFileBean>> images) {
        // 列表模式
        mChartRecyclerView.setVisibility(View.GONE);
        mListRecyclerView.setVisibility(View.VISIBLE);
        mModelTypeImage.setImageResource(R.drawable.mode_list_icon);

        if(mListAdapter == null) {
            mListAdapter = new FolderAdapter(getActivity());
            mListRecyclerView.setAdapter(mListAdapter);
            mListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int pos) {
                    String folderName = mListAdapter.getItem(pos).getFolderName();
                    List<MediaFileBean> childList = images.get(folderName);

                    Intent intent = new Intent(getActivity(), ShowImageListActivity.class);
                    intent.putExtra("title", folderName);
                    intent.putExtra("data", (Serializable) childList);
                    startActivity(intent);
                }
            });
        }
        mListAdapter.clear();
        mListAdapter.appendToList(mPresenter.subGroupOfMedia(images));
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCallbackMediasByChart(List<MediaFileBean> files) {
        // 图表模式
        mListRecyclerView.setVisibility(View.GONE);
        mChartRecyclerView.setVisibility(View.VISIBLE);
        mModelTypeImage.setImageResource(R.drawable.mode_chrat_icon);

        if(mChartAdapter == null) {
            mChartAdapter = new ChildImageAdapter(_getActivity());
            mChartRecyclerView.setAdapter(mChartAdapter);
            mChartAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int pos) {
                    if(isUpload) {
                        ToastUtil.showToast(getActivity(), "正在上传,请稍后");
                        return;
                    }
                    if(mSelectLayout.getVisibility() == View.VISIBLE) {
                        // 选择模式
                        mSelectNumTxt.setText("已选择" + mChartAdapter.select(pos) + "项");
                        checkSelectAll();
                    } else {
                        MediaFileBean mediaFileBean = mChartAdapter.getItem(pos);
                        if(mediaFileBean.getFileType() == PhotoContract.MEDIA_FILE_TYPE.IMAGE) {
                            // 图片
                            Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
                            intent.putExtra("path", mediaFileBean.getFilePath());
                            intent.putExtra("title", mediaFileBean.getFileName());
                            intent.putExtra("date", mediaFileBean.getDate());
                            intent.putExtra("size", mediaFileBean.getSize());
                            startActivity(intent);
                        } else {
                            String type = "audio/*";
                            if(mediaFileBean.getFileType() == PhotoContract.MEDIA_FILE_TYPE.VIDEO) {
                                type = "video/*";
                            }

                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setDataAndType(Uri.parse("file://" + mediaFileBean.getFilePath()), type);
                            startActivity(it);
                        }
                    }
                }
            });
            mChartAdapter.setOnInViewClickListener(R.id.item_child_upload_cancel, new BaseRecyclerAdapter.onInternalClickListener() {
                @Override
                public void OnClickListener(View parentV, View v, Integer position) {
                    // 停止上传任务
                    try {
                        String path = mChartAdapter.getItem(position).getFilePath();
                        for (UploadInfo uploadInfo : uploadManager.getAllTask()) {
                            if(path.equals(uploadInfo.getTaskKey())) {
                                uploadManager.getAllTask().remove(uploadInfo);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        mChartAdapter.clear();
        mChartAdapter.appendToList(files);
        mChartAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(PhotoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onClick(View view) {
        if(isUpload) {
            ToastUtil.showToast(getActivity(), "正在上传,请稍后");
            return;
        }
        switch (view.getId()) {
            case R.id.fragment_photo_model_type:
                // 点击切换浏览模式
                new ChooseModePopView(getActivity(), this).showAsDropDown(view, 0, 0);
                break;
            case R.id.fragment_photo_choose_tab:
                // 点击选择
                if(mChartRecyclerView.getVisibility() == View.VISIBLE) {
                    // 图表模式
                    showSelect();
                }
                break;
            case R.id.fragment_photo_select_cancel_txt:
                // 取消
                closeSelect();
                break;
            case R.id.fragment_photo_select_all_txt:
                // 全选
                if(isSelectAll()) {
                    // 取消全选
                    mSelectNumTxt.setText("已选择" + mChartAdapter.clearSelect() + "项");
                } else {
                    mSelectNumTxt.setText("已选择" + mChartAdapter.selectAll() + "项");
                }
                checkSelectAll();
                break;
            case R.id.view_chart_mode:
                // 图表模式
                mPresenter.showType(PhotoContract.MEDIA_SHOW_TYPE.CHART);
                break;
            case R.id.view_list_mode:
                // 列表模式
                mPresenter.showType(PhotoContract.MEDIA_SHOW_TYPE.LIST);
                break;
            case R.id.pop_upload_to_device_bt:
                // 上传到设备
                uploadFiles();
                break;
            case R.id.pop_delete_bt:
                // 删除
                deleteFiles();
                break;
        }
    }

    private void uploadFiles() {
        isUpload = true;
         for(MediaFileBean bean : mChartAdapter.getSelectFiles()) {
             MyUploadListener listener = new MyUploadListener();
             listener.setUserTag(mChartRecyclerView.getChildAt(bean.getPosition()));

             PostRequest postRequest = OkGo.post(Constants.URL_FORM_UPLOAD);
             postRequest.params("fileKey" + bean.getPosition(), new File(bean.getFilePath()));
             uploadManager.addTask(bean.getFilePath(), postRequest, listener);

             mChartAdapter.addUpload(bean.getPosition());
         }
    }

    private void deleteFiles() {
        showTipDialog(getActivity(), "是否删除?", new DialogTips.onDialogOkListenner() {
            @Override
            public void onClick() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        showLoading();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        for(MediaFileBean file : mChartAdapter.getSelectFiles()) {
                            FileUtil.deleteFile(file.getFilePath());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mSelectNumTxt.setText("已选择" + mChartAdapter.clearSelect() + "项");
                        switch (mTabLayout.getSelectedTabPosition()) {
                            case 0:
                                // 图片
                                mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.IMAGE);
                                break;
                            case 1:
                                // 视频
                                mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.VIDEO);
                                break;
                            case 2:
                                // 音乐
                                mPresenter.showMediaFiles(PhotoContract.MEDIA_FILE_TYPE.AUDIO);
                                break;
                        }
                        hideLoading();
                    }
                }.execute();
            }
        }, null, false);
    }

    private void showSelect() {
        mTabBarLayout.setVisibility(View.GONE);
        mSelectLayout.setVisibility(View.VISIBLE);

        mPhotoUploadPopView.showAtLocation(mTabBarLayout, Gravity.BOTTOM, 0, 0);
    }

    private void hideSelect() {
        mTabBarLayout.setVisibility(View.VISIBLE);
        mSelectLayout.setVisibility(View.GONE);

        mPhotoUploadPopView.dismiss();
    }

    public boolean isShowSelect() {
        return mSelectLayout.getVisibility() == View.VISIBLE;
    }

    public void closeSelect() {
        hideSelect();
        mSelectNumTxt.setText("已选择" + mChartAdapter.clearSelect() + "项");
    }

    private boolean isSelectAll() {
        return mChartAdapter.getSelectFiles().size() == mChartAdapter.getList().size();
    }

    private void checkSelectAll() {
        if(isSelectAll()) {
            mSelectAllTxt.setText("取消全选");
        } else {
            mSelectAllTxt.setText("全选");
        }
    }

    @Override
    public void onAllTaskEnd() {
        // 全部上传完成
        isUpload = false;
        mChartAdapter.clearUpload();
    }

    private class MyUploadListener extends UploadListener<String> {

        @Override
        public void onProgress(UploadInfo uploadInfo) {
            Log.e("MyUploadListener", "onProgress:" + uploadInfo.getTotalLength() + " " + uploadInfo.getUploadLength() + " " + uploadInfo.getProgress());

            refresh(uploadInfo);
        }

        @Override
        public void onFinish(String s) {
            Log.e("MyUploadListener", "finish:");
        }

        @Override
        public void onError(UploadInfo uploadInfo, String errorMsg, Exception e) {
            Log.e("MyUploadListener", "onError:" + errorMsg);
        }

        @Override
        public String parseNetworkResponse(Response response) throws Exception {
            return response.body().string();
        }

        private void refresh(UploadInfo uploadInfo) {
            ProgressPieView pieView = (ProgressPieView) mChartRecyclerView.findViewWithTag("ProgressPieView" + uploadInfo.getTaskKey());
            if(pieView != null) {
                if (uploadInfo.getState() == DownloadManager.NONE) {
                    pieView.setText("准备");
                } else if (uploadInfo.getState() == UploadManager.WAITING) {
                    pieView.setText("等待");
                } else if (uploadInfo.getState() == UploadManager.UPLOADING) {
                    pieView.setProgress((int) (uploadInfo.getProgress() * 100));
                    pieView.setText((Math.round(uploadInfo.getProgress() * 10000) * 1.0f / 100) + "%");
                } else if (uploadInfo.getState() == UploadManager.ERROR) {
                    pieView.setText("错误");
                } else if (uploadInfo.getState() == UploadManager.FINISH) {
                    pieView.setText("成功");
                }
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        uploadManager.getThreadPool().getExecutor().removeOnAllTaskEndListener(this);
    }

}
