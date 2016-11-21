package com.yealink.ims.fileshare.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 文件共享异常
 * author:pengzhiyuan
 * Created on:2016/5/31.
 */
public class FileShareException extends RuntimeException {
    private Throwable nestedThrowable = null;

    public FileShareException() {
        super();
    }

    public FileShareException(String msg) {
        super(msg);
    }

    public FileShareException(Throwable nestedThrowable) {
        this.nestedThrowable = nestedThrowable;
    }

    public FileShareException(String msg, Throwable nestedThrowable) {
        super(msg);
        this.nestedThrowable = nestedThrowable;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (nestedThrowable != null) {
            nestedThrowable.printStackTrace();
        }
    }

    @Override
    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (nestedThrowable != null) {
            nestedThrowable.printStackTrace(ps);
        }
    }

    @Override
    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (nestedThrowable != null) {
            nestedThrowable.printStackTrace(pw);
        }
    }
}
