package com.ltbrew.brewbeer.api.longconnection;

import android.util.Log;

import com.ltbrew.brewbeer.api.common.CSSLog;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;
import com.ltbrew.brewbeer.api.longconnection.process.cmdconnection.CmdsConstant;
import com.ltbrew.brewbeer.api.longconnection.process.CommonParam;
import com.ltbrew.brewbeer.api.longconnection.process.ManageLongConnIp;
import com.ltbrew.brewbeer.api.longconnection.process.ReqType;
import com.ltbrew.brewbeer.api.model.UploadParam;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Jason on 2015/6/9.
 */
public class TransmitCmdService {

    private static final boolean DEBUG = true;
    private final String authorizeToken;
    private ExecutorService pool;
    private TransmitFileService transmitFileService;
    private InetAddress serverAddress;
    private CommonParam cmdSocketLocker = new CommonParam();
    private Object hbLocker = new Object();
    private Socket cmdSocket;
    private OutputStream cmdOutputStream;
    private static TransmitCmdService cmdService;
    private FileSocketReadyCallback fileSocketReadyCallback;
    private SocketRead cmdRead;
    private SocketWrite cmdsWrite;
    private String ip;
    private int port;

    public static TransmitCmdService newInstance(String authorizeToken, FileSocketReadyCallback fileSocketReadyCallback) {
        if (cmdService == null) {
            cmdService = new TransmitCmdService(authorizeToken, fileSocketReadyCallback);
        }
        return cmdService;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void nullCmdTransmitService() {
        cmdService = null;
    }

    private TransmitCmdService(String authorizeToken, FileSocketReadyCallback fileSocketReadyCallback) {
        this.authorizeToken = authorizeToken;
        this.fileSocketReadyCallback = fileSocketReadyCallback;
    }

    private void newPool() {
        pool = Executors.newFixedThreadPool(4);
    }

    public boolean initializeCmdLongConn() {
        try {
            initCmdTransmitSocket();
            startCmdWriteRead(cmdSocket, cmdOutputStream, cmdSocketLocker);
        } catch (IOException e) {
            ManageLongConnIp.getInstance().switchPort();
            fileSocketReadyCallback.onInitializeLongConnFailed();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initCmdTransmitSocket() throws IOException {
//        if(ManageLongConnIp.ipHost == null)
//        serverAddress = InetAddress.getByName(ManageLongConnIp.getInstance().ipHost); // "27.154.54.242"
//        cmdSocket = new Socket(serverAddress, ManageLongConnIp.getInstance().port); //25712
//        serverAddress = InetAddress.getByName("27.154.54.242"); // "27.154.54.242"
        if(cmdSocket != null && !cmdSocket.isClosed())
            return;
        serverAddress = InetAddress.getByName("218.5.96.6"); // "27.154.54.242"
        cmdSocket = new Socket(serverAddress, 25712); //25712
        CSSLog.showLog("serverAddress:" + serverAddress, "cmdSocket:" + cmdSocket);
        cmdOutputStream = cmdSocket.getOutputStream();
    }

    private void startCmdWriteRead(Socket socket, final OutputStream outputStream, final CommonParam locker) {
        newPool();
        transmitFileService = new TransmitFileService(authorizeToken, pool, fileSocketReadyCallback);
        cmdsWrite = new SocketWrite(outputStream, this.authorizeToken, ReqType.cmd);
        //写操作设置锁
        cmdsWrite.setLocker(locker);
        cmdsWrite.sethbLocker(hbLocker);
        cmdsWrite.register(new SocketWriteCallback() {
            @Override
            public void onMaximumFileLen(int len) {
                fileSocketReadyCallback.onMaximumFileLength(len);
            }

            @Override
            public void onWriteHbFailed() {

            }
        });
        cmdRead = newSocketRead(socket);
        //读操作设置和写操作一样的锁
        cmdRead.setLocker(locker);
        cmdRead.sethbLocker(hbLocker);
        pool.execute(cmdsWrite);
        pool.execute(cmdRead);
    }

    private SocketRead newSocketRead(Socket socket) {
        SocketRead cmdRead = new SocketRead(socket, new SocketReadCallback() {

            @Override
            public void onIPHostReceived(String[] ips) {
                boolean initFileSocket = false;
                for (int ipScount = 0; ipScount <= ips.length - 1; ipScount++) {
                    String ipsigle = ips[ipScount];
//                    Log.wtf("lqm", "ipsigle=" + ipsigle);
                    String[] splitedIp = ipsigle.split(":");
                    String ipaddr = splitedIp[0];
                    String host = splitedIp[1];
                    if (transmitFileService != null) {
                        if (transmitFileService.initializeFileLongConn(ipaddr, Integer.parseInt(host))) {
                            initFileSocket = true;
                            break;
                        }
                    }
                }
                if (!initFileSocket) {
                    if (fileSocketReadyCallback != null)
                        fileSocketReadyCallback.onInitFileSocketFailed();
                }

            }

            @Override
            public void onReconnect(String command) {
                System.out.println("reconnect");
                closeCmdWriteSocket();
                boolean isCmdExist = CmdsConstant.CMDSTR.checkCmd(command);
                if (isCmdExist && CmdsConstant.CMDSTR.valueOf(command) == CmdsConstant.CMDSTR.auth) {
                    fileSocketReadyCallback.onOAuthFailed();
                }
                fileSocketReadyCallback.onCmdSocketReconnect();
                initializeCmdLongConn();

                if (transmitFileService != null && transmitFileService.isFileSocketAvailable()) {
                    newPool();
                    transmitFileService.closeFileSocketConnection();
                    transmitFileService = new TransmitFileService(authorizeToken, pool, fileSocketReadyCallback);
                }

            }


            @Override
            public void hasPush(List<String> msgBack, String ackSeqNo, String endQueueNo, long times) throws IOException, InterruptedException {
                cmdsWrite.sendPok(ackSeqNo, endQueueNo);
                fileSocketReadyCallback.onCmdHasPush(msgBack, ackSeqNo, times);
            }

            @Override
            public void onReady() {
                fileSocketReadyCallback.onCmdSocketReady();
            }

            @Override
            public void hasHeartRr(ArrayList<Integer> result, String r_hrr_endtime, String linkedIndex, String endindex) {
                fileSocketReadyCallback.onGetPidHeart(result,r_hrr_endtime,linkedIndex,endindex);
            }

            //            hasHeartRr()
            @Override
            public void getHeartHistory(String endindex, HashMap<String, ArrayList<Integer>> maps) {
                fileSocketReadyCallback.onGetPidHeartHistory(endindex,maps);
            }

            @Override
            public void onGeBrewSessionResp(String tk, String state) {

            }

            @Override
            public void onGetCmnPrgs(String percent, String seq_index, String body) {
            }

            @Override
            public void onServerRespError() {
                fileSocketReadyCallback.onServerRespError();
            }


        });

        return cmdRead;
    }

    private void closeCmdWriteSocket() {
        cmdsWrite.setExcute(false);
        cmdSocketLocker.seqNo = 0;
        try {
            cmdOutputStream.close();
            cmdSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("closeCmdWriteSocket fail");
        }
        synchronized (cmdSocketLocker) {
            cmdSocketLocker.notifyAll();
        }

        pool.shutdown();
    }


    public void closeSocket() {
        transmitFileService.closeFileSocketConnection();
        cmdRead.endReadThread(false);
        closeCmdWriteSocket();
    }

    public void closeFileSocket() {
        transmitFileService.closeFileSocketConnection();
    }

    public void closeCommandSocket() {
        cmdRead.endReadThread(false);
        closeCmdWriteSocket();
    }

    public SOCKETSTATE isCmdSocketClosed() {
        if (cmdSocket.isClosed() && !transmitFileService.isfileSocketClose()) {
            return SOCKETSTATE.cmd_socket_closed;
        } else if (!cmdSocket.isClosed() && !transmitFileService.isfileSocketClose()) {
            return SOCKETSTATE.socket_alive;
        } else if (cmdSocket.isClosed() && transmitFileService.isfileSocketClose()) {
            return SOCKETSTATE.socket_closed;
        } else if (!cmdSocket.isClosed() && transmitFileService.isfileSocketClose()) {
            return SOCKETSTATE.file_socket_closed;
        }
        return SOCKETSTATE.unknown;
    }
    //api called from outside
    public void beginFileUpload(UploadParam uploadParam) {
        if(transmitFileService != null)
            transmitFileService.beginFileUpload(uploadParam);
    }

    public void fileUpload(byte[] fileParts) {
        if(transmitFileService != null)
            transmitFileService.fileUpload(fileParts);
    }

    public void endFileUpload() {
        if(transmitFileService != null)
            transmitFileService.endFileUpload();
    }

    public void getHeartRealResult(String id, String bTime, int linkIndex, int testBindex) throws IOException {
        if(transmitFileService != null)
            transmitFileService.getPidHeartRr(id,bTime,linkIndex,testBindex);
    }

    public void getHeartHistory(String id, int endIndex, int whats6day, boolean isZip) throws IOException {
        if(transmitFileService != null)
            transmitFileService.getPidHearHistory(id,endIndex, whats6day, isZip);
    }
    public void sendBrewSessionCmd(Long package_id){
        if(transmitFileService != null){
            transmitFileService.sendBrewSessionCmd(package_id);
        }
    }

    public interface SocketReadCallback {
        void onIPHostReceived(String[] ip);

        void onReconnect(String command);

        void hasPush(List<String> msgBack, String ackSeqNo, String endQueueNo, long times) throws IOException, InterruptedException;

        void onReady();

        void hasHeartRr(ArrayList<Integer> result, String r_hrr_endtime, String linkedIndex, String endindex);

        void getHeartHistory(String endindex, HashMap<String, ArrayList<Integer>> maps);

        void onGeBrewSessionResp(String tk, String state);

        void onGetCmnPrgs(String percent, String seq_index, String body);

        void onServerRespError();
    }

    public interface SocketWriteCallback {
        void onMaximumFileLen(int len);
        void onWriteHbFailed();
    }
}
