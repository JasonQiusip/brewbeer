package com.ltbrew.brewbeer.api.longconnection;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.ltbrew.brewbeer.api.common.RsyncUtils;
import com.ltbrew.brewbeer.api.longconnection.process.cmdconnection.CmdsConstant;
import com.ltbrew.brewbeer.api.longconnection.process.CommonParam;
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Jason on 2015/6/10.
 */
public class SocketRead implements Runnable {

    private final Socket socket;
    private final TransmitCmdService.SocketReadCallback socketReadCallback;
    private InputStream inputStream;
    private CommonParam locker;
    private ParsePackKits.ResultType decodeResp;
    ParsePackKits pushServiceKits;
    private boolean excute = true;
    private TransmitFileService.FileSocketReadCallback callback;
    private Object hbLocker;
    private int threadHashCode;

    public SocketRead(Socket socket, TransmitCmdService.SocketReadCallback socketReadCallback) {
        this.socket = socket;
        this.pushServiceKits = new ParsePackKits();
        this.socketReadCallback = socketReadCallback;
        this.threadHashCode = this.hashCode();
    }

    public void registerSocketReadCb(TransmitFileService.FileSocketReadCallback cb) {
        this.callback = cb;
    }

    public void endReadThread(boolean excute) {
        this.excute = excute;
        if(inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在创建读写操作时， 给读写操作同时设置的相同的锁
     *
     * @param locker
     */
    public void setLocker(CommonParam locker) {
        this.locker = locker;
    }

    /**
     * 设置心跳锁
     *
     * @param hbLocker
     */
    public void sethbLocker(Object hbLocker) {
        this.hbLocker = hbLocker;
    }

    //数据包前两个参数为命令字和序号， 该情况下为发出包之后的应答包
    void readDataFromServer() throws IOException {
        while (excute) {
            inputStream = socket.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            //如果decodeResp的数据为空或者decode成功
            if (checkRespDecode()) {
                len = read(buffer);
            }
            if (len == -1 && excute) {
                inputStream.close();
                socketReadCallback.onReconnect("outstream err");
                excute = false;
                //reconnect
                /**
                 *   1. network disconnect
                 *   2. network is connecting, but not good
                 */
                break;
            }
            long timeMsg = System.currentTimeMillis();
            outStream.write(buffer, 0, len);
            byte[] byteArray = outStream.toByteArray();
            StringBuilder sb = ParsePackKits.byteArrayToStr(byteArray);
            String resp = sb.toString();
            if (isRemain()) {
                resp = decodeResp.finalResult.remain + resp;
            }
            System.out.println(new Date().toString()+" PARSEPACKKITS" + "RESP-----------------------" +new String(byteArray) +"\n"+ RsyncUtils.toHexStr(resp.getBytes()) + "\n");

            decodeResp = pushServiceKits.decodeResp(resp);
            boolean decodeOK = decodeResp.finalResult.decodeOK;
            if (!decodeOK) {
                continue;
            }
            List<String> listResult = decodeResp.finalResult.resultList;
            if (handlerError(listResult)) {
                socketReadCallback.onServerRespError(listResult.get(0));
                decodeResp.finalResult.remain = "";
                continue;
            }
            boolean checkPack = !checkRespPack(listResult, timeMsg);
            if (checkPack) {
                inputStream.close();
                socketReadCallback.onReconnect(listResult.get(0));
                excute = false;
                break;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private boolean checkRespPack(List<String> listResult, long timeMsg) throws IOException {
        String commandWord = listResult.get(0);
        String seqNo = listResult.get(1);
        System.out.println("commandWord = " + listResult);

        try {
            boolean isCmdExist = CmdsConstant.CMDSTR.checkCmd(commandWord);
            if(!isCmdExist)
                return false;
            CmdsConstant.CMDSTR cmdstr = CmdsConstant.CMDSTR.valueOf(commandWord);
            if (cmdstr != CmdsConstant.CMDSTR.hb) {
                synchronized (locker) {
                    locker.notifyAll();  //收到服务器回包之后才能解锁写操作
                }
            }
            switch (cmdstr) {
                case kick:
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    inputStream.close();
                    socketReadCallback.onReconnect("kick");
                    excute = false;
                    break;
                case auth:
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    String ipAddr = listResult.get(2);
                    String isMultiAccSupport = listResult.get(3);
                    if (ipAddr.contains(".") & ipAddr.contains(":")) {
                        String[] ipAddrs = new String[]{ipAddr};
                        socketReadCallback.onIPHostReceived(ipAddrs);
                        return true;
                    }
                    char[] charIPS = isMultiAccSupport.toCharArray();
                    int groupCount = charIPS[0];
                    if (groupCount == 0) {
                        try {
                            throw new Exception("ips lenth == 0");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (groupCount > 0) {
                        String[] ipAddrs = new String[groupCount];
                        for (int ipscount = 1; ipscount <= groupCount; ipscount++) {
                            char[] goups = Arrays.copyOfRange(charIPS, 6 * (ipscount - 1) + 1, 6 * ipscount + 1);
                            StringBuffer sBuffer = new StringBuffer();
                            sBuffer.append(goups[0] & 0xff).append(".");
                            sBuffer.append(goups[1] & 0xff).append(".");
                            sBuffer.append(goups[2] & 0xff).append(".");
                            sBuffer.append(goups[3] & 0xff).append(":");
                            sBuffer.append((goups[4] & 0xff) << 8 | goups[5]);
                            String str = sBuffer.toString();
                            ipAddrs[ipscount - 1] = str;
                        }
                        socketReadCallback.onIPHostReceived(ipAddrs);
                    }
                    break;
                case mss:
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    break;
                case pok:

                    break;
                case hb:
                    /**
                     *  继续'Socket写'的心跳操作
                     *  参考SocketCustomWriter中的{@link SocketCustomWriter#writeHeartbeat}
                     */
                    synchronized (hbLocker) {
                        hbLocker.notify();
                    }
                    if (locker.seqNo == Integer.parseInt(seqNo)) {
                        socketReadCallback.onReady();
                    }
                    break;
                case push:
                case re_push:

                    String endQueueNo = listResult.get(2);
                    List<String> pushList = listResult.subList(3, listResult.size());
                    socketReadCallback.hasPush(pushList, seqNo, endQueueNo, timeMsg);
                    break;
                case file_ul_begin:
                    callback.onFileUploadBegin(Integer.parseInt(seqNo));
                    break;
                case file_ul:
                    callback.onFileUploadSuccess();
                    break;
                case file_ul_end:

                    callback.onFileUploadEnd();
                    break;
//                应答
//
//                参数	说明	类型
//                brew_session	命令字	str
//                序号		int
//                tk	会话token。如果token不存在，取None	str
//                state	啤酒酿造会话状态。0：未开始，1：进行中，2：已完成，3：中断	int
                case brew_session:
                    String tk = listResult.get(2);
                    String state = listResult.get(3);
                    Log.e("brew_session", tk + "   " + state);
                    ((TransmitFileService.SocketReackCallback)socketReadCallback).onGeBrewSessionResp(tk, state);

                    break;

//                参数	说明	类型
//                cmn_prgs	命令字	str
//                序号		int
//                percent	进度百分比。>=0, <=100	int
//                seq_index	序列索引。从0开始；选填的情况编码为-1；对于啤酒机项目来说，即配方步骤索引	int
//                body	说明文字	str
                case cmn_prgs:
                    String percent = listResult.get(2);
                    String seq_index = listResult.get(3);
                    String body = listResult.get(4);
                    ((TransmitFileService.SocketReackCallback)socketReadCallback).onGetCmnPrgs(percent, seq_index, body);
                    break;
                default:
                    break;
            }
            return true;
        } catch (Exception e) {
            Log.wtf("hrh", "e = " + e.toString());
            for (String s : listResult) {
                System.out.print(s);
            }
            return true;
        }
    }

    private Long getCss_r_h_TimeInfo(String sverORG) {
        char[] Time = sverORG.toCharArray();
        StringBuffer startTimeSb = new StringBuffer();
        for (char cc : Time) {
            int ii = cc;
            String iihEX = Integer.toHexString(ii);
            if (iihEX.length() < 2) {
                iihEX = "0" + iihEX;
            }
            Log.wtf("getCss_r_h_TimeInfo", "iihEX = " + iihEX);
            startTimeSb.append(iihEX);
        }
        return Long.parseLong(startTimeSb.toString(), 16);
    }

    private boolean handlerError(List<String> listResult) {
        if (listResult.size() >= 3 && ParsePackKits.checkIsError(listResult.get(2))) {
            System.out.println("RECV------------- ERROR" + listResult.get(0));
            return true;
        }
        return false;
    }

    private boolean checkRespDecode() {
        boolean isDecodeRespNull = (decodeResp == null);
        boolean isdecodeOK = (decodeResp != null && decodeResp.finalResult.decodeOK);
        boolean remainEmpty = (decodeResp != null && decodeResp.finalResult.remain != null && decodeResp.finalResult.remain.equals(""));
        return isDecodeRespNull || !isdecodeOK || (isdecodeOK && remainEmpty);
    }

    private boolean isRemain() {
        return decodeResp != null && decodeResp.finalResult != null && decodeResp.finalResult.remain != null && !decodeResp.finalResult.remain.equals("");
    }

    public int read(byte[] buffer) throws IOException {
        int bytes = -1;
        if (inputStream != null) bytes = inputStream.read(buffer);
        return bytes;
    }

    @Override
    public void run() {
//        System.out.println(new Date().toString() + "READ"+"***************" + this.hashCode() + "***********************");
        try {
            readDataFromServer();
        } catch (SocketException e) {
//            System.out.println(new Date().toString() + "READ"+ this.hashCode() + "  =================socket close exception=================");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
