package com.ltbrew.brewbeer.api.longconnection.process;

/**
 * Created by Hack on 2016/4/12.
 */
public class SocketDataCache {

    private String ackSeqNo,  endQueueNo;

    public String getAckSeqNo() {
        return ackSeqNo;
    }

    public void setAckSeqNo(String ackSeqNo) {
        this.ackSeqNo = ackSeqNo;
    }

    public String getEndQueueNo() {
        return endQueueNo;
    }

    public void setEndQueueNo(String endQueueNo) {
        this.endQueueNo = endQueueNo;
    }
}
