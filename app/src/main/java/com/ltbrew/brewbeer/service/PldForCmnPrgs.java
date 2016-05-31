package com.ltbrew.brewbeer.service;

import android.os.Parcel;
import android.os.Parcelable;

public class PldForCmnPrgs implements Parcelable {
        public String body;
        public int si;
        public int ratio;
        public String st;

        public PldForCmnPrgs(){}

        protected PldForCmnPrgs(Parcel in) {
            body = in.readString();
            si = in.readInt();
            ratio = in.readInt();
            st = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(body);
            dest.writeInt(si);
            dest.writeInt(ratio);
            dest.writeString(st);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PldForCmnPrgs> CREATOR = new Creator<PldForCmnPrgs>() {
            @Override
            public PldForCmnPrgs createFromParcel(Parcel in) {
                return new PldForCmnPrgs(in);
            }

            @Override
            public PldForCmnPrgs[] newArray(int size) {
                return new PldForCmnPrgs[size];
            }
        };
    }