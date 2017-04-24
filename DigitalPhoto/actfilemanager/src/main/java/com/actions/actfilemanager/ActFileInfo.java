package com.actions.actfilemanager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description to a file item.
 */
public class ActFileInfo implements Parcelable {

    public static final int FILE_TYPE_FILE = 1;
    public static final int FILE_TYPE_DIRECTORY = 2;
    public static final int FILE_TYPE_LINK = 3;

    private String mFileName; //file name
    private int mFileType;

    public ActFileInfo() {

    }

    public ActFileInfo(String fileName, int fileType) {
        this.mFileName = fileName;
        this.mFileType = fileType;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public int getFileType() {
        return mFileType;
    }

    public void setFileType(int mFileType) {
        this.mFileType = mFileType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFileName);
        //dest.writeString(mFileThumbUrl);
        //dest.writeInt(mFileSize);
        dest.writeInt(mFileType);
    }

    public static final Creator<ActFileInfo> CREATOR = new Creator() {
        public ActFileInfo createFromParcel(Parcel in) {
            ActFileInfo file = new ActFileInfo();

            file.mFileName = in.readString();
            //file.mFileThumbUrl = in.readString();
            //file.mFileSize = in.readInt();
            file.mFileType = in.readInt();

            return file;
        }

        public ActFileInfo[] newArray(int size) {
            return new ActFileInfo[size];
        }
    };
}
