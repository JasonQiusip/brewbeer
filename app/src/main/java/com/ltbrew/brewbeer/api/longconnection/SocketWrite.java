package com.ltbrew.brewbeer.api.longconnection;

import com.ltbrew.brewbeer.api.longconnection.process.CommonParam;
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;
import com.ltbrew.brewbeer.api.longconnection.process.ReqType;
import com.ltbrew.brewbeer.api.longconnection.process.SocketCmdWriter;
import com.ltbrew.brewbeer.api.longconnection.process.SocketCustomWriter;
import com.ltbrew.brewbeer.api.longconnection.process.SocketFileWriter;
import com.ltbrew.brewbeer.api.model.UploadParam;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Created by Jason on 2015/6/10.
 */
public class SocketWrite implements Runnable {

    private final String authorizeToken;
    private final OutputStream outputStream;
    private final ReqType type;
    private SocketCustomWriter socketWritter;

    private CommonParam locker;
    private Object hblocker = new Object();
    private final ParsePackKits pushServiceKits;

    public SocketWrite(OutputStream outputStream, String authorizeToken, ReqType type) {
        this.outputStream = outputStream;
        this.authorizeToken = authorizeToken;
        this.pushServiceKits = new ParsePackKits();
        this.type = type;
        if (this.type == ReqType.file) {
            socketWritter = new SocketFileWriter();
        } else {
            socketWritter = new SocketCmdWriter();
        }
        socketWritter.outputStream = this.outputStream;
        socketWritter.pushServiceKits = this.pushServiceKits;
        socketWritter.authorizeToken = this.authorizeToken;
    }

    public void setLocker(CommonParam locker) {
        this.locker = locker;
        socketWritter.locker = locker;
    }

    public void sethbLocker(Object locker) {
        this.hblocker = locker;
        socketWritter.hblocker = locker;
    }

    public void register(TransmitCmdService.SocketWriteCallback callback) {
        socketWritter.socketWriteCb = callback;
    }

    public void setExcute(boolean excute) {
        socketWritter.setExcute(excute);
    }



    /*************************************************************************************
     ***********************************   需要外部调用的命令  ***************************
     *************************************************************************************/

    /**
     * 文件开始上传
     *
     * @param uploadParam
     */
    public void setUploadParam(UploadParam uploadParam) {
        socketWritter.setUploadParam(uploadParam);
    }

    /**
     * 文件上传
     *
     * @param file
     */
    public void setFileParts(byte[] file) {
        socketWritter.setFileParts(file);
    }

    /**
     * 请求心跳数据
     * @param pid
     * @throws IOException
     */
    public void sendHeartRr(String pid, String bTime, int linkIndex, int testBindex) throws IOException {
        socketWritter.sendheartreal(pid,bTime,linkIndex,testBindex);
    }

    public void senHeartHistory(String pid, int endIndex, int whats6day, boolean isZip) throws IOException {
        socketWritter.senHeartHistory(pid, endIndex,whats6day,isZip);
    }

    /**
     * 文件上传结束
     */
    public void setFileEnd() {
        socketWritter.setFileEnd();
    }



    /**
     * 文件终止上传
     */
    public void setAbort(){

    }

    public void sendPok(String ackSeqNo, String endQueueNo) throws IOException, InterruptedException {
        socketWritter.changeCmdToSendPok(ackSeqNo, endQueueNo);
    }

    public void sendCmnPrgsCmd(String token){
        socketWritter.changeCmdToSendCmnPrgs(token);
    }

    public void sendBrewSessionCmd(Long package_id){
        socketWritter.changeCmdToSendBrewSession(package_id);
    }

    /************************************************************************************
     * *********************************   需要外部调用的命令  ****************************
     *************************************************************************************/

    @Override
    public void run() {
        if (type == ReqType.cmd) {
            System.out.println(new Date().toString() + "WRITE" + "HashCODE  CMD:#################################" + this.hashCode() + "#################################");
            //发送cmd请求包, 在传输cmd的长连接发送
            socketWritter.sendCmdReqPacks();
        } else if (type == ReqType.file) {
            System.err.println(new Date().toString() + "WRITE" + "HashCODE  FILE:>>>>>>>>>>>>>>>>>>>>>>>>" + this.hashCode() + "<<<<<<<<<<<<<<<<<<<<<<<<<");
            //发送文件请求包， 在传输文件的长连接发送
            socketWritter.sendFileReqPacks();
        }
    }

}
