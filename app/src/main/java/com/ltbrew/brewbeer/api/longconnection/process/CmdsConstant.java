package com.ltbrew.brewbeer.api.longconnection.process;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jason on 2015/6/9.
 */
public interface CmdsConstant {
    int MSSMAX = 1460;
    int DECREASERATE = 10;

   enum CMDSTR{
        auth,
        mss,
        hb,
        file_ul_begin,
        file_ul,
        file_ul_end,
        idle,
        abort,
        push,
        pok,
        sendPok,
        re_push,
        r_hrr,
        r_hrh,
        kick,
        cmn_prgs,
        brew_session;

        public static final Map lookup = new HashMap();
        static {
            for (CMDSTR mCMDSTR : EnumSet.allOf(CMDSTR.class)) {
                lookup.put(mCMDSTR.name(), mCMDSTR);
            }
        }

       public static boolean checkCmd(String commandWord){
           Object obj = CmdsConstant.CMDSTR.lookup.get(commandWord);
           if(obj == null) {
               System.out.println("未识别的指令");
               return false;
           }
           return true;
       }
    }


}
