package com.ltbrew.brewbeer.api.longconnection.process;

import com.ltbrew.brewbeer.api.longconnection.TransmitCmdService;
import com.ltbrew.brewbeer.api.longconnection.process.cmdconnection.Cmds;
import com.ltbrew.brewbeer.api.longconnection.process.cmdconnection.CmdsConstant;
import com.ltbrew.brewbeer.api.model.UploadParam;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Jason on 2015/6/13.
 * 定义所有的cmd和文件的写操作
 */
public abstract class BaseSocketWriter {

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
    /**************文件socket写和命令socket写都需要做的操作    start***************************/

    public void stopHB() {
        stopHb = true;
    }

    public void hbRecover() {
        //开启心跳包
        synchronized (stopHbSignal) {
            stopHbSignal.notify();
        }
    }

    public void writeAuthorizePack() throws IOException {
        locker.seqNo++;
        String authorizePack = ParsePackKits.makePack(Cmds.buildAuthorizeCmd(authorizeToken, locker.seqNo, this.pushServiceKits));
        System.out.println("writeAuthorizePack： "+ authorizePack);
        writeData(authorizePack);
    }



    public void writeSt() throws IOException {
        locker.seqNo++;
        String st = ParsePackKits.makePack(Cmds.buildStCmd(locker.seqNo, this.pushServiceKits));
        System.out.println("writeSt： "+ st);
        writeData(st);
    }
    /**
     * 发送心跳包
     */
    public void writeHeartbeat() {
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
                        if(socketWriteCb != null)
                            socketWriteCb.onBeforeWriteHb();
                        locker.seqNo++;
                        writeData(ParsePackKits.makePack(Cmds.buildHeartRateCmd(locker.seqNo, pushServiceKits)));
                        System.err.print("hb = " + locker.seqNo);
                        /**
                         *  只有hb读的返回包成功之后才会解除该锁定
                         *  SocketRead{@link com.linktop.LongConn.SocketRead#checkRespPack}
                         */
                        synchronized (hblocker) {
                            hblocker.wait();
                        }
                        Thread.sleep(4*60*1000);
                    } catch (IOException e) {
                        excute = false;
                        if(socketWriteCb != null)
                            socketWriteCb.onWriteHbFailed();
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        excute = false;
                        if(socketWriteCb != null)
                            socketWriteCb.onWriteHbFailed();
                        e.printStackTrace();
                    }
                }
            }
        });
        hbThread.start();
    }

    private void writeData(String data) throws IOException {
        outputStream.write(toByteArr(data));
    }

    public byte[] toByteArr(String str) {
        return ParsePackKits.charToByteArray(str.toCharArray());
    }

    /**************文件socket写和命令socket写都需要做的操作    end***************************/



    /*************方法定义用于外部调用， 目前外部初始化写操作(包括命令socket的写和文件socket的写)的时候都是用的父类   start ********************/
    /**
     * 开始写文件
     *
     * @param uploadParam
     */
    public void setUploadParam(UploadParam uploadParam) {}
    /**
     * 写文件
     *
     * @param file
     */
    public void setFileParts(byte[] file) {}
    /**
     * 写文件结束
     */
    public void setFileEnd() {}

    public void sendFileReqPacks() {}
    //
    public void sendCmdReqPacks() {}
    // push
    public void changeCmdToSendPok(String ackSeqNo, String endQueueNo) throws IOException, InterruptedException {}

    public void changeCmdToSendCmnPrgs(String token) {}

    public void changeCmdToSendBrewSession(Long package_id) {}

    public void checkLastCmnMsg(String pid, String token){}

    public void sendheartreal(String id, String bTime, int linkIndex, int testBindex) throws IOException {}

    public void senHeartHistory(String pid, int endIndex, int whatsday, boolean isZip)throws IOException {}
    /********************方法定义用于外部调用， 目前外部初始化写操作的时候都是用的父类    end********************/
}
