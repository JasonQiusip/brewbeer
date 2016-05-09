package com.ltbrew.brewbeer.presenter.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 151117a on 2016/5/5.
 */
public class Device implements Parcelable{
    String id;
    int p;

    public Device(){}

    protected Device(Parcel in) {
        id = in.readString();
        p = in.readInt();
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(p);
    }
}
