package com.xyz.digital.photo.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xyz.digital.photo.app.util.EnvironmentUtil;
import com.xyz.digital.photo.app.util.PreferenceUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 * 
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String IS_SAVE_CRASH_LOG = "is_save_carsh_log";
	private static CrashHandler INSTANCE = new CrashHandler();// CrashHandler实例
	private Context mContext;// 程序的Context对象

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {

	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		PreferenceUtils.getInstance().remove(IS_SAVE_CRASH_LOG);
		mContext = context;
		Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
	}

	/**
	 * 当UncaughtException发生时会转入该重写的方法来处理
	 */
	public void uncaughtException(Thread thread, final Throwable ex) {
		ex.printStackTrace();
		// 1秒钟后重启应用
		boolean isReboot = true;
		try {
			String carchTimeStr = PreferenceUtils.getInstance().getString(IS_SAVE_CRASH_LOG + "_Time", "");
			if(!TextUtils.isEmpty(carchTimeStr)) {
				long carchTime = Long.parseLong(carchTimeStr);
				if(System.currentTimeMillis() - carchTime < 60000) {
					// 如果重启间隔少于1分钟，则不重启了。
					isReboot = false;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(isReboot) {
			Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
			AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1500, restartIntent);
		}

		String ss = PreferenceUtils.getInstance().getString(IS_SAVE_CRASH_LOG, "");
		if (TextUtils.isEmpty(ss)) {
			new Thread() {
				public void run() {
					// 保存日志文件
					saveCrashInfo2File(ex);
					Looper.prepare();
					Toast.makeText(mContext, "很抱歉,程序出现异常,需要重启应用", Toast.LENGTH_SHORT).show();;
					Looper.loop();
				}
			}.start();
			PreferenceUtils.getInstance().putString(IS_SAVE_CRASH_LOG, "save");
			PreferenceUtils.getInstance().putString(IS_SAVE_CRASH_LOG + "_Time", String.valueOf(System.currentTimeMillis()));
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		// 退出程序
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private String saveCrashInfo2File(Throwable ex) {
		Log.e("error", ex.toString());
		StringBuffer sb = new StringBuffer();

		String errPath = getDefaultLogFileName(mContext);
		File f = new File(errPath);
		try {
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();

				sb.append("test device");
			}
			sb.append("\r\n\r\n\r\n\r\ncrash==================================================\r\n");


			sb.append("\r\n user: " + "ZXF" + " \r\n");

			Writer result = new StringWriter();
			PrintWriter printWriter = new PrintWriter(result);
			ex.printStackTrace(printWriter);
			sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ":\r\n\r\n");
			sb.append(result.toString().replace("\n", "\r\n"));
			sb.append("\r\n\r\n");

			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(errPath, true);
			writer.write(sb.toString());
			printWriter.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 日志保存目录
	 * 
	 * @return
	 */
	private String getDefaultLogFileName(Context mContext) {
		return EnvironmentUtil.getLogPath() + "/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt";
	}
}
