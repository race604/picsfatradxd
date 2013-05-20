
package com.race604.http;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.race604.http.CustomMultipartEntity.ProgressListener;

import android.content.Context;

/**
 * A utility class to handle HTTP request/response.
 */
public class HttpClient {

    public static final int NETWORK_ERROR = -1;// OK: Success!

    public static final int OK = 200;// OK: Success!

    public static final int PARTIAL_CONTENT = 206;// Partial Content

    public static final int REDIRECT = 302;// OK: Success!

    public static final int NOT_MODIFIED = 304;// Not Modified: There was no
                                               // new data to return.

    public static final int BAD_REQUEST = 400;// Bad Request: The request was
                                              // invalid. An accompanying
                                              // error message will explain
                                              // why. This is the status code
                                              // will be returned during rate
                                              // limiting.

    public static final int NOT_AUTHORIZED = 401;// Not Authorized:
                                                 // Authentication
                                                 // credentials were missing
                                                 // or incorrect.

    public static final int FORBIDDEN = 403;// Forbidden: The request is
                                            // understood, but it has been
                                            // refused. An accompanying
                                            // error message will explain
                                            // why.

    public static final int NOT_FOUND = 404;// Not Found: The URI requested is
                                            // invalid or the resource
                                            // requested, such as a user,
                                            // does not exists.

    public static final int NOT_ACCEPTABLE = 406;// Not Acceptable: Returned by
                                                 // the Search API when an
                                                 // invalid format is
                                                 // specified in the request.

    public static final int VCODE_ERROR = 440;

    public static final int REGISTER_ERROR = 445;

    public static final int INTERNAL_SERVER_ERROR = 500;// Internal Server
                                                        // Error: Something
                                                        // is broken. Please
                                                        // post to the group
                                                        // so the DianDian team
                                                        // can investigate.

    public static final int BAD_GATEWAY = 502;// Bad Gateway: DianDian is down
                                              // or
                                              // being upgraded.

    public static final int SERVICE_UNAVAILABLE = 503;// Service Unavailable:
                                                      // The DianDian servers
                                                      // are
                                                      // up, but overloaded
                                                      // with requests. Try
                                                      // again later. The
                                                      // search and trend
                                                      // methods use this to
                                                      // indicate when you are
                                                      // being rate limited.

    private Context context;

    private int retryCount = Configuration.getRetryCount();

    private int retryIntervalMillis = Configuration.getRetryIntervalSecs() * 1000;

    private int connectionTimeout = Configuration.getConnectionTimeout();

    private int readTimeout = Configuration.getReadTimeout();

    private Map<String, String> requestHeaders = new HashMap<String, String>();

    private static HttpClient mHttpClient = null;

    private DefaultHttpClient defaultHttpClient;

    private DefaultHttpClient sslHttpClient;

    private HttpClient(Context ctx) {
        context = ctx;
        setUserAgent(null);
        setRequestHeader("Accept-Encoding", "gzip");
        defaultHttpClient = getHttpClient();
        sslHttpClient = getSSLHttpClient();
    }

    public static void createInstance(Context ctx) {
        if (mHttpClient == null) {
            mHttpClient = new HttpClient(ctx);
        }
    }

    public static HttpClient getInstance() {
        return mHttpClient;
    }

    public static void closeQuietly(Reader input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening
     * a communications link to the resource referenced by this URLConnection.
     * System property -Dsinat4j.http.connectionTimeout overrides this
     * attribute.
     * 
     * @param connectionTimeout - an int that specifies the connect timeout
     *            value in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = Configuration.getConnectionTimeout(connectionTimeout);

    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout to a specified timeout, in milliseconds. System
     * property -Dsinat4j.http.readTimeout overrides this attribute.
     * 
     * @param readTimeout - an int that specifies the timeout value to be used
     *            in milliseconds
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = Configuration.getReadTimeout(readTimeout);
    }

    public void setRetryCount(int retryCount) {
        if (retryCount >= 0) {
            this.retryCount = Configuration.getRetryCount(retryCount);
        } else {
            throw new IllegalArgumentException("RetryCount cannot be negative.");
        }
    }

