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
    public void sendCmdReqPacks() {
        while (excute) {
            try {
                if (cmdType == CmdsConstant.CMDSTR.idle) {
                    //实时 关注pushservice是不是关掉了
                    System.out.println("空转好吗？？？？？？");
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

    private void writeCmdPacks() throws IOException, InterruptedException {
        switch (cmdType) {
            case auth:
                writeAuthorizePack();
                cmdType = CmdsConstant.CMDSTR.hb;
                break;
//            case mss:
//                writeProbeMss();
//                cmdType = CmdsConstant.CMDSTR.hb;
//                break;
            case hb:
                writeHeartbeat();
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case sendPok:
                writePok();
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case cmn_prgs:
                sendCmdTogetCmnPrgs();
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
            case brew_session:
                sendCmdToCheckBrewSession();
                cmdType = CmdsConstant.CMDSTR.idle;
                break;
        }
    }

    @Override
    void writeAuthorizePack() throws IOException {
        super.writeAuthorizePack();
    }

    private void writeProbeMss() {
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

    private void writePok() {
        locker.seqNo++;
        try {
            String requestStr = Cmds.buildPockCmd(locker.seqNo, ackSeqNo, endQueueNo, this.pushServiceKits);
            outputStream.write(toByteArr(ParsePackKits.buildPack(requestStr)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCmdTogetCmnPrgs() {
        locker.seqNo++;
        try {
            String requestStr = Cmds.buildCmnPrgsCmd(locker.seqNo, this.pushServiceKits);
            outputStream.write(toByteArr(ParsePackKits.buildPack(requestStr)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCmdToCheckBrewSession() {
        locker.seqNo++;
        try {
            String requestStr = Cmds.buildBrewSessionCmd(locker.seqNo,this.pushServiceKits);
            outputStream.write(toByteArr(ParsePackKits.buildPack(requestStr)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //******************************************************** 外部控制修改命令******************************************//
    //外部修改命令类型， 内部更加命令类型执行命令
    @Override
    public void changeCmdToSendPok(String ackSeqNo, String endQueueNo) throws IOException, InterruptedException {
        super.changeCmdToSendPok(ackSeqNo, endQueueNo);
        cmdType = CmdsConstant.CMDSTR.sendPok;
        this.ackSeqNo = ackSeqNo;
        this.endQueueNo = endQueueNo;
    }

    @Override
    public void changeCmdToSendCmnPrgs(){
        cmdType = CmdsConstant.CMDSTR.cmn_prgs;
    }
    @Override
    public void changeCmdToSendBrewSession(){
        cmdType = CmdsConstant.CMDSTR.brew_session;
    }
}
