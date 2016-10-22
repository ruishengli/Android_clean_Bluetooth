package com.inlook.android_clean_bluetooth.data.exception;

/**
 * NetConnectionException
 *
 * @author or
 * @since 2016/10/21.
 */

public class NetWorkConnectionException extends RuntimeException {

    public NetWorkConnectionException() {
        super();
    }

    public NetWorkConnectionException(String message) {
        super(message);
    }

    public NetWorkConnectionException(String message,Throwable e) {
        super(message,e);
    }

    public NetWorkConnectionException(Throwable e) {
        super(e);
    }
}
