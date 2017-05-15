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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.FolderAdapter;
import com.xyz.digital.photo.app.adapter.LocalMediaAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.bean.FolderBean;
import com.xyz.digital.photo.app.bean.MediaFileBean;
import com.xyz.digital.photo.app.bean.UploadInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.mvp.Photo.PhotoContract;
import com.xyz.digital.photo.app.mvp.Photo.PhotoPresenter;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.PhotoViewActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.FileUtil;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DialogTips;
import com.xyz.digital.photo.app.view.DividerItemDecoration;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.PhotoUploadPopView;
import com.xyz.digital.photo.app.view.ProgressPieView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/3/18.
 * 本地文件
 */

public class PhotoFragment extends BaseFragment implements PhotoContract.View, View.OnClickListener{

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
        EventBus.getDefault().register(this);
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
        setSelectTab(1);

        // 图表模式
        mChartAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int pos) {
                if (mSelectLayout.getVisibility() == View.VISIBLE) {
                    // 选择模式
                    mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(mChartAdapter.select(pos))));
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
                    mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(mListAdapter.select(pos))));
                    checkSelectAll();
                } else {
                    // 获取第一个可见view的位置
                    if(!isShowChild()) {
                        // 点击进入子文件夹
                        firstItemPosition = ((LinearLayoutManager) mListRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                        List<FolderBean> files = new ArrayList<FolderBean>();
                        String folderName = mListAdapter.getItem(pos).getFolderName();
                        mPathTxt.setText(AppContext.getInstance().getSString(R.string.bendi_device_txt) + " > " + folderName);
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
                        UploadInfo uploadInfo = DeviceManager.getInstance().getCurUploadInfo();
                        if(uploadInfo != null && uploadInfo.getFilePath().equals(path)) {
                            return;
                        }
                        DeviceManager.getInstance().removeUpload(path);
                        mChartAdapter.notifyItemChanged(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.item_child_isupload_txt:
                    // 点击单个上传
                    MediaFileBean file = mChartAdapter.getItem(position);
                    if(!DeviceManager.getInstance().isUpload(file.getFilePath())) {
                        boolean isUploading = DeviceManager.getInstance().isUploading();
                        // 执行上传
                        mChartAdapter.select(position);
                        mChartAdapter.addUpload(position);
                        if(!isUploading) {
                            DeviceManager.getInstance().startUpload();
                        }
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
                    if(!DeviceManager.getInstance().isUpload(file.getTopImagePath())) {
                        boolean isUploading = DeviceManager.getInstance().isUploading();
                        // 执行上传
                        mListAdapter.select(position);
                        mListAdapter.addUpload(position);
                        if(!isUploading) {
                            DeviceManager.getInstance().startUpload();
                        }
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
                    mPathTxt.setText(AppContext.getInstance().getSString(R.string.bendi_device_txt));
                    mListAdapter.notifyDataSetChanged();
                    if (isRefreshModel) {
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
        if (DeviceManager.getInstance().isUploading()) {
            ToastUtil.showToast(getActivity(), AppContext.getInstance().getSString(R.string.uploading_txt));
            return;
        }
        switch (view.getId()) {
            case R.id.fragment_photo_model_type:
                // 点击切换浏览模式
                new ChooseModePopView(getActivity(), this).showAsDropDown(view, 0, 0);
                break;
            case R.id.fragment_photo_choose_tab:
                // 点击选择
                if(mListLayout.getVisibility() == View.VISIBLE) {
                    // 列表模式
                    if(!mListAdapter.getItem(0).isFolder()) {
                        showSelect();
                    }
                } else {
                    showSelect();
                }
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
                        mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(mListAdapter.clearSelectAll())));
                    } else {
                        mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(mChartAdapter.clearSelectAll())));
                    }
                } else {
                    if(mListLayout.getVisibility() == View.VISIBLE) {
                        mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(mListAdapter.selectAll())));
                    } else {
                        mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(mChartAdapter.selectAll())));
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
                    ToastUtil.showToast(getActivity(), AppContext.getInstance().getSString(R.string.pleace_upload_file_txt));
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
        if(mListLayout.getVisibility() == View.VISIBLE) {
            // 列表模式
            List<FolderBean> uploads = mListAdapter.getSelectFiles();
            boolean start = false;
            boolean isUploading = DeviceManager.getInstance().isUploading();
            for (int i = 0; i < uploads.size(); i++) {
                FolderBean bean = uploads.get(i);
                if(!mListAdapter.isUpload(bean.getTopImagePath())) {
                    // 执行上传
                    start = true;
                    mListAdapter.addUpload(bean.getPosition());
                }
            }
            if(!isUploading && start) {
                DeviceManager.getInstance().startUpload();
            }
        } else {
            // 图表模式
            List<MediaFileBean> uploads = mChartAdapter.getSelectFiles();
            boolean isUploading = DeviceManager.getInstance().isUploading();
            for (int i = 0; i < uploads.size(); i++) {
                MediaFileBean bean = uploads.get(i);
                if(!mChartAdapter.isUpload(bean.getFilePath())) {
                    // 添加上传
                    mChartAdapter.addUpload(bean.getPosition());
                }
            }
            if(!isUploading) {
                DeviceManager.getInstance().startUpload();
            }
        }
    }

    private void deleteFiles() {
        showTipDialog(getActivity(), AppContext.getInstance().getSString(R.string.is_delete_txt), new DialogTips.onDialogOkListenner() {
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
                        mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, "0"));
                    }
                }.execute();
            }
        }, null, false);
    }

    private void showSelect() {
        mChartAdapter.setShowSelectType(true);
        mTabBarLayout.setVisibility(View.GONE);
        mSelectLayout.setVisibility(View.VISIBLE);

        mSelectNumTxt.setText(AppContext.getInstance().getSString(R.string.select_z_txt, String.valueOf(0)));
        mPhotoUploadPopView.showAtLocation(mTabBarLayout, Gravity.BOTTOM, 0, 0);
        if(mListLayout.getVisibility() == View.VISIBLE) {
            mListAdapter.showSelect(true);
        }
    }

    private void hideSelect() {
        mChartAdapter.setShowSelectType(false);
        mTabBarLayout.setVisibility(View.VISIBLE);
        mSelectLayout.setVisibility(View.GONE);

        mPhotoUploadPopView.dismiss();
        mChartAdapter.clearSelect();
        mListAdapter.clearSelect();
        mListAdapter.showSelect(false);
    }

    public boolean isShowChild() {
        boolean bol = false;
        if(mListLayout != null && mListLayout.getVisibility() == View.VISIBLE) {
            if(!mListAdapter.getItem(0).isFolder()) {
                bol = true;
            }
        }
        return bol;
    }

    public void closeShowChild() {
        mPathTxt.setText(AppContext.getInstance().getSString(R.string.bendi_device_txt));
        mListAdapter.clear();
        mListAdapter.appendToList(mPresenter.subGroupOfMedia(mListImages));
        mListAdapter.notifyDataSetChanged();
        mListRecyclerView.scrollToPosition(firstItemPosition);
    }

    public boolean isShowSelect() {
        try {
            return mSelectLayout.getVisibility() == View.VISIBLE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
            mSelectAllTxt.setText(AppContext.getInstance().getSString(R.string.select_uall_txt));
        } else {
            mSelectAllTxt.setText(AppContext.getInstance().getSString(R.string.select_all_txt));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(EventBase eventBase) {
        String action = eventBase.getAction();
        if (action.equals(Constants.SEND_REFRESH_UPLOAD_STATE)) {
            // 刷新上传状态
            UploadInfo uploadInfo = (UploadInfo) eventBase.getData();
            refresh(uploadInfo);
        } else if(action.equals(Constants.SEND_DELETE_FILE_RESULT)) {
            // 刷新下列表
            if(mChartAdapter != null) {
                mChartAdapter.notifyDataSetChanged();
            }
            if(mListAdapter != null) {
                mListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void clear(UploadInfo uploadInfo) {
        if(mListLayout.getVisibility() == View.VISIBLE) {
            for(FolderBean bean : mListAdapter.getList()) {
                if(uploadInfo.getFilePath().equals(bean.getTopImagePath())) {
                    mListAdapter.select(bean.getPosition());
                    return;
                }
            }
        } else {
            for(MediaFileBean bean : mChartAdapter.getList()) {
                if(uploadInfo.getFilePath().equals(bean.getFilePath())) {
                    mChartAdapter.select(bean.getPosition());
                    return;
                }
            }
        }
    }

    private synchronized void refresh(UploadInfo uploadInfo) {
        try {
            ProgressPieView pieView;
            if(mListLayout.getVisibility() == View.VISIBLE) {
                pieView = (ProgressPieView) mListRecyclerView.findViewWithTag("ProgressPieView" + uploadInfo.getFilePath());
            } else {
                pieView = (ProgressPieView) mChartRecyclerView.findViewWithTag("ProgressPieView" + uploadInfo.getFilePath());
            }
            if (pieView != null) {
                int state = uploadInfo.getState();
                if(state == 0) {
                    // 等待上传
                    pieView.setText(AppContext.getInstance().getSString(R.string.download_wait_txt));
                } else if(state == 1) {
                    // 上传中
                    int progress = Integer.parseInt(PubUtils.getSHCollagen(uploadInfo.getTotal(), uploadInfo.getProcessed()));
                    pieView.setProgress(progress);
                    pieView.setText((Math.round(progress * 100) * 1.0f / 100) + "%");
                } else if (state == -1) {
                    // 上传出错
                    pieView.setText(AppContext.getInstance().getSString(R.string.error_txt));
                    clear(uploadInfo);
                } else if (state == 2) {
                    // 上传成功
                    pieView.setText(AppContext.getInstance().getSString(R.string.download_success_txt));
                    if(mListLayout.getVisibility() == View.VISIBLE) {
                        for(FolderBean folderBean : mListAdapter.getList()) {
                            if(folderBean.getTopImagePath().equals(uploadInfo.getFilePath())) {
                                DeviceManager.getInstance().addRemoteFileMap(folderBean.getFolderName());
                                PreferenceUtils.getInstance().putBoolen(folderBean.getTopImagePath(), true);
                                mListAdapter.removeUpload(folderBean.getPosition());
                                mListAdapter.notifyItemChanged(folderBean.getPosition());
                                return;
                            }
                        }
                    } else {
                        for(MediaFileBean mediaFileBean : mChartAdapter.getList()) {
                            if(mediaFileBean.getFilePath().equals(uploadInfo.getFilePath())) {
                                DeviceManager.getInstance().addRemoteFileMap(mediaFileBean.getFileName());
                                PreferenceUtils.getInstance().putBoolen(mediaFileBean.getFilePath(), true);
                                mChartAdapter.removeUpload(mediaFileBean.getPosition());
                                mChartAdapter.notifyItemChanged(mediaFileBean.getPosition());
                                return;
                            }
                        }
                    }
                    clear(uploadInfo);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }

}
