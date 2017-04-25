package com.xyz.digital.photo.app.bean;

/**
 * Created by O on 2016/11/8.
 */

public class EventBase<T> {

    private String action;

    private T data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
