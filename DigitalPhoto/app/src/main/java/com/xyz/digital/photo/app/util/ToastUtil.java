package com.xyz.digital.photo.app.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.xyz.digital.photo.app.AppContext;

/**
 * @author ouyangbo
 * @date 2014-11-13
 * @email ouyangbo@kingnode.com
 * @remark
 * @modify by
 */
public class ToastUtil {
	private static String oldMsg;
	protected static Toast toast = null;
	private static long oneTime = 0;
	private static long twoTime = 0;

	public static void showToast(Context context, String s) {
		try {
			if (toast == null && !TextUtils.isEmpty(s)) {
				toast = Toast.makeText(AppContext.getInstance(), s, Toast.LENGTH_SHORT);
				toast.show();
				oneTime = System.currentTimeMillis();
			} else {
				twoTime = System.currentTimeMillis();
//				if (s.equals(oldMsg)) {
//					if (twoTime - oneTime > Toast.LENGTH_SHORT) {
//						toast.show();
//					}
//				} else {
//					oldMsg = s;
//					toast.setText(s);
//					toast.show();
//				}
				oldMsg = s;
				toast.setText(s);
				toast.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		oneTime = twoTime;
	}

	public static void showToast(Context context, int resId) {
		showToast(context, context.getString(resId));
	}
}
