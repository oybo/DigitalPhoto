package com.xyz.digital.photo.app.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.xyz.digital.photo.app.adapter.FolderAdapter;
import com.xyz.digital.photo.app.adapter.LocalMediaAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;
import com.xyz.digital.photo.app.mvp.Photo.PhotoPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.PhotoViewActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.FileUtil;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DialogTips;
import com.xyz.digital.photo.app.view.DividerItemDecoration;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.PhotoUploadPopView;
import com.xyz.digital.photo.app.view.ProgressPieView;

import java.io.File;
import java.util.ArrayList;
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
    @Bind(R.id.fragment_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.fragment_media_list_recyclerview) RecyclerView mListRecyclerView;
    @Bind(R.id.fragment_photo_tab_layout) RelativeLayout mTabBarLayout;
    @Bind(R.id.fragment_photo_select_layout) RelativeLayout mSelectLayout;
    @Bind(R.id.fragment_photo_select_num_txt) TextView mSelectNumTxt;
    @Bind(R.id.fragment_photo_select_all_txt) TextView mSelectAllTxt;
    @Bind(R.id.view_loading) LoadingView mLoadingView;
    @Bind(R.id.fragment_photo_image_tab) TextView fragmentPhotoImageTab;
    @Bind(R.id.fragment_photo_video_tab) TextView fragmentPhotoVideoTab;
    @Bind(R.id.fragment_photo_audio_tab) TextView fragmentPhotoAudioTab;
    @Bind(R.id.fragment_media_list_path_txt) TextView mPathTxt;
    @Bind(R.id.fragment_media_list_layout) LinearLayout mListLayout;

    private PhotoUploadPopView mPhotoUploadPopView;
    private PhotoContract.Presenter mPresenter;
    private LocalMediaAdapter mChartAdapter;
    private HashMap<String, List<MediaFileBean>> mListImages;
    private FolderAdapter mListAdapter;
    private int firstItemPosition;

    /**
     * 上传管理
     */
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
        mChartRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mModelTypeImage.setOnClickListener(this);
        fragmentPhotoImageTab.setOnClickListener(this);
        fragmentPhotoVideoTab.setOnClickListener(this);
        fragmentPhotoAudioTab.setOnClickListener(this);
        getView().findViewById(R.id.fragment_photo_choose_tab).setOnClickListener(this);
        getView().findViewById(R.id.fragment_photo_select_cancel_txt).setOnClickListener(this);
        mSelectAllTxt.setOnClickListener(this);
        mPathTxt.setOnClickListener(this);
    }

    private void initData() {
        mChartAdapter = new LocalMediaAdapter(_getActivity());
        mChartRecyclerView.setAdapter(mChartAdapter);

        mListAdapter = new FolderAdapter(getActivity());
        mListRecyclerView.setAdapter(mListAdapter);

        mPhotoUploadPopView = new PhotoUploadPopView(getActivity(), this);
        mPresenter = new PhotoPresenter(this);
        // 默认图表模式
        mPresenter.showType(MEDIA_SHOW_TYPE.CHART);

        // 上传管理
        uploadManager = UploadManager.getInstance();
        uploadManager.getThreadPool().setCorePoolSize(1);
        uploadManager.getThreadPool().getExecutor().addOnAllTaskEndListener(this);

        // 图表模式
        mChartAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                if (mSelectLayout.getVisibility() == View.VISIBLE) {
                    // 选择模式
                    mSelectNumTxt.setText("已选择" + mChartAdapter.select(pos) + "项");
                    checkSelectAll();
                } else {
                    MediaFileBean mediaFileBean = mChartAdapter.getItem(pos);
                    if (mediaFileBean.getFileType() == MEDIA_FILE_TYPE.IMAGE) {
                        // 图片
                        Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
                        intent.putExtra("path", mediaFileBean.getFilePath());
                        intent.putExtra("title", mediaFileBean.getFileName());
                        intent.putExtra("date", mediaFileBean.getDate());
                        intent.putExtra("size", mediaFileBean.getSize());
                        startActivity(intent);
                    } else {
                        String type = "audio/*";
                        if (mediaFileBean.getFileType() == MEDIA_FILE_TYPE.VIDEO) {
                            type = "video/*";
                        }

                        Intent it = new Intent(Intent.ACTION_VIEW);
                        it.setDataAndType(Uri.parse("file://" + mediaFileBean.getFilePath()), type);
                        startActivity(it);
                    }
                }
            }
        });
        mChartAdapter.setOnInViewClickListener(R.id.item_child_upload_cancel, mChartListener);
        mChartAdapter.setOnInViewClickListener(R.id.item_child_isupload_txt, mChartListener);

        // 列表模式
        mListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                if (mSelectLayout.getVisibility() == View.VISIBLE) {
                    // 选择模式
                    mSelectNumTxt.setText("已选择" + mListAdapter.select(pos) + "项");
                    checkSelectAll();
                } else {
                    // 获取第一个可见view的位置
                    if(!isShowChild()) {
                        // 点击进入子文件夹
                        firstItemPosition = ((LinearLayoutManager) mListRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        List<FolderBean> files = new ArrayList<FolderBean>();
                        String folderName = mListAdapter.getItem(pos).getFolderName();
                        mPathTxt.setText("本地存储设备 > " + folderName);
                        List<MediaFileBean> childList = mListImages.get(folderName);
                        for(MediaFileBean mediaFileBean : childList) {
                            FolderBean folderBean  = new FolderBean();
                            folderBean.setDate(mediaFileBean.getDate());
                            folderBean.setSize(mediaFileBean.getSize());
                            folderBean.setFolderName(mediaFileBean.getFileName());
                            folderBean.setImageCounts(1);
                            folderBean.setTopImagePath(mediaFileBean.getFilePath());
                            folderBean.setFolder(false);
                            folderBean.setFileType(mediaFileBean.getFileType());
                            files.add(folderBean);
                        }

                        mListAdapter.clear();
                        mListAdapter.appendToList(files);
                        mListAdapter.notifyDataSetChanged();
                    } else {
                        // 打开
                        FolderBean folderBean = mListAdapter.getItem(pos);
                        if (folderBean.getFileType() == MEDIA_FILE_TYPE.IMAGE) {
                            // 图片
                            Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
                            intent.putExtra("path", folderBean.getTopImagePath());
                            intent.putExtra("title", folderBean.getFolderName());
                            intent.putExtra("date", folderBean.getDate());
                            intent.putExtra("size", folderBean.getSize());
                            startActivity(intent);
                        } else {
                            String type = "audio/*";
                            if (folderBean.getFileType() == MEDIA_FILE_TYPE.VIDEO) {
                                type = "video/*";
                            }

                            Intent it = new Intent(Intent.ACTION_VIEW);
                            it.setDataAndType(Uri.parse("file://" + folderBean.getTopImagePath()), type);
                            startActivity(it);
                        }
                    }
                }
            }
        });
        mListAdapter.setOnInViewClickListener(R.id.item_child_isupload_txt, mListListener);
    }

    // 图表模式
    private BaseRecyclerAdapter.onInternalClickListener mChartListener = new BaseRecyclerAdapter.onInternalClickListener() {
        @Override
        public void OnClickListener(View parentV, View v, Integer position) {
            switch (v.getId()) {
                case R.id.item_child_upload_cancel:
                    // 停止上传任务
                    try {
                        String path = mChartAdapter.getItem(position).getFilePath();
                        for (UploadInfo uploadInfo : uploadManager.getAllTask()) {
                            if (path.equals(uploadInfo.getTaskKey())) {
                                uploadInfo.getTask().cancel(true);
                                uploadManager.getAllTask().remove(uploadInfo);
                                mChartAdapter.addUpload(position);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.item_child_isupload_txt:
                    // 点击单个上传
                    MediaFileBean file = mChartAdapter.getItem(position);
                    boolean isUpload = PreferenceUtils.getInstance().getBoolean(file.getFilePath(), false);
                    if(!isUpload) {
                        mChartAdapter.select(position);
                        uploadFiles();
                    }
                    break;
            }
        }
    };

    // 列表模式
    private BaseRecyclerAdapter.onInternalClickListener mListListener = new BaseRecyclerAdapter.onInternalClickListener() {
        @Override
        public void OnClickListener(View parentV, View v, Integer position) {
            switch (v.getId()) {
                case R.id.item_child_isupload_txt:
                    // 点击单个上传
                    FolderBean file = mListAdapter.getItem(position);
                    boolean isUpload = PreferenceUtils.getInstance().getBoolean(file.getTopImagePath(), false);
                    if(!isUpload) {
                        mListAdapter.select(position);
                        uploadFiles();
                    }
                    break;
            }
        }
    };

    @Override
    public Activity _getActivity() {
        return getActivity();
    }

    @Override
    public void onCallbackMediasByList(final boolean isRefreshModel, final HashMap<String, List<MediaFileBean>> images) {
        // 列表模式
        try {
            mListImages = images;
            mListAdapter.clear();
            mListAdapter.appendToList(mPresenter.subGroupOfMedia(mListImages));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPathTxt.setText("本地存储设备");
                    mListAdapter.notifyDataSetChanged();
                    if (isRefreshModel) {
                        setSelectTab(1);
                        mChartRecyclerView.setVisibility(View.GONE);
                        mListLayout.setVisibility(View.VISIBLE);
                        mModelTypeImage.setImageResource(R.drawable.mode_list_icon);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallbackMediasByChart(final boolean isRefreshModel, List<MediaFileBean> files) {
        // 图表模式
        try {
            mChartAdapter.clear();
            mChartAdapter.appendToList(files);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChartAdapter.notifyDataSetChanged();
                    if (isRefreshModel) {
                        setSelectTab(1);
                        mListLayout.setVisibility(View.GONE);
                        mChartRecyclerView.setVisibility(View.VISIBLE);
                        mModelTypeImage.setImageResource(R.drawable.mode_chrat_icon);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPresenter(PhotoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mLoadingView.show();
    }

    @Override
    public void hideLoading() {
        mLoadingView.hide();
    }

    @Override
    public void onClick(View view) {
        if (isUpload) {
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
                if((mListAdapter != null && mListAdapter.getList().size() > 0) && mListAdapter.getItem(0).isFolder()) {
                    return;
                }
                showSelect();
                break;
            case R.id.fragment_photo_image_tab:
                // 图片
                if(fragmentPhotoImageTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.IMAGE);
                setSelectTab(1);
                break;
            case R.id.fragment_photo_video_tab:
                // 视频
                if(fragmentPhotoVideoTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.VIDEO);
                setSelectTab(2);
                break;
            case R.id.fragment_photo_audio_tab:
                // 音乐
                if(fragmentPhotoAudioTab.isSelected()) {
                    return;
                }
                mPresenter.showMediaFiles(MEDIA_FILE_TYPE.AUDIO);
                setSelectTab(3);
                break;
            case R.id.fragment_photo_select_cancel_txt:
                // 取消
                closeSelect();
                break;
            case R.id.fragment_photo_select_all_txt:
                // 全选
                if (isSelectAll()) {
                    // 取消全选
                    if(mListLayout.getVisibility() == View.VISIBLE) {
                        mSelectNumTxt.setText("已选择" + mListAdapter.clearSelectAll() + "项");
                    } else {
                        mSelectNumTxt.setText("已选择" + mChartAdapter.clearSelectAll() + "项");
                    }
                } else {
                    if(mListLayout.getVisibility() == View.VISIBLE) {
                        mSelectNumTxt.setText("已选择" + mListAdapter.selectAll() + "项");
                    } else {
                        mSelectNumTxt.setText("已选择" + mChartAdapter.selectAll() + "项");
                    }
                }
                checkSelectAll();
                break;
            case R.id.view_chart_mode:
                // 图表模式
                mPresenter.showType(MEDIA_SHOW_TYPE.CHART);
                break;
            case R.id.view_list_mode:
                // 列表模式
                mPresenter.showType(MEDIA_SHOW_TYPE.LIST);
                break;
            case R.id.pop_upload_to_device_bt:
                // 上传到设备
                int uploadSize = 0;
                if(mListLayout.getVisibility() == View.VISIBLE) {
                    uploadSize = mListAdapter.getSelectFiles().size();
                } else {
                    uploadSize = mChartAdapter.getSelectFiles().size();
                }
                if(uploadSize == 0) {
                    ToastUtil.showToast(getActivity(), "请选择要上传的文件");
                    return;
                }
                uploadFiles();
                closeSelect();
                break;
            case R.id.pop_delete_bt:
                // 删除
                deleteFiles();
            case R.id.fragment_media_list_path_txt:
                // 列表模式的点击返回
                if(isShowChild()) {
                    closeShowChild();
                }
                break;
        }
    }

    private void setSelectTab(int id) {
        fragmentPhotoImageTab.setSelected(false);
        fragmentPhotoVideoTab.setSelected(false);
        fragmentPhotoAudioTab.setSelected(false);
        switch (id) {
            case 1:
                fragmentPhotoImageTab.setSelected(true);
                break;
            case 2:
                fragmentPhotoVideoTab.setSelected(true);
                break;
            case 3:
                fragmentPhotoAudioTab.setSelected(true);
                break;
        }
    }

    private void uploadFiles() {
        isUpload = true;
        if(mListLayout.getVisibility() == View.VISIBLE) {
            // 列表模式
            List<FolderBean> uploads = mListAdapter.getSelectFiles();
            for (int i = 0; i < uploads.size(); i++) {
                FolderBean bean = uploads.get(i);
                if(!mListAdapter.isUpload(bean.getTopImagePath())) {
                    PostRequest postRequest = OkGo.post(Constants.URL_FORM_UPLOAD);
                    postRequest.params("fileKey" + i, new File(bean.getTopImagePath()));
                    uploadManager.addTask(bean.getTopImagePath(), postRequest, new MyUploadListener());

                    mListAdapter.addUpload(bean.getPosition());
                }
            }
        } else {
            // 图表模式
            List<MediaFileBean> uploads = mChartAdapter.getSelectFiles();
            for (int i = 0; i < uploads.size(); i++) {
                MediaFileBean bean = uploads.get(i);
                if(!mChartAdapter.isUpload(bean.getFilePath())) {
                    PostRequest postRequest = OkGo.post(Constants.URL_FORM_UPLOAD);
                    postRequest.params("fileKey" + i, new File(bean.getFilePath()));
                    uploadManager.addTask(bean.getFilePath(), postRequest, new MyUploadListener());

                    mChartAdapter.addUpload(bean.getPosition());
                }
            }
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
                        for (MediaFileBean file : mChartAdapter.getSelectFiles()) {
                            FileUtil.deleteFile(file.getFilePath());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        // 重新刷新下数据
                        if(fragmentPhotoImageTab.isSelected()) {
                            // 图片
                            mPresenter.showMediaFiles(MEDIA_FILE_TYPE.IMAGE);
                        } else if(fragmentPhotoVideoTab.isSelected()) {
                            // 视频
                            mPresenter.showMediaFiles(MEDIA_FILE_TYPE.VIDEO);
                        } else if(fragmentPhotoAudioTab.isSelected()) {
                            // 音乐
                            mPresenter.showMediaFiles(MEDIA_FILE_TYPE.AUDIO);
                        }
                        mSelectNumTxt.setText("已选择0项");
                    }
                }.execute();
            }
        }, null, false);
    }

    private void showSelect() {
        mTabBarLayout.setVisibility(View.GONE);
        mSelectLayout.setVisibility(View.VISIBLE);

        mSelectNumTxt.setText("已选择0项");
        mPhotoUploadPopView.showAtLocation(mTabBarLayout, Gravity.BOTTOM, 0, 0);
        if(mListLayout.getVisibility() == View.VISIBLE) {
            mListAdapter.showSelect(true);
        }
    }

    private void hideSelect() {
        mTabBarLayout.setVisibility(View.VISIBLE);
        mSelectLayout.setVisibility(View.GONE);

        mPhotoUploadPopView.dismiss();
        mChartAdapter.clearSelect();
        mListAdapter.clearSelect();
        mListAdapter.showSelect(false);
    }

    public boolean isShowChild() {
        boolean bol = false;
        if(mListLayout.getVisibility() == View.VISIBLE) {
            if(!mListAdapter.getItem(0).isFolder()) {
                bol = true;
            }
        }
        return bol;
    }

    public void closeShowChild() {
        mPathTxt.setText("本地存储设备");
        mListAdapter.clear();
        mListAdapter.appendToList(mPresenter.subGroupOfMedia(mListImages));
        mListAdapter.notifyDataSetChanged();
        mListRecyclerView.scrollToPosition(firstItemPosition);
    }

    public boolean isShowSelect() {
        return mSelectLayout.getVisibility() == View.VISIBLE;
    }

    public void closeSelect() {
        hideSelect();
    }

    private boolean isSelectAll() {
        boolean bool;
        if(mListLayout.getVisibility() == View.VISIBLE) {
            bool = mListAdapter.getSelectFiles().size() == mListAdapter.getList().size();
        } else {
            bool = mChartAdapter.getSelectFiles().size() == mChartAdapter.getList().size();
        }
        return bool;
    }

    private void checkSelectAll() {
        if (isSelectAll()) {
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
        mSelectNumTxt.setText("已选择0项");
        ToastUtil.showToast(getActivity(), "上传完成");
    }

    private class MyUploadListener extends UploadListener<String> {

        @Override
        public void onProgress(UploadInfo uploadInfo) {
            Log.e("MyUploadListener", "onProgress:" + uploadInfo.getTotalLength() + " " + uploadInfo.getUploadLength() + " " + uploadInfo
                    .getProgress());

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
            ProgressPieView pieView;
            if(mListLayout.getVisibility() == View.VISIBLE) {
                pieView = (ProgressPieView) mListRecyclerView.findViewWithTag("ProgressPieView" + uploadInfo.getTaskKey());
            } else {
                pieView = (ProgressPieView) mChartRecyclerView.findViewWithTag("ProgressPieView" + uploadInfo.getTaskKey());
            }
            if (pieView != null) {
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
                    if(mListLayout.getVisibility() == View.VISIBLE) {
                        for(FolderBean folderBean : mListAdapter.getList()) {
                            if(folderBean.getTopImagePath().equals(uploadInfo.getTaskKey())) {
                                PreferenceUtils.getInstance().putBoolen(folderBean.getTopImagePath(), true);
                                mListAdapter.removeUpload(folderBean.getPosition());
                                mListAdapter.notifyItemChanged(folderBean.getPosition());
                                return;
                            }
                        }
                    } else {
                        for(MediaFileBean mediaFileBean : mChartAdapter.getList()) {
                            if(mediaFileBean.getFilePath().equals(uploadInfo.getTaskKey())) {
                                PreferenceUtils.getInstance().putBoolen(mediaFileBean.getFilePath(), true);
                                mChartAdapter.removeUpload(mediaFileBean.getPosition());
                                mChartAdapter.notifyItemChanged(mediaFileBean.getPosition());
                                return;
                            }
                        }
                    }
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
