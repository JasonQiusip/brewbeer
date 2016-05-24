package com.ltbrew.brewbeer.api.longconnection.process.fileconnection;

import android.text.TextUtils;
import android.util.Log;

import com.ltbrew.brewbeer.api.common.CSSLog;
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;
import com.ltbrew.brewbeer.api.longconnection.process.SocketCustomWriter;
import com.ltbrew.brewbeer.api.longconnection.process.cmdconnection.CmdsConstant;
import com.ltbrew.brewbeer.api.model.UploadParam;

import java.io.IOException;

/**
 * Created by Jason on 2015/6/13.
 */
public class SocketFileWriter extends SocketCustomWriter {

    private int fileBeginSeqNo;
    private boolean fileUL = false;
    private Object signal = new Object();
    private String token;
    @Override
    public void sendFileReqPacks() {
        while (excute) {
            try {
                //cmd处于idle状态的时候停住该线程
                if (cmdType == CmdsConstant.CMDSTR.idle) {
                    synchronized (signal) {
                        signal.wait();
                    }
                }
                //非idle状态时启动该操作
                fileWriterOperator();
                //将数据从queue中取出写到服务器

                if (queue != null && queue.size() > 0 ) {
                    String dataSend = queue.get(0);
                    if(dataSend == null){
                        queue.remove(0);
                        continue;
                    }

                    Log.e("","===###################sendFileReqPacks####################====\n"+dataSend);

                    outputStream.write(toByteArr(dataSend));
                    queue.remove(0);
                    synchronized (locker) {
                        locker.wait();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void fileWriterOperator() throws IOException {
        switch (cmdType) {
            case auth:
                CSSLog.showLog("fileWriterOperator", "auth");
                writeAuthorizePack();
                cmdType = CmdsConstant.CMDSTR.hb;
                break;
            case hb:
                writeHeartbeat();
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case file_ul_begin:
                break;
            case file_ul:
                break;
            case file_ul_end:
                //队列为空时， 即文件上传结束
                if (queue.size() == 0) {
                    cmdType = CmdsConstant.CMDSTR.idle;
                    super.hbRecover();
                }
                break;
            case abort:
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case r_hrr:
                CSSLog.showLog("lc", "r_hrr");
                break;
            case r_hrh:
                CSSLog.showLog("lc", "r_hrh");
                cmdType = CmdsConstant.CMDSTR.hb;
                break;
            case cmn_prgs:
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case brew_session:
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
        }
    }

    @Override
    public void senHeartHistory(String id, int endIndex, int whatsday, boolean isZip) throws IOException {
        cmdType = CmdsConstant.CMDSTR.r_hrh;
        StringBuffer sb = new StringBuffer();
        getIDHexStr(id, sb);
        locker.seqNo++;
        String idpack = sb.toString();
        String authorizePack = ParsePackKits.makePack(FileTrasmitPackBuilder.buildhr_dPacks(idpack, locker.seqNo, endIndex, whatsday, isZip, this.pushServiceKits));
        outputStream.write(toByteArr(authorizePack));
    }

    private void getIDHexStr(String id, StringBuffer sb) {
        int idStrLenth = id.length();
        int idStrcounts = idStrLenth / 2;
        int n = 0;
        for (int i = 1; i <= idStrcounts; i++) {
            String string = id.substring(n, i * 2);
            n = i * 2;
            int int1 = Integer.valueOf(string, 16);
            char c = (char) int1;
            sb.append(c);
        }
    }

    @Override
    public void sendheartreal(String id, String bTime, int linkIndex, int testBindex) throws IOException {
        cmdType = CmdsConstant.CMDSTR.r_hrr;
        StringBuffer sb = new StringBuffer();
        getIDHexStr(id, sb);
        String idpack = sb.toString();
        locker.seqNo++;
        if (TextUtils.isEmpty(bTime)) {
            int zore = 0x0000;
            char[] z = new char[4];
            z[0] = (char) (zore & 0xff);
            z[1] = (char) ((zore >> 8) & 0xff);
            z[2] = (char) ((zore >> 16) & 0xff);
            z[3] = (char) ((zore >> 24) & 0xff);
            StringBuffer ssb = new StringBuffer();
            for (char ch : z) {
                ssb.append(ch);
            }
            bTime = ssb.toString();
        }
        String requestStr = FileTrasmitPackBuilder.buildr_hrrPacks(idpack, locker.seqNo, bTime, linkIndex, testBindex, this.pushServiceKits);
        String authorizePack = ParsePackKits.makePack(requestStr);
        CSSLog.showLog("writeAuthorizePack", "authorizePack = " + authorizePack);
        outputStream.write(toByteArr(authorizePack));
    }

    //*************************************

    /**
     * 写认证的数据包
     *
     * @throws IOException
     */
    @Override
    public void writeAuthorizePack() throws IOException {
        super.writeAuthorizePack();
    }

    /**
     * 写心跳包
     */
    @Override
    public void writeHeartbeat() {
        super.writeHeartbeat();
    }

    /**
     * 恢复心跳包
     */
    @Override
    public void hbRecover() {
        super.hbRecover();
    }

    //******************************外部调用************************
    //*************************************
    //------------------------------------------开启文件上传

    @Override
    public void setUploadParam(UploadParam uploadParam) {
        super.setUploadParam(uploadParam);
        //停止心跳包
        super.stopHB();
        this.uploadParam = uploadParam;
        cmdType = CmdsConstant.CMDSTR.file_ul_begin;
        //启动线程
        synchronized (signal) {
            signal.notifyAll();
        }
        try {
            writeFileBegin(uploadParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFileBegin(UploadParam uploadParam) throws IOException {
        locker.seqNo++;
        writeData(ParsePackKits.makePack(FileTrasmitPackBuilder.buildFileBeginCmd(locker.seqNo, uploadParam,
                this.pushServiceKits)));
        fileBeginSeqNo = locker.seqNo;
    }

    //------------------------------------------上传文件

    @Override
    public void setFileParts(byte[] file) {
        super.setFileParts(file);
        this.file = file;
        cmdType = CmdsConstant.CMDSTR.file_ul;

        try {
            writeFile(file); //将文件放入伪队列中
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFileEnd(int fileBeginSeqNo) throws IOException {
        fileUL = false;
        locker.seqNo++;
        writeData(ParsePackKits.makePack(FileTrasmitPackBuilder.buildFileEndCmd(locker.seqNo, fileBeginSeqNo,
                this.pushServiceKits)));
    }

    //------------------------------------------上传结束
    @Override
    public void setFileEnd() {
        cmdType = CmdsConstant.CMDSTR.file_ul_end;
        try {
            writeFileEnd(fileBeginSeqNo); //将文件放入伪队列中
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.setFileEnd();
    }

    private void writeFile(byte[] fileParts) throws IOException {
        fileUL = true;
        locker.seqNo++;
        String pack = ParsePackKits.makePack(FileTrasmitPackBuilder.buildFileParts(locker.seqNo, fileParts,
                this.pushServiceKits));
        writeData(pack);
    }
    //------------------------------------------发送brew_session

    @Override
    public void changeCmdToSendBrewSession(Long package_id){
        super.changeCmdToSendBrewSession(package_id);
        cmdType = CmdsConstant.CMDSTR.brew_session;
        sendCmdToCheckBrewSession(package_id); //将文件放入伪队列中
        //启动线程
        synchronized (signal) {
            signal.notifyAll();
        }
    }

    private void sendCmdToCheckBrewSession(Long package_id) {
        synchronized (locker){
            locker.notifyAll();
        }
        locker.seqNo++;
        try {
            String requestStr = FileTrasmitPackBuilder.buildBrewSessionCmd(package_id, locker.seqNo,this.pushServiceKits);
            writeData(ParsePackKits.makePack(requestStr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------- 发送cmn_prgs
    @Override
    public void changeCmdToSendCmnPrgs(String token){
        super.changeCmdToSendCmnPrgs(token);
        this.token = token;
        cmdType = CmdsConstant.CMDSTR.cmn_prgs;
        sendCmdToGetCmnPrgs(token);//将文件放入伪队列中
        //启动线程
        synchronized (signal) {
            signal.notifyAll();
        }
    }

    private void sendCmdToGetCmnPrgs(String token) {
        locker.seqNo++;
        try {
            String requestStr = FileTrasmitPackBuilder.buildCmnPrgsCmd(locker.seqNo,token, this.pushServiceKits);
            writeData(ParsePackKits.makePack(requestStr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------

    private void writeData(String data) throws IOException {
        queue.add(data);
    }

}
