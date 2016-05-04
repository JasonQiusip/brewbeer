package com.ltbrew.brewbeer.api.longconnection.process;

import com.ltbrew.brewbeer.api.common.CSSLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class ParsePackKits {

    static String _END_CRLF = "\r\n";
    static final Pattern NULL_LEN = Pattern.compile("^[$]([-][\\d]+)");
    static final Pattern BULK_LEN = Pattern.compile("^[$]([\\d]+)");
    static final Pattern BAT_LEN = Pattern.compile("^[*]([\\d]+)");
    static final Pattern ERR_PATTERN = Pattern.compile("^-[^\\r\\n]+\\r\\n");
    static final Pattern ERR_SEV_PATTERN = Pattern.compile("[-][^\\r\\n]+^[\\r\\n]]");
    static final Pattern SINGLE_PATTERN = Pattern.compile("^\\+[^\\r\\n]+\\r\\n");
    static final Pattern NUMBER_ONLY = Pattern.compile("^([\\d]+)$");
    static final Pattern INTEGER_PATTERN = Pattern.compile("^[:]([\\d]+)");
    private static final String UTF8 = "UTF-8";
    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * 按固定格式构建请求包
     *
     * @param list
     * @return
     */
    String buildRequestString(List<String> list) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("*");
        strBuilder.append(list.size());
        strBuilder.append("\r\n");
        for (String item : list) {
            if ("".equals(item)) {
                continue;
            }
            strBuilder.append(packParam(item));
        }
        return strBuilder.toString();
    }

    String packParam(String param) {
        StringBuilder strBuilder = new StringBuilder();
        if (checkIsNumber(param)) {
            strBuilder.append(":");
            strBuilder.append(param);
            strBuilder.append("\r\n");
        } else {
            strBuilder.append("$");
            strBuilder.append(param.length());
            strBuilder.append("\r\n");
            strBuilder.append(param);
            strBuilder.append("\r\n");
        }
        return strBuilder.toString();
    }

    /**
     * 解析返回包
     *
     * @param str
     * @return
     */
    public ResultType decodeResp(String str) {
        ResultType resultType = new ResultType();
        if (str.length() < 3) {
            resultType.type = RespType.IDLE;
            resultType.finalResult = new Result();
            resultType.finalResult.decodeOK = false;
            resultType.finalResult.remain = str;
            return resultType;
        }
        //将接受到的字符串赋值给待处理字符串
        String remain = str;
        if (remain == null || remain.length() == 0)
            return resultType;
        //检查剩余的字符串是否能够解析成功
        resultType = checkRemain(remain);

        return resultType;
    }

    public ResultType checkHrhRemain(String resp) {
        ResultType checkedType = new ResultType();
        if (resp.length() < 3) {
            checkedType.type = RespType.IDLE;
            checkedType.finalResult = new Result();
            checkedType.finalResult.decodeOK = false;
            checkedType.finalResult.remain = resp;
            return checkedType;
        }
        checkedType.finalResult = new Result();
        Result result;
        checkedType.type = RespType.BAT;
        result = unpackBat(resp);
        checkedType.finalResult = result;
        return checkedType;
    }

    ResultType checkRemain(String resp) {

        ResultType checkedType = new ResultType();
        checkedType.finalResult = new Result();
        Result result;
        boolean b = checkIsBat(resp);
        if (b) {
            checkedType.type = RespType.BAT;
            result = unpackBat(resp);
            checkedType.finalResult = result;
        } else if (checkIsBulk(resp)) {
            checkedType.type = RespType.BULK;
            result = unpackResp(resp, resp, 1);
            checkedType.finalResult = result;

        } else if (checkIsInteger(resp)) {
            checkedType.type = RespType.INTEGER;
            result = unpackInteger(resp);
            checkedType.finalResult = result;

        } else if (checkIsError(resp)) {
            checkedType.type = RespType.ERR;
        } else if (checkIsOK(resp)) {
            checkedType.type = RespType.SINGLE;
        } else {
            checkedType.type = RespType.IDLE;
        }

        return checkedType;

    }

    private Result unpackInteger(String resp) {
        String integer = unpackIntegerPack(INTEGER_PATTERN, resp);
        return null;
    }

    Result unpackBat(String respOrigin) {
        String resp = respOrigin;
        String batlen = unpackPack(BAT_LEN, resp);
        if (batlen == null)
            return null;
        //包的个数
        int batLenInt = Integer.valueOf(batlen);
        //去除包头
        int start = batlen.length() + _END_CRLF.length() + 1;
        CSSLog.showLog("unpackBat", "start=" + start);
        resp = resp.substring(start);
        //解析bulk
        return unpackResp(respOrigin, resp, batLenInt);
    }

    /**
     * 解析bulk包，
     * 应答为错误的包格式： -ERR unknown command 'INC'\r\n
     * 应答为数字的包格式： :1000\r\n
     * 应答为bulk的包格式： $7\r\nmyvalue\r\n
     * <p/>
     * 客户端收到的应答的包例子： *4\r\n$3\r\nfoo\r\n:3\r\n$5\r\nhello\r\n$5\r\nworld\r\n
     * <p/>
     * *4         $3         foo         :3          $5          hello        $5          world
     * *参数个数  $参数长度  字符串参数  :数字参数   $参数长度   字符串参数   $参数长度   字符串参数
     *
     * @param respOrigin
     * @param resp
     * @param len
     * @return
     */
    Result unpackResp(String respOrigin, String resp, int len) {
        Result result = new Result();
        boolean decodeFail = false;
        int i = 0;
        for (; i < len; i++) {
            //检查是否为数字， 如果是则单独解析该包
            if (checkIsInteger(resp)) {
                String integer = unpackIntegerPack(INTEGER_PATTERN, resp);
                if (1 + integer.length() + _END_CRLF.length() > resp.length()) {
                    if (i != len - 1)
                        decodeFail = true;
                    else
                        resp = "";
                    break;
                }
                result.resultList.add(integer);
                resp = resp.substring(1 + integer.length() + _END_CRLF.length());
                continue;
            } else if (checkIsError(resp)) {
                // 检查包是否为错误包， 如果是， 则直接把整个包添加到结果列表中返回
                result.resultList.add(resp);
                resp = "";
                break;
            } else if (checkIsNull(resp)) {
                String str_null = unpackPack(NULL_LEN, resp);
                if(str_null.equals("-1")){
                    str_null = "!";
                }
                result.resultList.add(str_null);
                int length = resp.length();
                int subLength = (1 + str_null.length() + _END_CRLF.length());
                if (length == subLength) {
                    resp = "";
                    break;
                } else {
                    resp = resp.substring(1 + str_null.length() + _END_CRLF.length());
                    continue;
                }
            }

            //获取单个bulk包的长度
            String lenBulk = unpackPack(BULK_LEN, resp);
            if (lenBulk == null) {
                decodeFail = true;
                break;
            }
            int indexStart = lenBulk.length() + _END_CRLF.length() + 1;
            //预计的包结束的index
            int indexEnd = indexStart + Integer.valueOf(lenBulk);
            //如果预计包结束的index大于包长度， 即包不完整， 退出解包循环
            if (indexEnd > resp.length()) {
                decodeFail = true;
                break;
            }
            //截取数据包
            String resultItem = resp.substring(indexStart, indexEnd);
            //将数据包添加到返回结果的list中
            result.resultList.add(resultItem);
            //如果预计第i个包结束的index加上结尾结束符长度大于整个包的长度
            if (indexEnd + _END_CRLF.length() > resp.length()) {
                //如果不是最后一个包则认为解析包失败， 反之则认为成功
                if (i != len - 1)
                    decodeFail = true;
                else
                    resp = "";
                break;
            }

            //截取剩余的包到resp中继续解析
            resp = resp.substring(indexEnd + _END_CRLF.length());
        }
        //解析循环结束， 将剩余的resp赋给结果中的remain以便下次解析
        result.remain = resp;
        result.decodeOK = true;
        if (decodeFail) {
            //如果解析失败， 把结果列表中的项清楚
            result.decodeOK = false;
            result.resultList.clear();
            //把原结果赋给remain以便下次解析
            result.remain = respOrigin;
        }
        return result;
    }

    String unpackPack(Pattern p, String resp) {
        Matcher matcher = p.matcher(resp);
        boolean find = matcher.find();
        if (find && matcher.groupCount() >= 1) {
            String group = matcher.group(1);
            return group;
        } else {
            return null;
        }
    }

    String unpackIntegerPack(Pattern p, String resp) {
        Matcher matcher = p.matcher(resp);
        boolean find = matcher.find();
        if (find && matcher.groupCount() >= 1) {
            String group = matcher.group(1);
            return group;
        } else {
            return null;
        }
    }

    static void showLog(String group) {
        System.out.println(group);
    }

    //HMAC SHA1
    byte[] doSign(String toSign, String keyString) throws Exception {
        SecretKeySpec key = new SecretKeySpec((keyString).getBytes(UTF8),
                HMAC_SHA1);
        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(key);
        byte[] bytes = mac.doFinal(toSign.getBytes(UTF8));

        return bytes;
    }


    public static StringBuilder byteArrayToStr(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            char ch = (char) (b & 0xff);
            sb.append(ch);
        }
        return sb;
    }

    static byte[] charToByteArray(char[] charArray) {

        byte[] input = new byte[charArray.length];
        int i = 0;
        for (char ch : charArray) {
            input[i] = (byte) ch;
            i++;
        }
        return input;
    }

    static String buildPack(String requestStr) {
        char first = (char) (requestStr.length() & 0xff);
        char second = (char) ((requestStr.length() >> 8) & 0xff);
        char third = (char) ((requestStr.length() >> 16) & 0xff);
        char fourth = (char) ((requestStr.length() >> 24) & 0xff);
        String pack = "*1" + fourth + third + second + first + requestStr;
        System.out.println(new Date().toString() + " PARSEPACKKITS" + "SEND-----------------------" + pack + "\n");
        return pack;
    }

    public static boolean checkIsError(String resp) {
        boolean b = ERR_SEV_PATTERN.matcher(resp).find();
        return ERR_PATTERN.matcher(resp).find();
    }

    public static boolean checkIsNull(String resp) {
        return NULL_LEN.matcher(resp).find();
    }

    static boolean checkIsOK(String resp) {
        return SINGLE_PATTERN.matcher(resp).find();
    }

    static boolean checkIsBulk(String resp) {
        return BULK_LEN.matcher(resp).find();
    }

    static boolean checkIsBat(String resp) {
        return BAT_LEN.matcher(resp).find();
    }

    static boolean checkIsInteger(String resp) {
        return INTEGER_PATTERN.matcher(resp).find();
    }

    static boolean checkIsNumber(String resp) {
        return NUMBER_ONLY.matcher(resp).find();
    }

    public class Result {
        public boolean decodeOK;
        public List<String> resultList = new ArrayList<String>();
        public String remain;
    }

    public class ResultType {
        public RespType type;
        public Result finalResult;
    }
}
