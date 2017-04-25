package com.xyz.digital.photo.app.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actions.actfilemanager.ACTFileEventListener;
import com.actions.actfilemanager.ActFileInfo;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.DeviceListMediaAdapter;
import com.xyz.digital.photo.app.adapter.DeviceMediaAdapter;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.bean.e.MEDIA_SHOW_TYPE;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.ui.BaseFragment;
import com.xyz.digital.photo.app.ui.activity.MainActivity;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.util.ScreenUtils;
import com.xyz.digital.photo.app.util.ToastUtil;
import com.xyz.digital.photo.app.view.ChooseModePopView;
import com.xyz.digital.photo.app.view.DividerItemDecoration;
import com.xyz.digital.photo.app.view.LoadingView;
import com.xyz.digital.photo.app.view.swipemenulistview.SwipeMenu;
import com.xyz.digital.photo.app.view.swipemenulistview.SwipeMenuCreator;
import com.xyz.digital.photo.app.view.swipemenulistview.SwipeMenuItem;
import com.xyz.digital.photo.app.view.swipemenulistview.SwipeMenuListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by O on 2017/4/12.
 */

public class DevicePhotoFragment extends BaseFragment implements View.OnClickListener, BaseRecyclerAdapter.onInternalClickListener {

    @Bind(R.id.device_photo_model_type) ImageView mModelTypeImage;
    @Bind(R.id.device_media_chart_recyclerview) RecyclerView mChartRecyclerView;
    @Bind(R.id.device_media_list_recyclerview) SwipeMenuListView mListRecyclerView;
    @Bind(R.id.view_loading) LoadingView mLoadingView;
    @Bind(R.id.fragment_photo_image_tab) TextView fragmentPhotoImageTab;
    @Bind(R.id.fragment_photo_video_tab) TextView fragmentPhotoVideoTab;
    @Bind(R.id.fragment_photo_audio_tab) TextView fragmentPhotoAudioTab;
    @Bind(R.id.fragment_photo_all_tab) TextView fragmentPhotoAllTab;
    @Bind(R.id.device_media_list_layout) LinearLayout mListLayout;
    @Bind(R.id.remote_browser_frag_txt_upper) TextView mUpperView;

    /**
     * 显示模式
     */
    private MEDIA_SHOW_TYPE mShowModelType;
    private MEDIA_FILE_TYPE mShowMediaType;

    /**
     * 图表模式
     */
    private DeviceMediaAdapter mChartAdapter;
    /**
     * 列表模式
     */
    private DeviceListMediaAdapter mListAdapter;

    private List<ActFileInfo> mRemoteFileList = new ArrayList<>();

