package com.ltbrew.brewbeer.api.longconnection.process;

import com.ltbrew.brewbeer.api.longconnection.TransmitCmdService;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;

/**
 * Created by Jason on 2015/6/10.
 */
public class CmdTest {
    private static TransmitCmdService service;
    private static boolean send = false;
    public static void main(String[] args){

        service = TransmitCmdService.newInstance("0ec24e36a96b7b0a09a0bcccfc2e7f13ce98dcf02ca852c2151cc8562956d7232dcb9fa658553364b570a023189ddd0e38e46d4b119dd26921c451f59df2ae7a4b4b3d79759fba1c4817166d3afed721f596",
                getFileSocketReadyCallback());
        service.initializeLongConn();
//        System.out.print(PushServiceKits.checkIsInteger(":10\r\n$4\r\nujmd\r\n"));
    }

    private static FileSocketReadyCallback getFileSocketReadyCallback() {
//        return new FileSocketReadyCallback(){
//
//             @Override
//             public void onFileSocketReady() {
//                 System.out.println("onFileSocketReady");
//                if(!send){
////                    UploadParam uploadParam = new UploadParam();
////                    uploadParam.deviId = "c2c00010";
////                    uploadParam.fn = "audio_test";
////                    uploadParam.share = FileEnum.SHAREDFILE;
////                    uploadParam.src = "4";
////                    uploadParam.usage = "voice";
////                    service.beginFileUpload(uploadParam);
////                    send = true;
//                }
//             }
//
//             @Override
//             public void onInitializeLongConnFailed() {
//                 System.out.println("onInitializeLongConnFailed");
//             }
//
//             @Override
//             public void onGetOauthTokenFailed() {
//                 System.out.println("onGetOauthTokenFailed");
//             }
//
//             @Override
//             public void onCmdSocketReady() {
//                 System.out.println("onCmdSocketReady");
//
//             }
//
//             @Override
//             public void onInitFileSocketFailed() {
//                 System.out.println("onInitFileSocketFailed");
//             }
//
//             @Override
//             public void onMaximumFileLength(int length) {
//                 System.out.println("onMaximumFileLength");
//             }
//
//             @Override
//             public void onFileBegined() {
////                 service.closeSocket();
//             }
//
//             @Override
//             public void onFileUploadSuccess() {
//
//             }
//
//             @Override
//             public void onFileUploadFailed() {
//
//             }
//
//             @Override
//             public void onFileUploadEnd() {
//
//             }
//
//            @Override
//            public void onGetPidHeart(ArrayList<Integer> result, String r_hrr_endtime, String linkedIndex, String endindex) {
//
//            }
//
//            @Override
//            public void onCmdHasPush(List<String> pushLists, String pok, long pushTime) {
//
//            }
//
//            @Override
//            public void onGetPidHeartHistory(String hrh_endIndex, HashMap<String, ArrayList<Integer>> maps) {
//
//            }
//
////            @Override
////            public void onGetPidHeartHistory(long beginTime, long endTime, ArrayList<Integer> result) {
////
////            }
//
//            @Override
//            public void onFileSocketReconnect() {
//
//            }
//
//            @Override
//            public void onCmdSocketReconnect() {
//
//            }
//
//            @Override
//            public void onOAuthFailed() {
//
//            }
//        };
        return null;

    }
}
