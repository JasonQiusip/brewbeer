package com.ltbrew.brewbeer.uis.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.ltbrew.brewbeer.BrewApp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 151117a on 2016/6/7.
 */
public class BrewSessionUtils {

    public static final String START_TIME_STAMP = "boilStart";
    public static final String FERMENTING_TS = "fermenting_ts";

    //进程间数据共享不能使用sp 只能使用文件或者contentprovider
    public static void storeStepStartTimeStamp(String pack_id, long timeStamp) {
        storeFile(START_TIME_STAMP + pack_id, timeStamp);
    }


    public static long getStepStartTimeStamp(String pack_id) {

        return readFile(START_TIME_STAMP + pack_id);
    }

    public static void storeFermentingStartTimeStamp(Long package_id, long timeStamp) {
        storeFile(package_id + FERMENTING_TS, timeStamp);
    }

    public static long getFermentingStartTimeStamp(Long package_id) {
        return readFile(package_id + FERMENTING_TS);
    }


    private static long readFile(String filename){
        File file = getDiskCacheDir(BrewApp.getInstance(), filename);
        if(file == null)
            return 0;
        if(file.length() == 0)
            return 0;
        byte[] data = new byte[(int) file.length()];
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            dataInputStream.readFully(data);
            dataInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String time = new String(data);
        try {
            long timeL = Long.valueOf(time);
            return timeL;
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;

    }

    private static void storeFile(String filename, long timeStamp) {
        File file = getDiskCacheDir(BrewApp.getInstance(), filename);
        if(file != null && !file.getParentFile().exists())
            file.getParentFile().mkdirs();

        try {
            if(!file.exists())
                file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write((timeStamp+"").getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath();
            } else {
                return null;
            }
        } else {

            if (context.getCacheDir() != null) {
                cachePath = context.getCacheDir().getPath();
            } else {
                return null;
            }
        }
        return new File(cachePath + File.separator + uniqueName);
    }
}
