package com.xyz.digital.photo.app.bean;

/**
 * Created by O on 2017/4/27.
 */

public class ItemSelect {

    private String title;
    private boolean select;

    public ItemSelect(String title, boolean select) {
        this.title = title;
        this.select = select;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
