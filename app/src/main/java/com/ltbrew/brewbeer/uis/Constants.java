package com.ltbrew.brewbeer.uis;

public interface Constants {

    String NETWORK_ERROR = "-1";
    String PASSWORD_ERROR = "401";

    public interface ReqValCodeState {
        public static final String SUCCESS = "0";
        public static final String ACCOUNT_FORMAT_ERROR = "1";
        public static final String ACCOUNT_NOT_MOBILE = "2";
    }

    public interface RegisterState {
        public static final String SUCCESS = "0";
        public static final String APIKEY_NO_NEED_TO_ACTIVE = "1";
        public static final String CODE_ERROR = "2";
        public static final String CHECK_PHONE_NO = "3";
        public static final String CODE_ERROR_AGAIN = "4";
        public static final String CHECK_YOUR_PARAM = "5";
    }

    public interface LoginState {
        String PASSWORD_ERROR = "401";
        String NETWORK_ERROR = "-1";
    }

    interface CheckAccState{
        String PHONE_NOT_REGISTERED = "0";
        String NUMB_NOT_PHONE = "1";
        String ACC_REGISTERED = "2";

        String NOT_PHONE_NOTICE = "账号不是手机号";
        String ACC_REGISTERED_NOTICE = "账号已注册";
    }

    interface ReqPwdLostState{
        String SUCCESS = "0";
        String REQ_CODE_TOO_OFTEN = "1";
        String CHECK_PHONE_NO = "2";
    }

    public interface PwdNewState {
        public static final String SUCCESS = "0";
        public static final String ACCONT_FORMAT_ERROR = "1";
        public static final String CHECK_PHONE_NO = "2";
        public static final String VAL_CODE_ERROR = "3";
    }

    interface BrewSessionType{
        int BREWING = 0;
        int FERMENTING = 1;
        int SUSPEND = 2;
        int FINSHED = 3;
    }

    String FermentDoneMsg = "time left less than or equal to 0";

}
