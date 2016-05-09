package com.ltbrew.brewbeer.api.longconnection;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.ltbrew.brewbeer.api.longconnection.process.CmdsConstant;
import com.ltbrew.brewbeer.api.longconnection.process.CommonParam;
import com.ltbrew.brewbeer.api.longconnection.process.ParsePackKits;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
            decodeResp = pushServiceKits.decodeResp(resp);
            boolean decodeOK = decodeResp.finalResult.decodeOK;
            if (!decodeOK) {
                continue;
            }
            List<String> listResult = decodeResp.finalResult.resultList;
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
        System.out.println("commandWord = " + commandWord);
        if (handlerError(listResult)) return false;
        try {
            CmdsConstant.CMDSTR cmdstr = CmdsConstant.CMDSTR.valueOf(commandWord);
            if (cmdstr != CmdsConstant.CMDSTR.hb) {
                synchronized (locker) {
                    locker.notify();
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
                            System.err.println("lqmlqmlqmstr:" + str);
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
                    synchronized (locker) {
                        locker.notifyAll();
                    }

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
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    String endQueueNo = listResult.get(2);
                    List<String> pushList = listResult.subList(3, listResult.size());
                    socketReadCallback.hasPush(pushList, seqNo, endQueueNo, timeMsg);
                    break;
                case file_ul_begin:
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    callback.onFileUploadBegin(Integer.parseInt(seqNo));
                    break;
                case file_ul:
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    callback.onFileUploadSuccess();
                    break;
                case file_ul_end:
                    synchronized (locker) {
                        locker.notifyAll();
                    }
                    callback.onFileUploadEnd();
                    break;
                case r_hrr:
                    boolean isnullreault = false;
                    String frequency = null;
                    String hrr = null;
                    if (listResult.size() < 7) {
                        isnullreault = true;
                    }
                    String str_endtime = listResult.get(2);
                    Log.wtf("str_endtime", "str_endtime = " + str_endtime);
//                    .order(ByteOrder.BIG_ENDIAN)
//                    int r_hrr_endtime = ByteBuffer.wrap(str_endtime.getBytes()).order(ByteOrder.BIG_ENDIAN).getInt();
//                    int r_hrr_endtime_ll = ByteBuffer.wrap(str_endtime.getBytes()).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    long r_hrr_endtime = getCss_r_h_TimeInfo(str_endtime);
                    String linkedIndex = listResult.get(3);
                    String endindex = listResult.get(4);
                    if (isnullreault) {
                        hrr = listResult.get(5);
                    } else {
                        frequency = listResult.get(5);
                        hrr = listResult.get(6);
                    }
                    ArrayList<Integer> integers = new ArrayList<>();
                    if (hrr.equals("!")) {
                        integers.add(255);
                    } else {
                        char[] r_hrr_charArray = hrr.toCharArray();

                        for (char c : r_hrr_charArray) {
                            String iihEX = Integer.toHexString((int) c);
                            int int_result = Integer.valueOf(iihEX, 16);
                            integers.add(int_result);
                        }

                    }
                    socketReadCallback.hasHeartRr(integers, str_endtime, linkedIndex, endindex);
                    break;
                case r_hrh:
                    HashMap<String, ArrayList<Integer>> time2hbnun = new HashMap<>();
                    //TODO 用于判断是否解析合理的数据给上层
                    if (listResult.size() > 4) {
                        String hrh_endIndex = listResult.get(3);
                        for (int count = 4; count < listResult.size(); count++) {
                            ArrayList<Integer> hbnum = new ArrayList<>();
                            String s = listResult.get(count);
                            String head1 = s.substring(0, 2);
                            String head2 = s.substring(2, 3);
                            String head3 = s.substring(3, 4);
                            String head4 = s.substring(4, 8);
                            int startime = ByteBuffer.wrap(head4.getBytes()).order(ByteOrder.BIG_ENDIAN).getInt();
                            String head5 = s.substring(8, 12);
                            int endtime = ByteBuffer.wrap(head5.getBytes()).order(ByteOrder.BIG_ENDIAN).getInt();
                            String time = startime + "L" + endtime;
                            String hbnums = s.substring(12);
                            char[] charArray = hbnums.toCharArray();
                            for (char c : charArray) {
                                String iihEX = Integer.toHexString((int) c);
                                int int_result = Integer.valueOf(iihEX, 16);
                                hbnum.add(int_result);
                            }
                            time2hbnun.put(time, hbnum);
                            socketReadCallback.getHeartHistory(hrh_endIndex, time2hbnun);
                        }
                        //**********循环结束，然后就是把数据丢到我们可爱的app端了****************
                    } else if (listResult.size() <= 4) {
                        //TODO 这个包文明显少于应有包文的长度，即为不合法的包。
                        time2hbnun.put("error", null);
                        socketReadCallback.getHeartHistory("error", time2hbnun);
                    }
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
        if (listResult.size() > 2 && ParsePackKits.checkIsError(listResult.get(2))) {
//            System.out.println("RECV------------- ERROR");
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
