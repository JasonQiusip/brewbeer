package com.ltbrew.brewbeer.uis.utils;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.ltbrew.brewbeer.service.ILtPushServiceAidlInterface;
import com.ltbrew.brewbeer.service.LtPushService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 151117a on 2016/5/10.
 */
public class ReqSessionStateQueue extends Thread {

    public Handler handler;
    ILtPushServiceAidlInterface ltPushService;
    public static final int CHECK_CMN_PRGS = 1;
    public static final int CHECK_CMN_MSG = 2;

    public ILtPushServiceAidlInterface getLtPushService() {
        return ltPushService;
    }

    public void setLtPushService(ILtPushServiceAidlInterface ltPushService) {
        this.ltPushService = ltPushService;
    }

    public Lock lock = new ReentrantLock();

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.e("", "looper ");
                Long package_id = (Long) msg.obj;
                if(ltPushService != null) {
                    Log.e("", "ltPushService is not null");

                    try {
                        switch (msg.what) {
                            case CHECK_CMN_PRGS:
                                ltPushService.sendBrewSessionCmd(package_id);
                                break;
                            case CHECK_CMN_MSG:
                                ltPushService.sendCmdToCheckTemp(package_id);
                                break;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e("", "remote call error?");
                    }
                    try {
                        synchronized (lock) {
                            lock.wait(5000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Looper.loop();
    }
}
