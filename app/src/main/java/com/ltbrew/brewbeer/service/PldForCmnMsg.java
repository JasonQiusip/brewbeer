package com.ltbrew.brewbeer.service;

import android.os.Parcel;
import android.os.Parcelable;

public class PldForCmnMsg implements Parcelable{
        public int ms;
    public String tk;
    public String id;

    public PldForCmnMsg(){}

    protected PldForCmnMsg(Parcel in) {
        ms = in.readInt();
        tk = in.readString();
        id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ms);
        dest.writeString(tk);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PldForCmnMsg> CREATOR = new Creator<PldForCmnMsg>() {
        @Override
        public PldForCmnMsg createFromParcel(Parcel in) {
            return new PldForCmnMsg(in);
        }

        @Override
        public PldForCmnMsg[] newArray(int size) {
            return new PldForCmnMsg[size];
        }
    };

    @Override
    public String toString() {
        return "PldForCmnMsg{" +
                "ms=" + ms +
                '}';
    }
}