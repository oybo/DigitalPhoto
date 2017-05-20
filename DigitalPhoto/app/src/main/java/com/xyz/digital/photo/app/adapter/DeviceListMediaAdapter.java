package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.actions.actfilemanager.ActFileInfo;
import com.xyz.digital.photo.app.AppContext;
import com.xyz.digital.photo.app.R;
import com.xyz.digital.photo.app.adapter.base.BaseRecyclerAdapter;
import com.xyz.digital.photo.app.adapter.base.RecyclerViewHolder;
import com.xyz.digital.photo.app.bean.DownloadInfo;
import com.xyz.digital.photo.app.bean.FileInfo;
import com.xyz.digital.photo.app.bean.e.MEDIA_FILE_TYPE;
import com.xyz.digital.photo.app.manager.DeviceManager;
import com.xyz.digital.photo.app.manager.ImageLoadManager;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import com.xyz.digital.photo.app.util.PubUtils;
import com.xyz.digital.photo.app.view.ProgressPieView;

import java.io.File;

/**
 * Created by O on 2017/3/18.
 */

public class DeviceListMediaAdapter extends BaseRecyclerAdapter<FileInfo> {


    public DeviceListMediaAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_grid_group_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, FileInfo item) {

        holder.setText(R.id.item_list_time_txt, item.getmModifyTime());

        holder.setText(R.id.item_list_size_txt, PubUtils.formatFileLen(item.getmFileSize()));

        holder.setText(R.id.item_list_title_txt, item.getFileName());

        ImageView imageView = holder.getImageView(R.id.item_list_image);

        ImageView playImage = holder.getImageView(R.id.item_child_arrows_image);
        if (item.getFileType() == ActFileInfo.FILE_TYPE_DIRECTORY) {
            // 文件夹
            playImage.setImageResource(R.drawable.btn_home_counterattack);

            imageView.setImageResource(R.drawable.folder);
            holder.getView(R.id.item_menu_download_bt).setVisibility(View.GONE);
        } else if (item.getFileType() == ActFileInfo.FILE_TYPE_FILE) {
            // 文件
            // 是否添加到了播放
            String remotePath = DeviceManager.getInstance().getRemotePath(item.getFileName());
            if(DeviceManager.getInstance().isPlay(remotePath)) {
                playImage.setImageResource(R.drawable.media_pause_icon);
            } else {
                playImage.setImageResource(R.drawable.media_play_icon);
            }

            // 图片
            String tempFile = PubUtils.getTempLocalPath(item.getFileName(), true);
            if(TextUtils.isEmpty(tempFile)) {
                if(item.getType() == MEDIA_FILE_TYPE.AUDIO) {
                    tempFile = "123.mp3";
                } else if(item.getType() == MEDIA_FILE_TYPE.VIDEO) {
                    tempFile = "123.mp4";
                }
            }
            ImageLoadManager.setImage(tempFile, imageView);

            holder.getView(R.id.item_menu_download_bt).setVisibility(View.VISIBLE);
        }

        // 是否在下载
        ProgressPieView pieView = (ProgressPieView) holder.getView(R.id.item_child_download_progress);
        pieView.setTag("ProgressPieView" + PubUtils.getDonwloadLocalPath(item.getFileName(), item.getType()));

        String localPath = PubUtils.getDonwloadLocalPath(item.getFileName(), item.getType());
        if (isDownload(localPath)) {
            pieView.setVisibility(View.VISIBLE);
            DownloadInfo downloadInfo = DeviceManager.getInstance().getDownloadInfo(localPath);
            if (downloadInfo != null) {
                switch (downloadInfo.getState()) {
                    case 0:
                        pieView.setText(AppContext.getInstance().getSString(R.string.download_wait_txt));
                        break;
                }
            }
        } else {
            pieView.setVisibility(View.GONE);
        }

        // 判断是否下载完成
        Button downloadBt = holder.getButton(R.id.item_menu_download_bt);
        boolean isUpload = PreferenceUtils.getInstance().getBoolean(localPath, false);
        if (isUpload && new File(localPath).exists()) {
            pieView.setText(AppContext.getInstance().getSString(R.string.download_success_txt));
            pieView.setVisibility(View.GONE);
            downloadBt.setText(AppContext.getInstance().getSString(R.string.download_finish_txt));
        } else {
            downloadBt.setText(AppContext.getInstance().getSString(R.string.download_download_txt));
        }

    }

    public boolean isDownload(String filePath) {
        return DeviceManager.getInstance().isDownload(filePath);
    }

    public void addDownload(int pos) {
        FileInfo bean = getItem(pos);
        DeviceManager.getInstance().addDownload(bean.getFileName(), bean.getType());
        notifyDataSetChanged();
    }

}
