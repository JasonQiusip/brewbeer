package com.ltbrew.brewbeer.uis.utils;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ltbrew.brewbeer.service.LtPushService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 151117a on 2016/5/10.
 */
public class ReqSessionStateQueue extends Thread {

    public Handler handler;
    LtPushService ltPushService;

    public LtPushService getLtPushService() {
        return ltPushService;
    }

    public void setLtPushService(LtPushService ltPushService) {
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
                Log.e("", "looper mao si mei zuo yong ?");
                Long package_id = (Long) msg.obj;
                if(ltPushService != null) {
                    ltPushService.sendBrewSessionCmd(package_id);
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
