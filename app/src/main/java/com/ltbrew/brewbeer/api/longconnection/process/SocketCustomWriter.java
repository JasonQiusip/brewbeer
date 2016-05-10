package com.ltbrew.brewbeer.api.longconnection.process;

import com.ltbrew.brewbeer.api.common.CSSLog;
import com.ltbrew.brewbeer.api.longconnection.TransmitCmdService;
import com.ltbrew.brewbeer.api.model.UploadParam;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Jason on 2015/6/13.
 * 定义所有的cmd和文件的写操作
 */
public abstract class SocketCustomWriter {

    public CommonParam locker;
    public OutputStream outputStream;
    public ParsePackKits pushServiceKits;
    public TransmitCmdService.SocketWriteCallback socketWriteCb;
    public CmdsConstant.CMDSTR cmdType = CmdsConstant.CMDSTR.auth;
    public UploadParam uploadParam;
    public ArrayList<String> queue = new ArrayList<String>();
    public byte[] file;
    public Object hblocker;
    public String authorizeToken;
    public boolean excute = true;
    private Object stopHbSignal = new Object();
    public boolean stopHb = false;

    public void setExcute(boolean excute) {
        synchronized (hblocker) {
            hblocker.notifyAll();
        }
        this.excute = excute;
    }

    /**
     * 开始写文件
     *
     * @param uploadParam
     */
    public void setUploadParam(UploadParam uploadParam) {
    }

    /**
     * 写文件
     *
     * @param file
     */
    public void setFileParts(byte[] file) {
    }

    /**
     * 写文件结束
     */
    public void setFileEnd() {

    }


    // write pok

    public void stopHB() {
        stopHb = true;
    }

    public void hbRecover() {
        //开启心跳包

        synchronized (stopHbSignal) {
            stopHbSignal.notify();
        }
    }

    void writeAuthorizePack() throws IOException {
        locker.seqNo++;
        String authorizePack = ParsePackKits.buildPack(Cmds.buildAuthorizeCmd(authorizeToken, locker.seqNo, this.pushServiceKits));
        System.out.println("writeAuthorizePack： "+ authorizePack);
        outputStream.write(toByteArr(authorizePack));
    }

    /**
     * 发送心跳包
     */
    void writeHeartbeat() {
        Thread hbThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (excute) {
                    try {
                        if (stopHb) {
                            synchronized (stopHbSignal) {
                                stopHbSignal.wait();

                            }
                        }
                        locker.seqNo++;
                        outputStream.write(toByteArr(ParsePackKits.buildPack(Cmds.buildHeartRateCmd(locker.seqNo, pushServiceKits))));
                        System.err.print("hb = " + locker.seqNo);
                        /**
                         *  只有读的返回包成功之后才会解除该锁定
                         *  SocketRead{@link com.linktop.LongConn.SocketRead#checkRespPack}
                         */
                        synchronized (hblocker) {
                            hblocker.wait();
                        }
                        Thread.sleep(300000);
                    } catch (IOException e) {
                        excute = false;
                        socketWriteCb.onWriteHbFailed();
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        excute = false;
                        socketWriteCb.onWriteHbFailed();
                        e.printStackTrace();
                    }
                }
            }
        });
        hbThread.start();
    }

    public byte[] toByteArr(String str) {
        return ParsePackKits.charToByteArray(str.toCharArray());
    }

    //
    public void writeFileBegin(UploadParam uploadParam) throws IOException {
    }

    public void writeFileEnd(int fileBeginSeqNo) throws IOException {
    }

    public void writeFile(byte[] fileParts) throws IOException {
    }

    public void sendFileReqPacks() {
    }

    //
    public void sendCmdReqPacks() {
    }

    // push
    public void changeCmdToSendPok(String ackSeqNo, String endQueueNo) throws IOException, InterruptedException {
    }
    public void changeCmdToSendCmnPrgs(String token) {
    }
    public void changeCmdToSendBrewSession(Long package_id) {
    }
    
    public void sendheartreal(String id, String bTime, int linkIndex, int testBindex) throws IOException {
    }
    public void senHeartHistory(String pid, int endIndex, int whatsday, boolean isZip)throws IOException {
    }

}
