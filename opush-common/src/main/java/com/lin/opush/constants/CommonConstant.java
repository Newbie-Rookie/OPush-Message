package com.lin.opush.constants;

/**
 * 通用常量
 */
public class CommonConstant {
    public final static String SPACE = " ";
    public final static String COMMA = ",";
    public final static String COLON = ":";
    public final static String LEFT = "「";
    public final static String RIGHT = "」";
    public final static String JOIN = " ➢ ";
    public final static String CRLF = "\r\n";

    public final static char QM = '?';
    public final static String QM_STRING = "?";
    public final static String EQUAL_STRING = "=";
    public final static String AND_STRING = "&";

    /**
     * boolean转换
     */
    public final static Integer TRUE = 1;
    public final static Integer FALSE = 0;

    /**
     * 加密算法
     */
    public static final String HMAC_SHA256_ENCRYPTION_ALGO = "HmacSHA256";

    /**
     * 编码格式
     */
    public static final String CHARSET_NAME = "UTF-8";

    /**
     * HTTP请求内容格式
     */
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final String CONTENT_TYPE_TEXT = "text/html;charset=utf-8";
    public static final String CONTENT_TYPE_XML = "application/xml; charset=UTF-8";
    public static final String CONTENT_TYPE_FORM_URL_ENCODE = "application/x-www-form-urlencoded;charset=utf-8;";
    public static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * HTTP 请求方法
     */
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";

    /**
     * JSON默认值
     */
    public final static String EMPTY_JSON_OBJECT = "{}";
    public final static String EMPTY_VALUE_JSON_ARRAY = "[]";

    /**
     * cron时间格式
     */
    public final static String CRON_FORMAT = "ss mm HH dd MM ? yyyy-yyyy";
}