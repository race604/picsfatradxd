
package com.race604.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Response {

    private final static int BUFFER_SIZE = 4096;

    /**
     * The magic header for the GZIP format.
     */
    public final static int GZIP_MAGIC = 0x8b1f;

    private int statusCode;

    private byte[] responseAsBytes = null;

    private String responseAsString = null;

    private InputStream is = null;

    private boolean streamConsumed = false;

    private HttpResponse httpResponse = null;

    private HttpEntity httpEntity = null;

    private long contentLength = -1;

    private OnReadProgressListener mListener = null;

    public Response(HttpResponse httpResponse, int statusCode) throws IOException {

        this.statusCode = statusCode;
        this.httpResponse = httpResponse;
        this.httpEntity = httpResponse.getEntity();
        this.contentLength = httpEntity.getContentLength();

        if (null != httpEntity) {
            is = httpEntity.getContent();
            /*
             * 为了使gzip数据跨过CMWAP网关， 这里不使用标准的HTTP协议， 不在头里携带"gzip"表示内容编码方式。
             * 接收到的数据流后不看ContentEncoding类型而直接尝试使用gzip解压缩
             */
            if (null != is) {
                /*
                 * 要取输入流的前两个字节判断是否是GZIP压缩格式（0x8b1f），
                 * 因为通过HttpURLConnection得到的InputStream不可以reset
                 * 所以把InputStream转换成BufferedInputStream，取前两个字节， 然后reset回输入流的开始位置
                 */
                BufferedInputStream bis = new BufferedInputStream(is);
                bis.mark(2);
                // 取前两个字节
                byte[] header = new byte[2];
                int result = bis.read(header);
                // reset输入流到开始位置
                bis.reset();

                // 判断是否是GZIP格式
                if (result != -1 && getShort(header, 0) == GZIP_MAGIC) {
                    is = new GZIPInputStream(bis);
                } else {
                    // 取前两个字节
                    is = bis;
                }
            }
        }
    }

    public void setOnReadProgressListener(OnReadProgressListener l) {
        this.mListener = l;
    }

    public String getFileNameByContentDisposition() {
        String mRet = null;
        if (httpResponse != null) {
            Header[] headers = httpResponse.getHeaders("Content-Disposition");
            if (headers != null) {
                for (Header h : headers) {
                    String value = h.getValue();
                    if (value != null) {
                        Pattern pattern = Pattern.compile("filename[:=]\"([^\"]+)\"");
                        Matcher matcher = pattern.matcher(value);
                        if (matcher.find()) {
                            mRet = matcher.group(1);
                            try {
                                mRet = new String(URLDecoder.decode(mRet, "iso8859-1").getBytes(
                                        "iso8859-1"), "gbk");
                            } catch (UnsupportedEncodingException e) {
//                                JKLog.LOGE(e.getMessage()); TODO
                            }
                        }
                    }
                }
            }
        }
        return mRet;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public boolean isOK() {
        return statusCode == HttpClient.OK;
    }

    public boolean isPartialContentOk() {
        return statusCode == HttpClient.PARTIAL_CONTENT;
    }

    public Header[] getResponseHeader(String name) {
        if (httpResponse != null) {
            return httpResponse.getHeaders(name);
        } else {
            return null;
        }
    }

    public String getFirstResponseHeader(String name) {
        if (httpResponse != null) {
            return httpResponse.getFirstHeader(name).getValue();
        } else {
            return null;
        }
    }

    /**
     * Returns the response stream.<br>
     * This method cannot be called after calling asString() or asDcoument()<br>
     * It is suggested to call disconnect() after consuming the stream.
     * Disconnects the internal HttpURLConnection silently.
     * 
     * @return response body stream
     * @throws HttpException
     * @see #disconnect()
     */
    public InputStream asStream() {
        if (streamConsumed) {
            throw new IllegalStateException("Stream has already been consumed.");
        }
        streamConsumed = true;
        return is;
    }

    public byte[] asBytes() throws HttpException {
        if (null == responseAsBytes) {
            try {
                readStream();
            } catch (NullPointerException npe) {
                // don't remember in which case npe can be thrown
                throw new HttpException(npe.getMessage(), npe);
            } catch (IOException ioe) {
                throw new HttpException(ioe.getMessage(), ioe);
            }
        }
        return responseAsBytes;
    }

    /**
     * Returns the response body as string.<br>
     * Disconnects the internal HttpURLConnection silently.
     * 
     * @return response body
     * @throws HttpException
     */
    public String asString() throws HttpException {
        if (null == responseAsString) {
            try {
                readStream();
            } catch (NullPointerException npe) {
                throw new HttpException(npe.getMessage(), npe);
            } catch (IOException ioe) {
                throw new HttpException(ioe.getMessage(), ioe);
            }
        }
        return responseAsString;
    }

    private void readStream() throws IOException {
        InputStream stream = asStream();
        if (null == stream) {
            return;
        }
        int total = 0;
        try {
            total = stream.available();
        } catch (IOException e) {
            try {
                Header[] headers = httpResponse.getAllHeaders();
                for (Header h : headers) {
                    String name = h.getName();
                    if (name.equalsIgnoreCase("content-length")) {
                        String val = h.getValue();
                        total = Integer.valueOf(val);
                        break;
                    }
                }
            } catch (Exception ee) {
                total = 0;
            }
        }
        if (mListener == null || total == 0) {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }
            if(buf.length()>0){
                buf.deleteCharAt(buf.length()-1);
            }
            br.close();
            this.responseAsString = buf.toString();
            this.responseAsBytes = responseAsString.getBytes("UTF-8");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = 0;
            int read = 0;
            long lastNotify = System.currentTimeMillis();
            while ((bytes = stream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytes);
                read += bytes;
                long now = System.currentTimeMillis();
                if (now - lastNotify > 300) {
                    mListener.onReadProgress(read, total);
                    lastNotify = now;
                }

            }
            bos.flush();
            this.responseAsBytes = bos.toByteArray();
            bos.close();
            this.responseAsString = new String(responseAsBytes, "UTF-8");
        }
        if (Configuration.isDalvik()) {
            this.responseAsString = unescape(responseAsString);
        }
        stream.close();
        streamConsumed = true;
    }

    /**
     * Returns the response body as sinat4j.org.json.JSONObject.<br>
     * Disconnects the internal HttpURLConnection silently.
     * 
     * @return response body as sinat4j.org.json.JSONObject
     * @throws HttpException
     */
    public JSONObject asJSONObject() throws HttpException {
        try {
            return new JSONObject(asString());
        } catch (JSONException jsone) {
            throw new HttpException(jsone.getMessage() + ":" + this.responseAsString, jsone);
        }
    }

    /**
     * Returns the response body as sinat4j.org.json.JSONArray.<br>
     * Disconnects the internal HttpURLConnection silently.
     * 
     * @return response body as sinat4j.org.json.JSONArray
     * @throws HttpException
     */
    public JSONArray asJSONArray() throws HttpException {
        try {
            return new JSONArray(asString());
        } catch (Exception jsone) {
            throw new HttpException(jsone.getMessage() + ":" + this.responseAsString, jsone);
        }
    }

    public InputStreamReader asReader() {
        try {
            return new InputStreamReader(is, "UTF-8");
        } catch (java.io.UnsupportedEncodingException uee) {
            return new InputStreamReader(is);
        }
    }

    private static Pattern escaped = Pattern.compile("&#([0-9]{3,5});");

    /**
     * Unescape UTF-8 escaped characters to string.
     * 
     * @author pengjianq...@gmail.com
     * @param original The string to be unescaped.
     * @return The unescaped string
     */
    public static String unescape(String original) {
        Matcher mm = escaped.matcher(original);
        StringBuffer unescaped = new StringBuffer();
        while (mm.find()) {
            mm.appendReplacement(unescaped,
                    Character.toString((char)Integer.parseInt(mm.group(1), 10)));
        }
        mm.appendTail(unescaped);
        return unescaped.toString();
    }

    @Override
    public String toString() {
        if (null != responseAsString) {
            return responseAsString;
        }
        return "Response{" + "statusCode=" + statusCode + ", responseString='" + responseAsString
                + '\'' + ", is=" + is + '}';
    }

    public String getResponseAsString() {
        return responseAsString;
    }

    private int getShort(byte[] buffer, int off) {
        return (buffer[off] & 0xFF) | ((buffer[off + 1] & 0xFF) << 8);
    }

    public static interface OnReadProgressListener {
        /**
         * @param progress value is 0~100
         */
        void onReadProgress(int read, int total);
    }
}
