package com.race604.http;

/**
 * An exception class that will be thrown when JiKeAPI calls are failed.<br>
 * In case the server returned HTTP error code, you can get the HTTP status code using getStatusCode() method.
 */

public class HttpException extends Exception {
	
    private int statusCode = -1;
    
    private static final long serialVersionUID = -2623309261327598087L;
    
    private String mExtra;

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, String extra) {
        super(message);
        mExtra = extra;
    }

    public HttpException(Exception cause) {
        super(cause);
    }

    public HttpException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;

    }

    public HttpException(String msg, Exception cause) {
        super(msg, cause);
    }

    public HttpException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return this.statusCode;
    }
    
    public String getExtra() {
        return mExtra;
    }
}
