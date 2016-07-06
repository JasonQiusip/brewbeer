package com.ltbrew.brewbeer.api.longconnection.process.cmdconnection;

import com.ltbrew.brewbeer.api.common.CSSLog;
import com.ltbrew.brewbeer.api.longconnection.process.ManageLongConn;
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2015/6/9.
 */
public class Cmds {

    public static String buildAuthorizeCmd(String token, int seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("auth");
        list.add(seqNo + "");
        list.add(token);
        String fd = ManageLongConn.getInstance().getFd();
        String stToken = ManageLongConn.getInstance().getStToken();
        if(fd != null)
            list.add(fd);
        if(stToken != null)
            list.add(stToken);
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    public static String buildStCmd(int seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("st");
        list.add(seqNo + "");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String buildProbeMssCmd(int seqNo, int reduceCount, ParsePackKits pushServiceKits) {
        byte[] mssByte = new byte[CmdsConstant.MSSMAX - reduceCount * CmdsConstant.DECREASERATE];
        List<String> list = new ArrayList<String>();
        list.add("mss");
        list.add(seqNo + "");
        list.add(new String(mssByte));
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String buildPockCmd(int seqNo, String ackSeqNo, String endQueueNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("pok");
        list.add("" + seqNo);
        list.add(ackSeqNo);
        list.add(endQueueNo);
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    public static String buildHeartRateCmd(long seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("hb");
        list.add(seqNo + "");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    //build pok

}
