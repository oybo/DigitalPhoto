package com.xyz.digital.photo.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.xyz.digital.photo.app.bean.EventBase;
import com.xyz.digital.photo.app.util.Constants;
import com.xyz.digital.photo.app.view.DialogTips;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by O on 2017/4/6.
 */

public class BaseFragment extends Fragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendRefreshStateMessage();
    }

    protected void showSimpleTipDialog(Context context, String message) {
        try {
            DialogTips dialogTips = new DialogTips(context);
            dialogTips.setMessage(message);
            dialogTips.setOkListenner(null);
            dialogTips.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showTipDialog(Context context, String message, DialogTips.onDialogOkListenner listenner) {
        showTipDialog(context, message, listenner, true);
    }

    protected void showTipDialog(Context context, String message, DialogTips.onDialogOkListenner listenner, boolean isTouchCancel) {

        try {
            DialogTips dialogTips = new DialogTips(context);
            dialogTips.setMessage(message);
            dialogTips.setOkListenner(listenner);
            if(!isTouchCancel) {
                dialogTips.setCancelable(false);
                dialogTips.setCanceledOnTouchOutside(false);
            }
            dialogTips.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showTipDialog(Context context, String message, DialogTips.onDialogOkListenner listenner,
                                 DialogTips.onDialogCancelListenner cancelListenner, boolean isTouchCancel) {
        try {
            DialogTips dialogTips = new DialogTips(context);
            dialogTips.setMessage(message);
            dialogTips.setCancelListenner(cancelListenner);
            dialogTips.setOkListenner(listenner);
            if(!isTouchCancel) {
                dialogTips.setCancelable(false);
                dialogTips.setCanceledOnTouchOutside(false);
            }
            dialogTips.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            sendRefreshStateMessage();
        }
    }

    private void sendRefreshStateMessage() {
        EventBase eventBase = new EventBase();
        eventBase.setAction(Constants.REFRESH_STATUSBAR_COLOR);
        EventBus.getDefault().post(eventBase);
    }
}
