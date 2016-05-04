package com.ltbrew.brewbeer.api.longconnection.process;


import java.io.IOException;

/**
 * Created by Jason on 2015/6/13.
 * 定义cmd的写操作
 */
public class SocketCmdWriter extends SocketCustomWriter {

    private String ackSeqNo;
    private String endQueueNo;

    @Override
    public void changeCmdToSendPok(String ackSeqNo, String endQueueNo) throws IOException, InterruptedException {
        super.changeCmdToSendPok(ackSeqNo, endQueueNo);
        cmdType = CmdsConstant.CMDSTR.sendPok;
        this.ackSeqNo = ackSeqNo;
        this.endQueueNo = endQueueNo;
//        writeCmdPacks();
    }

    @Override
    void writeAuthorizePack() throws IOException {
        super.writeAuthorizePack();
    }

    @Override
    public void writeProbeMss() {
        super.writeProbeMss();
        int reduceCount = 0;
        do {
            locker.seqNo++;
            try {
                outputStream.write(toByteArr(ParsePackKits.buildPack(Cmds.buildPockCmd(locker.seqNo, ackSeqNo, endQueueNo, this.pushServiceKits))));
                break;
            } catch (IOException e) {
                e.printStackTrace();
                reduceCount++;
            }
        } while (true);

        if (socketWriteCb != null) {
            socketWriteCb.onMaximumFileLen(CmdsConstant.MSSMAX - reduceCount * CmdsConstant.DECREASERATE);
        }
    }

    @Override
    void writeHeartbeat() {
        super.writeHeartbeat();
    }

    @Override
    public void sendheartreal(String id, String bTime, int linkIndex, int testBindex) throws IOException {
        super.sendheartreal(id, bTime, linkIndex, testBindex);

    }

    @Override
    public void senHeartHistory(String pid, int endIndex, int whatsday, boolean isZip) throws IOException {
        super.senHeartHistory(pid,endIndex,whatsday,isZip);
    }

    @Override
    public void sendCmdReqPacks() {
        while (excute) {
            try {
                if (cmdType == CmdsConstant.CMDSTR.idle) {
                    //实时 关注pushservice是不是关掉了
                    continue;
                }
                writeCmdPacks();
                System.out.println("sendCmdReqPacks lock");
                synchronized (locker) {
                    locker.wait();
                }
                System.out.println("sendCmdReqPacks unlocked");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void writePushMsgBack() {
        locker.seqNo++;
        try {
            String requestStr = Cmds.buildPockCmd(locker.seqNo, ackSeqNo, endQueueNo, this.pushServiceKits);
            outputStream.write(toByteArr(ParsePackKits.buildPack(requestStr)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeCmdPacks() throws IOException, InterruptedException {
        switch (cmdType) {
            case auth:
                writeAuthorizePack();
                cmdType = CmdsConstant.CMDSTR.hb;
                break;
            case mss:
                writeProbeMss();
                cmdType = CmdsConstant.CMDSTR.hb;
                break;
            case hb:
                writeHeartbeat();
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case sendPok:
                writePushMsgBack();
                cmdType = CmdsConstant.CMDSTR.hb;
                break;
        }
    }


}
