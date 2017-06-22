package com.xyz.digital.photo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 文件路径工具类
 * 
 * @author ouyangbo
 * 
 */
public class FilePathUtils {

	/** 存放HTML压缩包解压路径 */
	public static final String UNZIP_PATH_NAME = "unZip";
	/** 数据库文件路径 */
	public static final String DB_PATH_NAME = "db";
	/** 下载文件路径 */
	public static final String FILE_PATH_NAME = "file";
	/** 保存图片路径 */
	public static final String IMAGE_PATH_NAME = "image";
	/** 音频路径 */
	public static final String RECORD_PATH_NAME = "record";
	/** 视频路径 */
	public static final String VIDEO_PATH_NAME = "video";
	/** 日志文件路径 */
	public static final String LOG_PATH_NAME = "log";
	/** 网络请求日志文件路径 */
	public static final String NET_LOG_PATH_NAME = "net_log";
	/** 临时文件路径 */
	public static final String TEMP_PATH_NAME = "temp";

	private Context mContext;
	private static FilePathUtils mInstance;

	public static FilePathUtils getInstance() {
		if (mInstance == null) {
			synchronized (FilePathUtils.class) {
				if (mInstance == null) {
					mInstance = new FilePathUtils(AppContext.getInstance());
				}
			}
		}

		return mInstance;
	}

	private FilePathUtils(Context context) {
		mContext = context.getApplicationContext();
	}

	/**
	 * 获得当前应用默认的解压路径
	 * 
	 * @return
	 */
	public String getDefaultUnzipFile() {
		return getPath(UNZIP_PATH_NAME);
	}

	/**
	 * 获取SD卡目录下相对应包名程序下的数据库的路径
	 * 
	 * @return
	 */
	public String getDefaultDataBasePath() {
		return getPath(DB_PATH_NAME);
	}

	public String getDefaultTempZipFilePath() {
		return getPath(TEMP_PATH_NAME);
	}

	public String getDownloadPath() {
		String 	downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download";
		return downloadPath;
	}

	/**
	 * 获取SD卡目录下相对应包名程序下的文件保存的图片的路径
	 * 
	 * @return
	 */
	public String getDefaultFilePath() {
		return getPath(FILE_PATH_NAME);
	}

	/**
	 * 获取SD卡目录下相对应包名程序下的拍照保存的图片的路径
	 * 
	 * @return
	 */
	public String getDefaultImagePath() {
		return getPath(IMAGE_PATH_NAME);
	}

	/**
	 * 获取SD卡目录下相对应包名程序下的录音保存的图片的路径
	 * 
	 * @return
	 */
	public String getDefaultRecordPath() {
		return getPath(RECORD_PATH_NAME);
	}

	/**
	 * 获取SD卡目录下相对应包名程序下的视频保存的图片的路径
	 * 
	 * @return
	 */
	public String getDefaultVideoPath() {
		return getPath(VIDEO_PATH_NAME);
	}

	/**
	 * 日志保存目录
	 * 
	 * @return
	 */
	public String getDefaultLogPath() {
		return getPath(LOG_PATH_NAME);
	}
	
	/**
	 * 日志保存目录
	 * 
	 * @return
	 */
	public String getDefaultNetLogPath() {
		return getPath(NET_LOG_PATH_NAME);
	}

	/**
	 * 获得SD卡上缓存目录下文件夹路径
	 * 
	 * @param dirName
	 * @return
	 */
	public String getPath(String dirName) {
		File cacheDir = getDiskCacheDir(dirName);
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return cacheDir.getAbsolutePath();
	}

	/**
	 * 根据传入的uniqueName获取硬盘缓存的路径地址。
	 */
	@SuppressLint("NewApi")
	private File getDiskCacheDir(String uniqueName) {
		String cachePath = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				&& !Environment.isExternalStorageRemovable()) {
			// 默认第一 SD卡/Android/files目录下
			try {
				cachePath = mContext.getExternalFilesDir(null).getAbsolutePath();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (TextUtils.isEmpty(cachePath)) {
				// 默认第二 data/data/包名/files目录下
				try {
					cachePath = mContext.getFilesDir().getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (TextUtils.isEmpty(cachePath)) {
				// 默认第三 SD卡/Android/cache目录下
				try {
					cachePath = mContext.getExternalCacheDir().getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (TextUtils.isEmpty(cachePath)) {
				// 默认第四 data/data/包名/cache目录下
				try {
					cachePath = mContext.getCacheDir().getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			// 默认第四 data/data/包名/cache目录下
			try {
				cachePath = mContext.getCacheDir().getAbsolutePath();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new File(cachePath + File.separator + uniqueName);
	}

	@SuppressLint("NewApi")
	public static void deleteAllCacel() {
		try {
			String cachePath;
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
					&& !Environment.isExternalStorageRemovable()) {
				cachePath = AppContext.getInstance().getExternalCacheDir().getPath();
			} else {
				cachePath = AppContext.getInstance().getCacheDir().getPath();
			}
			deletefile(cachePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除某个文件夹下的所有文件夹和文件
	 * 
	 * @param delpath
	 *            String
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return boolean
	 */
	public static boolean deletefile(String delpath) throws Exception {
		try {
			File file = new File(delpath);
			// 当且仅当此抽象路径名表示的文件存在且 是一个目录时，返回 true
			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File delfile = new File(delpath + "/" + filelist[i]);
					if (!delfile.isDirectory()) {
						delfile.delete();
						System.out.println(delfile.getAbsolutePath() + "删除文件成功");
					} else if (delfile.isDirectory()) {
						deletefile(delpath + "/" + filelist[i]);
					}
				}
				System.out.println(file.getAbsolutePath() + "删除成功");
				file.delete();
			}

		} catch (FileNotFoundException e) {
		}
		return true;
	}

}
