package com.ltbrew.brewbeer.api.common;

public interface ApiConstants {

    int TOKEN_DISK_LRU_INDEX = 0;
    int PWD_DISK_LRU_INDEX = 1;

    int ACC_DISK_LRU_INDEX = 0;
    int SECRET_DISK_LRU_INDEX = 1;
    int EXPIRE_DISK_LRU_INDEX = 2;

    interface ShareFileKey {
        String GID = "gid";
        String SHARE_TO = "share_to";
        String LOCATION = "location";
        String TEXT = "text";
        String TYPE = "type";
        String MENTION = "mention";
        String FILES = "files";
    }

    final String GEN_TK = "gen_tk";
    // 验证是否已被注册
    final String SMS_VALID_ACC_NEW = "valid_acc_new";
    // 验证码
    final String SMS_REQ_REG_VAL_CODE = "req_reg_val_code";
    // 发送给用户
    final String SMS_CHECK_REG_VAL_CODE = "check_reg_val_code";

    final String ACTIVE_SMS = "active/sms";
    // 提起重置密码
    /**
     * 状态（state） 说明 0 申请成功 1 申请太频繁，失败 2 帐号不像手机号，失败
     */
    final String SMS_PWD_LOSE = "pwd_lost";

    // 提起重置密码
    final String SMS_PWD_NEW = "pwd_new";

    final String BREW_HISTORY = "brew_history";
    final String BREW_BEGIN = "brew_begin";
    final String BREW_LS_FORMULA = "brew_ls_formula";

    final String BIND = "bind";
    String UNBIND = "unbind";
    String PATCH_TID = "patch_tid";
    String LIST = "list";
    String INFO = "info";


    final String ACC_SUFFIX = "@linktop.com.cn";


}
