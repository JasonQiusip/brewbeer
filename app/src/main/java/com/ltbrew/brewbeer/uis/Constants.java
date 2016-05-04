package com.ltbrew.brewbeer.uis;

public interface Constants {

    String NETWORK_ERROR = "-1";

    public interface ReqValCodeState {
        public static final String SUCCESS = "0";
        public static final String ACCOUNT_FORMAT_ERROR = "1";
        public static final String ACCOUNT_NOT_MOBILE = "2";
    }

    public interface RegisterState {
        public static final String SUCCESS = "0";
        public static final String ACCOUNT_FORMAT_ERROR = "1";
        public static final String ACCOUNT_NOT_MOBILE = "2";
        public static final String VAL_CODE_FORMAT_ERROR = "3";
        public static final String VAL_CODE_INVALID_OR_NOT_EXIST = "4";
        public static final String ACCOUNT_EXIST = "5";
    }

    public interface LoginState {
        String PASSWORD_ERROR = "401";
        String NETWORK_ERROR = "-1";
    }

    interface CheckAccState{
        String PHONE_NOT_REGISTERED = "0";
        String NUMB_NOT_PHONE = "1";
        String ACC_REGISTERED = "2";
    }

    public interface PwdNewState {
        public static final String SUCCESS = "0";
        public static final String ACCOUNT_MISS = "1";
        public static final String PWD_MISS = "2";
        public static final String NO_VAL = "3";
        public static final String VAL_CODE_ERROR = "4";
    }


}
