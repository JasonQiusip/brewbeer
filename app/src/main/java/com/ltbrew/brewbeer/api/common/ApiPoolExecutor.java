package com.ltbrew.brewbeer.api.common;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApiPoolExecutor {


    private static final int CORE_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 1;
    private static ApiPoolExecutor apiPoolExecutor;
    private final int MAX_POOL_SIZE = 3;
    private final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private ThreadPoolExecutor threadPoolExecutor;
    private LinkedBlockingQueue<Runnable> linkedBlockingQueue;

    private ApiPoolExecutor() {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        linkedBlockingQueue = new LinkedBlockingQueue<Runnable>();
        threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, linkedBlockingQueue);
    }

    public static ApiPoolExecutor getInstance() {
        if (apiPoolExecutor == null) {
            apiPoolExecutor = new ApiPoolExecutor();
        }
        return apiPoolExecutor;
    }


    public void execute(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }


}