    private static final String PATH = "本机存储:";
    private static String mRemoteCurrentPath = "/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_manager, container, false);
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
        mChartRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "download" item
                menu.addMenuItem(createSwipeMenuItem(Color.parseColor("#C5C7C6"), "下载"));
                // create "delete" item
                menu.addMenuItem(createSwipeMenuItem(Color.parseColor("#C42708"), "删除"));
            }
        };
        // set creator
        mListRecyclerView.setMenuCreator(creator);
        // step 2. listener item click event
        mListRecyclerView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // 下载

                        break;
                    case 1:
                        // 删除

                        break;
                }
            }
        });
        mListRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final ActFileInfo info = DeviceManager.getInstance().getRemoteDeviceFiles().get(position);
                if (info.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
                    // 点击文件夹
                    String requestPath = "";
                    if (mRemoteCurrentPath.equalsIgnoreCase("/")) {
                        requestPath = mRemoteCurrentPath + info.getFileName();
                    } else {
                        requestPath = mRemoteCurrentPath + "/" + info.getFileName();
                    }
//                    actFileManager.browseFiles(requestPath + "/");
                    mRemoteCurrentPath = requestPath;
                    mUpperView.setText(PATH + requestPath);
                } else if (info.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
                    // 点击文件
                }
            }
        });

        mModelTypeImage.setOnClickListener(this);
        fragmentPhotoImageTab.setOnClickListener(this);
        fragmentPhotoVideoTab.setOnClickListener(this);
        fragmentPhotoAudioTab.setOnClickListener(this);
        fragmentPhotoAllTab.setOnClickListener(this);
        getView().findViewById(R.id.device_photo_choose_tab).setOnClickListener(this);
    }

    private void initData() {
        mChartAdapter = new DeviceMediaAdapter(getActivity());
        mChartRecyclerView.setAdapter(mChartAdapter);

        mListAdapter = new DeviceListMediaAdapter(getActivity(), mRemoteFileList, R.layout.item_grid_group_layout);
        mListRecyclerView.setAdapter(mListAdapter);

        mRemoteFileList.addAll(DeviceManager.getInstance().getRemoteDeviceFiles());
        // 默认图表模式
        showModel(MEDIA_SHOW_TYPE.CHART);

        mUpperView.setText(PATH + mRemoteCurrentPath);

        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_download_txt, this);
        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_delete_txt, this);
        mChartAdapter.setOnInViewClickListener(R.id.item_device_media_play_image, this);

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBase eventBase) {
        String action = eventBase.getAction();
        if (action.equals(Constants.REFRESH_DEVICE_FILE)) {
            mRemoteFileList.clear();
            mRemoteFileList.addAll(DeviceManager.getInstance().getRemoteDeviceFiles());
            showFiles(mShowMediaType);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_photo_image_tab:
                // 图片
                showFiles(MEDIA_FILE_TYPE.IMAGE);
                break;
            case R.id.fragment_photo_video_tab:
                // 视频
                showFiles(MEDIA_FILE_TYPE.VIDEO);
                break;
            case R.id.fragment_photo_audio_tab:
                // 音乐
                showFiles(MEDIA_FILE_TYPE.AUDIO);
                break;
            case R.id.fragment_photo_all_tab:
                // 全部
                showFiles(MEDIA_FILE_TYPE.ALL);
                break;
            case R.id.device_photo_choose_tab:
                // 切换设备
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        showLoading();
                    }
                    @Override
                    protected Void doInBackground(Void... voids) {
                        DeviceManager.getInstance().disConnect();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        hideLoading();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra("type", Constants.MAIN_DEVICE_LIST);
                        startActivity(intent);
                    }
                }.execute();
                break;
            case R.id.device_photo_model_type:
                // 点击切换浏览模式
                new ChooseModePopView(getActivity(), this).showAsDropDown(view, 0, 0);
                break;
            case R.id.view_chart_mode:
                // 图表模式
                showModel(MEDIA_SHOW_TYPE.CHART);
                break;
            case R.id.view_list_mode:
                // 列表模式
                showModel(MEDIA_SHOW_TYPE.LIST);
                break;
        }
    }

    public void refresh() {
        mRemoteFileList.clear();
        mRemoteFileList.addAll(DeviceManager.getInstance().getRemoteDeviceFiles());
        // 默认图表模式
        showModel(MEDIA_SHOW_TYPE.CHART);
    }

    private void showModel(MEDIA_SHOW_TYPE type) {
        mShowModelType = type;
        if (type == MEDIA_SHOW_TYPE.CHART) {
            // 图表模式
            mListLayout.setVisibility(View.GONE);
            mChartRecyclerView.setVisibility(View.VISIBLE);
            mModelTypeImage.setImageResource(R.drawable.mode_chrat_icon);
        } else if (type == MEDIA_SHOW_TYPE.LIST) {
            // 列表模式
            mChartRecyclerView.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            mModelTypeImage.setImageResource(R.drawable.mode_list_icon);
        }
        showFiles(MEDIA_FILE_TYPE.IMAGE);
    }

    private void showFiles(MEDIA_FILE_TYPE type) {
        mShowMediaType = type;
        if (type == MEDIA_FILE_TYPE.IMAGE) {
            // 图片
            if (fragmentPhotoImageTab.isSelected()) {
                return;
            }
            setSelectTab(1);
        } else if (type == MEDIA_FILE_TYPE.VIDEO) {
            // 视频
            if (fragmentPhotoVideoTab.isSelected()) {
                return;
            }
            setSelectTab(2);
        } else if (type == MEDIA_FILE_TYPE.AUDIO) {
            // 音乐
            if (fragmentPhotoAudioTab.isSelected()) {
                return;
            }
            setSelectTab(3);
        } else if (type == MEDIA_FILE_TYPE.ALL) {
            // 全部
            if (fragmentPhotoAllTab.isSelected()) {
                return;
            }
            setSelectTab(4);
        }
        refreshAdapter(type);
    }

    private void refreshAdapter(final MEDIA_FILE_TYPE type) {
        new AsyncTask<Void, Void, List<ActFileInfo>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoading();
            }

            @Override
            protected List<ActFileInfo> doInBackground(Void... voids) {
                List<ActFileInfo> result = new ArrayList<ActFileInfo>();
                for(ActFileInfo fileInfo : mRemoteFileList) {
//                    if(fileInfo.getFileType() == 0) {
                        result.add(fileInfo);
//                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<ActFileInfo> result) {
                super.onPostExecute(result);
                hideLoading();
                if(mShowModelType == MEDIA_SHOW_TYPE.CHART) {
                    // 图表模式
                    mChartAdapter.clear();
                    mChartAdapter.appendToList(result);
                    mChartAdapter.notifyDataSetChanged();
                } else {
                    // 列表模式

                }
            }
        }.execute();
    }

    private void setSelectTab(int id) {
        fragmentPhotoImageTab.setSelected(false);
        fragmentPhotoVideoTab.setSelected(false);
        fragmentPhotoAudioTab.setSelected(false);
        fragmentPhotoAllTab.setSelected(false);
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
            case 4:
                fragmentPhotoAllTab.setSelected(true);
                break;
        }
    }

    @Override
    public void OnClickListener(View parentV, View v, Integer position) {
        switch (v.getId()) {
            case R.id.item_device_media_download_txt:

                ToastUtil.showToast(getActivity(), "下载");
                break;
            case R.id.item_device_media_delete_txt:

                ToastUtil.showToast(getActivity(), "删除");
                break;
            case R.id.item_device_media_play_image:

                ToastUtil.showToast(getActivity(), "播放");
                break;
        }
    }

    private void showLoading() {
        mLoadingView.show();
    }

    private void hideLoading() {
        mLoadingView.hide();
    }

    private SwipeMenuItem createSwipeMenuItem(int color, String txt) {
        SwipeMenuItem swipeMenuItem = new SwipeMenuItem(getActivity().getApplicationContext());
        // set item background
        swipeMenuItem.setBackground(new ColorDrawable(color));
        // set item width
        swipeMenuItem.setWidth(ScreenUtils.dpToPxInt(80));
        // set a icon
        // set a txt
        swipeMenuItem.setTitleSize(16);
        swipeMenuItem.setTitleColor(Color.BLACK);
        swipeMenuItem.setTitle(txt);

        return swipeMenuItem;
    }

    public class MyACTFileEventListener implements ACTFileEventListener {
        @Override
        public void onOperationProgression(int opcode, int processed, int total) {
        }

        @Override
        public void onUploadCompleted(String remotePath, String localPath, int result) {
        }

        @Override
        public void onDownloadCompleted(String remotePath, String localPath, int result) {
        }

        @Override
        public void onDeleteCompleted(String parentPath, int result) {
        }

        @Override
        public void onBrowseCompleted(Object filelist, String currentPath, int result) {
            if (result == ACTFileEventListener.OPERATION_SUCESSFULLY) {
                List<ActFileInfo> remoteFileList = (ArrayList) filelist;
                if (remoteFileList != null) {
                    mRemoteFileList.clear();
                    mRemoteFileList.addAll(remoteFileList);
                    showModel(MEDIA_SHOW_TYPE.CHART);
                }
            }
        }

        @Override
        public void onDeleteDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onCreateDirectoryCompleted(String parentPath, int result) {
        }

        @Override
        public void onDisconnectCompleted(int result) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
