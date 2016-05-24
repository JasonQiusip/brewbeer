package com.ltbrew.brewbeer.service;

import android.os.Parcel;
import android.os.Parcelable;

public class PushMsg implements Parcelable {
    public String cb;
    public String des;
    public String id;
    public int f;
    public String body;
    public int si;
    public int ratio;
    public String st;
    public int ms;

    public PushMsg() {

    }

    protected PushMsg(Parcel in) {
        cb = in.readString();
        des = in.readString();
        id = in.readString();
        f = in.readInt();
        body = in.readString();
        si = in.readInt();
        ratio = in.readInt();
        st = in.readString();
        ms = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cb);
        dest.writeString(des);
        dest.writeString(id);
        dest.writeInt(f);
        dest.writeString(body);
        dest.writeInt(si);
        dest.writeInt(ratio);
        dest.writeString(st);
        dest.writeInt(ms);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PushMsg> CREATOR = new Creator<PushMsg>() {
        @Override
        public PushMsg createFromParcel(Parcel in) {
            return new PushMsg(in);
        }

        @Override
        public PushMsg[] newArray(int size) {
            return new PushMsg[size];
        }
    };
}