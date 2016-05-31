package com.ltbrew.brewbeer.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 151117a on 2016/5/25.
 */
public class PldForBrewSession implements Parcelable {

    public String packId;
    public String formulaId;
    public int state;

    public PldForBrewSession(){}

    protected PldForBrewSession(Parcel in) {
        packId = in.readString();
        formulaId = in.readString();
        state = in.readInt();
    }

    public static final Creator<PldForBrewSession> CREATOR = new Creator<PldForBrewSession>() {
        @Override
        public PldForBrewSession createFromParcel(Parcel in) {
            return new PldForBrewSession(in);
        }

        @Override
        public PldForBrewSession[] newArray(int size) {
            return new PldForBrewSession[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(packId);
        parcel.writeString(formulaId);
        parcel.writeInt(state);
    }
}
