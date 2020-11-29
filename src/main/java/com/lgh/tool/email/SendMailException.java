package com.lgh.tool.email;


/**
 * @ClassName SendMailException
 * @Author lgh
 * @Date 2020/11/3 9:46
 **/
public class SendMailException extends Exception {

    public SendMailException() {
    }

    public SendMailException(String message) {
        super(message);
    }

    public SendMailException(Throwable cause) {
        super(cause);
    }
}
