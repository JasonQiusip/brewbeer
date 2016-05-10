package com.ltbrew.brewbeer.api.longconnection.process;

import com.ltbrew.brewbeer.api.common.CSSLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2015/6/9.
 */
public class Cmds {

    static String buildAuthorizeCmd(String token, int seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("auth");
        list.add(seqNo + "");
        list.add(token);
        String buildRequestString = pushServiceKits.buildRequestString(list);
        CSSLog.showLog("wuyuan", "buildRequestString = " + buildRequestString);
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

    static String buildHeartRateCmd(long seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("hb");
        list.add(seqNo + "");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String buildCmnPrgsCmd(long seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("cmn_prgs");
        list.add(seqNo + "");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String buildBrewSessionCmd(Long pack_id, long seqNo, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("brew_session");
        list.add(seqNo + "");
        list.add(pack_id+"");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    //build pok

}
