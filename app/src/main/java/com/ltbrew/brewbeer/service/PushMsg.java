package com.ltbrew.brewbeer.service;

import android.os.Parcel;
import android.os.Parcelable;

public class PushMsg implements Parcelable {
    public String cb;
    public String des;
    public String id;
    public Integer f;
    public String body;
    public Integer si;
    public Integer ratio;
    public String st;

    public PushMsg() {

    }

    protected PushMsg(Parcel in) {
        cb = in.readString();
        des = in.readString();
        id = in.readString();
        body = in.readString();
        st = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cb);
        parcel.writeString(des);
        parcel.writeString(id);
        parcel.writeString(body);
        parcel.writeString(st);
    }
}