    public void setUserAgent(String ua) {
        setRequestHeader("User-Agent", Configuration.getUserAgent(ua));
    }

    public String getUserAgent() {
        return getRequestHeader("User-Agent");
    }

    public void setRetryIntervalSecs(int retryIntervalSecs) {
        if (retryIntervalSecs >= 0) {
            this.retryIntervalMillis = Configuration.getRetryIntervalSecs(retryIntervalSecs) * 1000;
        } else {
            throw new IllegalArgumentException("RetryInterval cannot be negative.");
        }
    }

    public Response get(String url) throws HttpException {
        return get(url, false);
    }

    public Response get(String url, boolean ssl) throws HttpException {
        return httpRequest(url, null, "GET", null, null, ssl);
    }

    public Response post(String url, PostParameter[] postParameters) throws HttpException {
        return post(url, postParameters, false);
    }

    public Response post(String url, PostParameter[] postParameters, boolean ssl)
            throws HttpException {
        return post(url, postParameters, null, null, ssl);
    }

    public Response post(String url, PostParameter[] postParameters, MultipartFile[] multFile,
            HttpRequestListener httpRequestListener) throws HttpException {
        return post(url, postParameters, multFile, httpRequestListener, false);
    }

    public Response post(String url, PostParameter[] postParameters, MultipartFile[] multFile,
            HttpRequestListener httpRequestListener, boolean ssl) throws HttpException {
        return httpRequest(url, postParameters, "POST", multFile, httpRequestListener, ssl);
    }

    public Response delete(String url) throws HttpException {
        return httpRequest(url, null, "DELETE", null, null, false);
    }

    protected long multPartFileTotalSize = 0;

