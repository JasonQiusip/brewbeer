package com.ltbrew.brewbeer.uis.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] fragments;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setFragments(Fragment[] fragments){
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }
        @Override
        public int getCount() {
            return fragments.length;
        }

    }