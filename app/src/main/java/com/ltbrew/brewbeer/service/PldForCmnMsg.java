package com.ltbrew.brewbeer.service;

import android.os.Parcel;
import android.os.Parcelable;

public class PldForCmnMsg implements Parcelable{
        public int ms;

    public PldForCmnMsg(){}
    protected PldForCmnMsg(Parcel in) {
        ms = in.readInt();
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ms);
    }
}