    public Response httpRequest(String url, PostParameter[] params, String httpMethod,
            MultipartFile[] multipartFile, final HttpRequestListener httpRequestListener,
            boolean ssl) throws HttpException {
        int retriedCount;
        int retry = retryCount + 1;
        Response res = null;
        for (retriedCount = 0; retriedCount < retry; retriedCount++) {
            int responseCode = -1;
            try {
                HttpRequestBase httpRequest = null;

                if ((null != params) || "POST".equals(httpMethod)) {
                    httpRequest = new HttpPost(url);

                    if (multipartFile == null) {
                        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
                        for (PostParameter mPostParameter : params) {
                            paramList.add(new BasicNameValuePair(mPostParameter.getName(),
                                    mPostParameter.getValue()));
                        }
                        /* 添加请求参数到请求对象 */
                        ((HttpPost)httpRequest).setEntity(new UrlEncodedFormEntity(paramList,
                                HTTP.UTF_8));

                    } else {
                        CustomMultipartEntity multipartEntity = new CustomMultipartEntity(
                                new ProgressListener() {
                                    public void transferred(long num) {
                                        // 文件上传进度监听
                                        if (httpRequestListener != null) {
                                            httpRequestListener
                                                    .call((int)((num / (float)multPartFileTotalSize) * 100));
                                        }
                                    }
                                });

                        for (MultipartFile item : multipartFile) {
                            multipartEntity.addPart(item.getName(),
                                    new FileBody(new File(item.getFilePath())));
                        }

                        if (params != null) {
                            for (PostParameter entry : params) {
                                multipartEntity.addPart(entry.getName(),
                                        new StringBody(entry.getValue(), Charset.forName("utf-8")));
                            }
                        }

                        // 上传文件的大小
                        multPartFileTotalSize = multipartEntity.getContentLength();

                        ((HttpPost)httpRequest).setEntity(multipartEntity);
                    }

                } else if ("DELETE".equals(httpMethod)) {
                    httpRequest = new HttpDelete(url);
                } else {
                    httpRequest = new HttpGet(url);
                }
                // 添加请求用的Headers
                if ((requestHeaders != null) && (requestHeaders.size() > 0)) {
                    Iterator<Entry<String, String>> iter = requestHeaders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (key != null && value != null) {
                            httpRequest.addHeader(key, value);
                        }
                    }
                }
                HttpResponse httpResponse = null;

                if (ssl) {
                    if (sslHttpClient == null) {
                        sslHttpClient = getSSLHttpClient();
                    }
                    httpResponse = sslHttpClient.execute(httpRequest);
                } else {
                    if (defaultHttpClient == null) {
                        defaultHttpClient = getHttpClient();
                    }
                    httpResponse = defaultHttpClient.execute(httpRequest);
                }

                /* 打印返回的HTTP Header */
                /* 状态码 */
                responseCode = httpResponse.getStatusLine().getStatusCode();

                res = new Response(httpResponse, responseCode);
                if (responseCode != OK && responseCode != PARTIAL_CONTENT) {
                    if ((responseCode < INTERNAL_SERVER_ERROR) || (retriedCount == retryCount)) {
                        throw new HttpException("\n" + responseCode + " : " + url + "\n"
                                + res.asString(), responseCode);
                    }
                    // will retry if the status code is
                    // INTERNAL_SERVER_ERROR
                } else {
                    break;
                }
            } catch (IOException ioe) {
                // connection timeout or read timeout
                if (retriedCount == retryCount) {
                    throw new HttpException(ioe.getMessage(), ioe, responseCode);
                }
            }
//            try {
//                Thread.sleep(retryIntervalMillis);
//            } catch (InterruptedException ignore) {
//                // nothing to do
//            }
        }
        if (requestHeaders != null) {
            requestHeaders.clear();
        }
        return res;
    }

    public static String encodeParameters(PostParameter[] postParams) {
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < postParams.length; j++) {
            if (j != 0) {
                buf.append("&");
            }
            try {
                buf.append(URLEncoder.encode(postParams[j].name, "UTF-8")).append("=")
                        .append(URLEncoder.encode(postParams[j].value, "UTF-8"));
            } catch (java.io.UnsupportedEncodingException neverHappen) {
            }
        }
        return buf.toString();

    }

    public void setRequestHeader(String name, String value) {
        requestHeaders.put(name, value);
    }

    public String getRequestHeader(String name) {
        return requestHeaders.get(name);
    }

    private HttpParams getHttpParams() {
        // 创建 HttpParams 以用来设置 HTTP 参数（这一部分不是必需的）
        HttpParams httpParams = new BasicHttpParams();
        // 取得当前的网络状态
        int netType = NetworkUtil.getAccessPointType(context);
        if (netType == NetworkUtil.APN_CMWAP) {
            HttpHost proxy = new HttpHost(NetworkUtil.getHostAddress(), NetworkUtil.getHostPort());
            httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        // 设置连接超时和 Socket超时，以及 Socket 缓存大小
        ConnManagerParams.setTimeout(httpParams, 2000);
 
        HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, readTimeout);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
        // 设置重定向，缺省为 false
        HttpClientParams.setRedirecting(httpParams, true);
        // 设置 user agent
        HttpProtocolParams.setUserAgent(httpParams, null);
        return httpParams;
    }

    private DefaultHttpClient getHttpClient() {

        // 创建 HttpParams 以用来设置 HTTP 参数（这一部分不是必需的）
        HttpParams httpParams = getHttpParams();

        // 配置连接池参数
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        // 使用线程安全的连接管理来创建HttpClient
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(httpParams, schReg);
        // 创建一个 HttpClient 实例
        DefaultHttpClient httpClient = new DefaultHttpClient(conMgr, httpParams);
        return httpClient;
    }

    public DefaultHttpClient getSSLHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schReg.register(new Scheme("https", sf, 443));

            HttpParams httpParams = getHttpParams();
            ClientConnectionManager conMgr = new ThreadSafeClientConnManager(httpParams, schReg);
            return new DefaultHttpClient(conMgr, httpParams);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    public Context getContext() {
        return context;
    }

    /**
     * 上传文件进度监听接口
     * 
     * @author rmss
     */

    public static interface HttpRequestListener {

        void call(long num);
    }
}
