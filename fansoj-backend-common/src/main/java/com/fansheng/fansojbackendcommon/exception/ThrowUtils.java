package com.fansheng.fansojbackendcommon.exception;

import com.fansheng.fansojbackendcommon.common.ErrorCode;

/**
 * 抛异常工具类
 *
 * @author fansheng
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new com.fansheng.fansojbackendcommon.exception.BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param errorCode
     * @param message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new com.fansheng.fansojbackendcommon.exception.BusinessException(errorCode, message));
    }
}
