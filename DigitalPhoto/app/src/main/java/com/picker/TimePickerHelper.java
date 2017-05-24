package com.picker;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.picker.adapter.NumericWheelAdapter;
import com.picker.widget.WheelView;
import com.xyz.digital.photo.app.R;

import java.util.Calendar;

/**
 * Created by O on 2017/5/24.
 */

public class TimePickerHelper {

    private Activity mActivity;

    private WheelView year;
    private WheelView month;
    private WheelView day;
    private WheelView hour;
    private WheelView mins;

    public TimePickerHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * 显示日期
     */
    public void showDateDialog(final OnPickerCallback callback) {
        final AlertDialog dialog = new AlertDialog.Builder(mActivity).create();
        dialog.show();
        Window window = dialog.getWindow();
        // 设置布局
        window.setContentView(R.layout.view_datapick);
        // 设置宽高
        window.getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setGravity(Gravity.BOTTOM);
        // 设置弹出的动画效果
        window.setWindowAnimations(R.style.signin_dialog_style);

        Calendar c = Calendar.getInstance();
        int curYear = c.get(Calendar.YEAR);
        int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        int curDate = c.get(Calendar.DATE);
        year = (WheelView) window.findViewById(R.id.year);
        initYear(curYear);
        month = (WheelView) window.findViewById(R.id.month);
        initMonth();
        day = (WheelView) window.findViewById(R.id.day);
        initDay(curYear, curMonth);


        year.setCurrentItem(curYear - 1950);
        month.setCurrentItem(curMonth - 1);
        day.setCurrentItem(curDate - 1);
        year.setVisibleItems(7);
        month.setVisibleItems(7);
        day.setVisibleItems(7);

        // 设置监听
        TextView ok = (TextView) window.findViewById(R.id.view_pick_set);
        TextView cancel = (TextView) window.findViewById(R.id.view_pick_cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                String monthS = (month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : String.valueOf(month
                        .getCurrentItem() + 1);
                String dayS = (day.getCurrentItem() + 1) < 10 ? "0" + (day.getCurrentItem() + 1) : String.valueOf(day.getCurrentItem() + 1);
                String str = (year.getCurrentItem() + 1950) + "-" + monthS + "-" + dayS;
                if (callback != null) {
                    callback.onTime(str);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }

    /**
     * 显示时间
     */
    public void showTimeDialog(final OnPickerCallback callback) {
        final AlertDialog dialog = new AlertDialog.Builder(mActivity).create();
        dialog.show();
        Window window = dialog.getWindow();
        // 设置布局
        window.setContentView(R.layout.view_timepick);
        // 设置宽高
        window.getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setGravity(Gravity.BOTTOM);
        // 设置弹出的动画效果
        window.setWindowAnimations(R.style.signin_dialog_style);

        Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY) - 1;
        int curMins = c.get(Calendar.MINUTE) - 1;
        hour = (WheelView) window.findViewById(R.id.hour);
        initHour();
        mins = (WheelView) window.findViewById(R.id.mins);
        initMins();
        // 设置当前时间
        hour.setCurrentItem(curHour);
        mins.setCurrentItem(curMins);
        hour.setVisibleItems(7);
        mins.setVisibleItems(7);

        // 设置监听
        TextView ok = (TextView) window.findViewById(R.id.view_pick_set);
        TextView cancel = (TextView) window.findViewById(R.id.view_pick_cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                String hourS = hour.getCurrentItem() < 10 ? "0" + (hour.getCurrentItem() + 1) : String.valueOf(hour.getCurrentItem() + 1);
                String minsS = mins.getCurrentItem() < 10 ? "0" + (mins.getCurrentItem() + 1) : String.valueOf(mins.getCurrentItem() + 1);
                String str = hourS + ":" + minsS;
                if (callback != null) {
                    callback.onTime(str);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        });
    }

    /**
     * 初始化年
     */
    private void initYear(int curYear) {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(mActivity, 1950, curYear);
        numericWheelAdapter.setLabel(" 年");
        //		numericWheelAdapter.setTextSize(15);  设置字体大小
        year.setViewAdapter(numericWheelAdapter);
        year.setCyclic(true);
    }

    /**
     * 初始化月
     */
    private void initMonth() {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(mActivity, 1, 12, "%02d");
        numericWheelAdapter.setLabel(" 月");
        //		numericWheelAdapter.setTextSize(15);  设置字体大小
        month.setViewAdapter(numericWheelAdapter);
        month.setCyclic(true);
    }

    /**
     * 初始化天
     */
    private void initDay(int arg1, int arg2) {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(mActivity, 1, getDay(arg1, arg2), "%02d");
        numericWheelAdapter.setLabel(" 日");
        //		numericWheelAdapter.setTextSize(15);  设置字体大小
        day.setViewAdapter(numericWheelAdapter);
        day.setCyclic(true);
    }

    /**
     * 初始化时
     */
    private void initHour() {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(mActivity, 1, 23, "%02d");
        numericWheelAdapter.setLabel(" 时");
        //		numericWheelAdapter.setTextSize(15);  设置字体大小
        hour.setViewAdapter(numericWheelAdapter);
        hour.setCyclic(true);
    }

    /**
     * 初始化分
     */
    private void initMins() {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(mActivity, 1, 59, "%02d");
        numericWheelAdapter.setLabel(" 分");
//		numericWheelAdapter.setTextSize(15);  设置字体大小
        mins.setViewAdapter(numericWheelAdapter);
        mins.setCyclic(true);
    }

    /**
     * @param year
     * @param month
     * @return
     */
    private int getDay(int year, int month) {
        int day = 30;
        boolean flag = false;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }

    public interface OnPickerCallback {
        void onTime(String time);
    }

}
