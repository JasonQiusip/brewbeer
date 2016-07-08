package com.ltbrew.brewbeer.api.longconnection;

import com.ltbrew.brewbeer.api.common.CSSLog;
import com.ltbrew.brewbeer.api.common.TokenDispatcher;
import com.ltbrew.brewbeer.api.longconnection.interfaces.FileSocketReadyCallback;
import com.ltbrew.brewbeer.api.longconnection.process.cmdconnection.CmdsConstant;
import com.ltbrew.brewbeer.api.longconnection.process.CommonParam;
import com.ltbrew.brewbeer.api.longconnection.process.ReqType;
import com.ltbrew.brewbeer.api.model.Direct_push;
import com.ltbrew.brewbeer.api.model.Lt_stream;
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
    private SocketWriteProxy cmdsWrite;
    private String ip;
    private int port;

    public static TransmitCmdService newInstance(String authorizeToken, FileSocketReadyCallback fileSocketReadyCallback) {
        if (cmdService == null) {
            cmdService = new TransmitCmdService(authorizeToken, fileSocketReadyCallback);
        }
        return cmdService;
    }

    private TransmitCmdService(String authorizeToken, FileSocketReadyCallback fileSocketReadyCallback) {
        this.authorizeToken = authorizeToken;
        this.fileSocketReadyCallback = fileSocketReadyCallback;
    }


    public boolean initializeLongConn() {
        newPool();
        initFileStreamTransmitSocket();
        return initCmdSocket();
    }

    private void newPool() {
        pool = Executors.newFixedThreadPool(4);
    }

    private void initFileStreamTransmitSocket(){
        boolean initFileSocket = false;
        transmitFileService = new TransmitFileService(authorizeToken, pool, fileSocketReadyCallback);
        Lt_stream ltStream = ManageLongConn.getInstance().getLtStream();
        if(ltStream == null)
            return;
        for (int ipScount = 0; ipScount <= ltStream.getPorts().size() - 1; ipScount++) {

            if (transmitFileService != null) {
                if(ManageLongConn.getInstance().ipHost == null){
                    ManageLongConn.getInstance().ipHost = "117.28.254.73";
                }
                if (transmitFileService.initializeFileLongConn(ManageLongConn.getInstance().ipHost, ltStream.getPorts().get(ipScount))) {
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

    private boolean initCmdSocket() {
        Direct_push directPush = ManageLongConn.getInstance().getDirectPush();
        if(directPush == null)
            return false;
        ArrayList<Integer> ports = directPush.getPorts();
        for(int i = 0; i < ports.size(); i++) {
            try {
                initCmdTransmitSocket(ports.get(i));
                startCmdWriteRead(cmdSocket, cmdOutputStream, cmdSocketLocker);
                return true;
            } catch (IOException e) {
                fileSocketReadyCallback.onInitializeLongConnFailed();
                e.printStackTrace();
            }
        }
        return false;
    }

    private void initCmdTransmitSocket(Integer port) throws IOException {

        if(cmdSocket != null && !cmdSocket.isClosed())
            return;
        if(ManageLongConn.getInstance().ipHost == null){
            ManageLongConn.getInstance().ipHost = "117.28.254.73";
        }
        if(port == null){
            port = 26012;
        }
        serverAddress = InetAddress.getByName(ManageLongConn.getInstance().getDirectPush().getHost()); // "27.154.54.242"
        cmdSocket = new Socket(serverAddress, port); //25712

//        serverAddress = InetAddress.getByName("218.5.96.6"); // "27.154.54.242"
//        cmdSocket = new Socket(serverAddress, 25712); //25712
        CSSLog.showLog("serverAddress:" + serverAddress, "cmdSocket:" + cmdSocket);
        cmdOutputStream = cmdSocket.getOutputStream();
    }

    private void startCmdWriteRead(Socket socket, final OutputStream outputStream, final CommonParam locker) {
        cmdsWrite = new SocketWriteProxy(outputStream, this.authorizeToken, ReqType.cmd);
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

            @Override
            public void onBeforeWriteHb() {
                fileSocketReadyCallback.onWritingHb();
            }

            @Override
            public void onOutputStreamIOError() {
                reconnect("");
            }

            @Override
            public void onWriteInerrupt() {
                reconnect("");
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

            }

            @Override
            public void onReconnect(String command) {
                System.out.println("reconnect");
                reconnect(command);

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
            public void onServerRespError(String cmd) {
                fileSocketReadyCallback.onServerRespError(cmd);
            }

            @Override
            public void onStResultResp(String fd, String stToken) {
                ManageLongConn.getInstance().storeFdAndStToken(fd, stToken);
            }
        });

        return cmdRead;
    }

    private void reconnect(String command) {
        closeCmdWriteSocket();
        boolean isCmdExist = CmdsConstant.CMDSTR.checkCmd(command);
        if (isCmdExist && CmdsConstant.CMDSTR.valueOf(command) == CmdsConstant.CMDSTR.auth) {
            fileSocketReadyCallback.onOAuthFailed();
        }
        if(isCmdExist && CmdsConstant.CMDSTR.valueOf(command) == CmdsConstant.CMDSTR.kick){
            TokenDispatcher.getInstance().setToken(null);
            fileSocketReadyCallback.onLongConnectionKickedOut();
            return;
        }
        fileSocketReadyCallback.onCmdSocketReconnect();
        initializeLongConn();

        if (transmitFileService != null && transmitFileService.isFileSocketAvailable()) {
            newPool();
            transmitFileService.closeFileSocketConnection();
            transmitFileService = new TransmitFileService(authorizeToken, pool, fileSocketReadyCallback);
        }
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
        if(cmdRead != null)
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

    public SOCKETSTATE isSocketClosed() {
        if (isCmdSocketClosed() && !transmitFileService.isfileSocketClose()) {

            return SOCKETSTATE.cmd_socket_closed;
        } else if (!isCmdSocketClosed() && !transmitFileService.isfileSocketClose()) {

            return SOCKETSTATE.socket_alive;
        } else if (isCmdSocketClosed() && transmitFileService.isfileSocketClose()) {

            return SOCKETSTATE.socket_closed;
        } else if (!isCmdSocketClosed()&& transmitFileService.isfileSocketClose()) {

            return SOCKETSTATE.file_socket_closed;
        }
        return SOCKETSTATE.unknown;
    }

    public boolean isCmdSocketClosed(){
        if (cmdSocket == null || (cmdSocket != null && cmdSocket.isClosed()))
            return true;
        else
            return false;

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

    public void checkCmnMSgLast(String pid, String token) {
        if (transmitFileService != null) {
            transmitFileService.checkCmnMsgLast(pid, token);
        }
    }

    public static void nullCmdTransmitService() {
        cmdService = null;
    }

    public interface SocketReadCallback {
        void onIPHostReceived(String[] ip);

        void onReconnect(String command);

        void hasPush(List<String> msgBack, String ackSeqNo, String endQueueNo, long times) throws IOException, InterruptedException;

        void onReady();

        void hasHeartRr(ArrayList<Integer> result, String r_hrr_endtime, String linkedIndex, String endindex);

        void getHeartHistory(String endindex, HashMap<String, ArrayList<Integer>> maps);

        void onServerRespError(String s);

        void onStResultResp(String fd, String stToken);
    }

    public interface SocketWriteCallback {
        void onMaximumFileLen(int len);
        void onWriteHbFailed();

        void onBeforeWriteHb();

        void onOutputStreamIOError();

        void onWriteInerrupt();
    }
}
