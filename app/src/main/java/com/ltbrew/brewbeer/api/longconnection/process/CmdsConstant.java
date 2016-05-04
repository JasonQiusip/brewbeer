package com.ltbrew.brewbeer.api.longconnection.process;

/**
 * Created by Jason on 2015/6/9.
 */
public interface CmdsConstant {
    public static final int MSSMAX = 1460;
    public static final int DECREASERATE = 10;

    enum CMDSTR{
        auth,
        mss,
        hb,
        file_ul_begin,
        file_ul,
        file_ul_end,
        idle,
        abort,
        push,
        pok,
        sendPok,
        re_push,
        r_hrr,
        r_hrh,
        kick
    }
}
