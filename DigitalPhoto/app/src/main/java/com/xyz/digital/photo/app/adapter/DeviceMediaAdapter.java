package com.xyz.digital.photo.app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

import static com.xyz.digital.photo.app.manager.DeviceManager.mRemoteCurrentPath;

/**
 * Created by O on 2017/3/18.
 */

public class DeviceMediaAdapter extends BaseRecyclerAdapter<FileInfo> {

    public DeviceMediaAdapter(Context ctx) {
        super(ctx);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_device_media_layout;
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, FileInfo item) {

        holder.setText(R.id.item_device_media_title_txt, item.getFileName());

//        holder.setText(R.id.item_device_media_date_txt, item.getDate());
//
//        holder.setText(R.id.item_device_media_size_txt, item.getSize());

        ImageView imageView = holder.getImageView(R.id.item_device_media_imange);

        // 是否添加到了播放
        ImageView playImage = holder.getImageView(R.id.item_device_media_play_image);
        String remotePath = mRemoteCurrentPath + item.getFileName();

        // 加载图片
        String tempFile = DeviceManager.getInstance().getTempFile(remotePath);
        if(!TextUtils.isEmpty(tempFile)) {
            ImageLoadManager.setImage(tempFile, imageView);
        } else {
            // 图片
            if (item.getType() == MEDIA_FILE_TYPE.AUDIO) {
                imageView.setImageResource(R.drawable.defult_audio_icon);
            } else if (item.getType() == MEDIA_FILE_TYPE.VIDEO) {
                imageView.setImageResource(R.drawable.defult_video_icon);
            } else {
                imageView.setImageResource(R.drawable.defult_image_icon);
            }
        }

        if(DeviceManager.getInstance().isPlay(remotePath)) {
            playImage.setImageResource(R.drawable.media_pause_icon);
        } else {
            playImage.setImageResource(R.drawable.media_play_icon);
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
                        pieView.setText("等待");
                        break;
                }
            }
        } else {
            pieView.setVisibility(View.GONE);
        }

        // 判断是否下载完成
        TextView download = holder.getTextView(R.id.item_device_media_download_txt);
        boolean isUpload = PreferenceUtils.getInstance().getBoolean(localPath, false);
        if (isUpload) {
            pieView.setText("成功");
            pieView.setVisibility(View.GONE);
            download.setSelected(true);
            download.setText("已下载");
        } else {
            download.setSelected(false);
            download.setText("下载");
        }

    }

    public boolean isDownload(String filePath) {
        return DeviceManager.getInstance().isDownload(filePath);
    }

    public void addDownload(int pos) {
        FileInfo bean = getItem(pos);
        DeviceManager.getInstance().addDownload(bean.getFileName(), bean.getType());
        notifyItemChanged(pos);
    }

}
