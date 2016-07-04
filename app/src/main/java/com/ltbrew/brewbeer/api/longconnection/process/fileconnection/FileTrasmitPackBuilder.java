package com.ltbrew.brewbeer.api.longconnection.process.fileconnection;

import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;
import com.ltbrew.brewbeer.api.model.FileEnum;
import com.ltbrew.brewbeer.api.model.UploadParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jason on 2015/6/9.
 */
public class FileTrasmitPackBuilder {

    static String buildFileBeginCmd(int seqNo, UploadParam param, ParsePackKits pushServiceKits){
        List<String> list = new ArrayList<String>();
        list.add("file_ul_begin");
        list.add(seqNo+"");
        list.add(param.deviId);
        if(param.share == FileEnum.PRIVATEFILE) {
            list.add("0");
        }else {
            list.add("1");
        }
        list.add(param.fn);
        list.add(param.src);
        list.add(param.usage);
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String
    buildFileParts(int seqNo, byte[] filePart, ParsePackKits pushServiceKits){
        List<String> list = new ArrayList<String>();
        list.add("file_ul");
        list.add(seqNo+"");
        list.add(toString(filePart));
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String
    buildr_hrrPacks(String id, int seqNo, String begineTime, int linkedindex, int testIndex , ParsePackKits pushServiceKits){
        List<String> list = new ArrayList<String>();
        list.add("r_hrr");
        list.add(seqNo + "");
        list.add(id);
        list.add(begineTime);
        list.add(""+linkedindex);
        list.add(""+testIndex);
        String buildRequestString = pushServiceKits.buildRequestString(list);
        System.out.println(buildRequestString);
        return buildRequestString;
    }

    static String
    buildhr_dPacks(String id, int seqNo, int endIndex, int whatsday, boolean iszip , ParsePackKits pushServiceKits){
        List<String> list = new ArrayList<String>();
        list.add("r_hrh");
        list.add(""+seqNo);
        list.add(id);
//        list.add(""+testIndex);
//        if(is6day){
            list.add(""+whatsday);
//        }else{
//            list.add(""+0);
//        }
        if(iszip){
            list.add("-"+1);
        }else{
            list.add("gzip");
        }
        list.add(""+endIndex);
        String buildRequestString = pushServiceKits.buildRequestString(list);
        System.out.println(buildRequestString);
        return buildRequestString;
    }


    static String buildFileEndCmd(int seqNo, int fileBeginSeqNo, ParsePackKits pushServiceKits){
        List<String> list = new ArrayList<String>();
        list.add("file_ul_end");
        list.add(seqNo+"");
        list.add(fileBeginSeqNo+"");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String buildCmnPrgsCmd(long seqNo, String token, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("cmn_prgs");
        list.add(seqNo + "");
        list.add(token);
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

    static String buildCmnMsgLastCmd(long seqNo, String pid, String token, ParsePackKits pushServiceKits) {
        List<String> list = new ArrayList<String>();
        list.add("cmn_msg_last");
        list.add(seqNo + "");
        list.add(pid+"");
        list.add(token+"");
        String buildRequestString = pushServiceKits.buildRequestString(list);
        return buildRequestString;
    }

    static String toString(byte[] filePart) {
        System.out.print(Arrays.toString(filePart));
        return ParsePackKits.byteArrayToStr(filePart).toString();
    }

}
