package com.race604.http;

import java.security.AccessControlException;
import java.util.Properties;

/**
 * 网络配置的属性类
 * @author rmss
 *
 */

public class Configuration {
	
    private static Properties defaultProperty;
    
    private static boolean DALVIK;

    static {
        init();
    }

   public static void init() {
        defaultProperty = new Properties();
        defaultProperty.setProperty("jike.debug", "true");
        defaultProperty.setProperty("jike.http.userAgent", "Android Client");
        defaultProperty.setProperty("jike.http.useSSL", "false");
        defaultProperty.setProperty("jike.http.proxyHost.fallback", "http.proxyHost");
        defaultProperty.setProperty("jike.http.proxyPort.fallback", "http.proxyPort");
        defaultProperty.setProperty("jike.http.connectionTimeout", "30000");
        defaultProperty.setProperty("jike.http.readTimeout", "60000");
        defaultProperty.setProperty("jike.http.retryCount", "0");
        defaultProperty.setProperty("jike.http.retryIntervalSecs", "10");
        defaultProperty.setProperty("jike.async.numThreads", "1");
        try {
            Class.forName("dalvik.system.VMRuntime");
            defaultProperty.setProperty("jike.dalvik", "true");
        } catch (ClassNotFoundException cnfe) {
            defaultProperty.setProperty("jike.dalvik", "false");
        }
        DALVIK = getBoolean("jike.dalvik");
    }

    public static boolean isDalvik() {
        return DALVIK;
    }


    public static String getProxyHost() {
        return getProperty("jike.http.proxyHost");
    }

    public static String getProxyHost(String proxyHost) {
        return getProperty("jike.http.proxyHost", proxyHost);
    }

    public static String getProxyUser() {
        return getProperty("jike.http.proxyUser");
    }

    public static String getProxyUser(String user) {
        return getProperty("jike.http.proxyUser", user);
    }

    public static String getProxyPassword() {
        return getProperty("jike.http.proxyPassword");
    }

    public static String getProxyPassword(String password) {
        return getProperty("jike.http.proxyPassword", password);
    }

    public static int getProxyPort() {
        return getIntProperty("jike.http.proxyPort");
    }

    public static int getProxyPort(int port) {
        return getIntProperty("jike.http.proxyPort", port);
    }

    public static int getConnectionTimeout() {
        return getIntProperty("jike.http.connectionTimeout");
    }

    public static int getConnectionTimeout(int connectionTimeout) {
        return getIntProperty("jike.http.connectionTimeout", connectionTimeout);
    }

    public static int getReadTimeout() {
        return getIntProperty("jike.http.readTimeout");
    }

    public static int getReadTimeout(int readTimeout) {
        return getIntProperty("jike.http.readTimeout", readTimeout);
    }

    public static int getRetryCount() {
        return getIntProperty("jike.http.retryCount");
    }

    public static int getRetryCount(int retryCount) {
        return getIntProperty("jike.http.retryCount", retryCount);
    }

    public static int getRetryIntervalSecs() {
        return getIntProperty("jike.http.retryIntervalSecs");
    }

    public static int getRetryIntervalSecs(int retryIntervalSecs) {
        return getIntProperty("jike.http.retryIntervalSecs", retryIntervalSecs);
    }

    public static String getUser() {
        return getProperty("jike.user");
    }

    public static String getUser(String userId) {
        return getProperty("jike.user", userId);
    }

    public static String getPassword() {
        return getProperty("jike.password");
    }

    public static String getPassword(String password) {
        return getProperty("jike.password", password);
    }

    public static String getUserAgent() {
        return getProperty("jike.http.userAgent");
    }

    public static String getUserAgent(String userAgent) {
        return getProperty("jike.http.userAgent", userAgent);
    }

    public static String getOAuthConsumerKey() {
        return getProperty("jike.oauth.consumerKey");
    }

    public static String getOAuthConsumerKey(String consumerKey) {
        return getProperty("jike.oauth.consumerKey", consumerKey);
    }
    
    public static boolean getBoolean(String name) {
        String value = getProperty(name);
        return Boolean.valueOf(value);
    }

    public static int getIntProperty(String name) {
        String value = getProperty(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static int getIntProperty(String name, int fallbackValue) {
        String value = getProperty(name, String.valueOf(fallbackValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static long getLongProperty(String name) {
        String value = getProperty(name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    public static String getProperty(String name, String fallbackValue) {
        String value;
        try {
            value = System.getProperty(name, fallbackValue);
            if (null == value) {
                value = defaultProperty.getProperty(name);
            }
            if (null == value) {
                String fallback = defaultProperty.getProperty(name + ".fallback");
                if (null != fallback) {
                    value = System.getProperty(fallback);
                }
            }
        } catch (AccessControlException ace) {
            // Unsigned applet cannot access System properties
            value = fallbackValue;
        }
        return replace(value);
    }

    private static String replace(String value) {
        if (null == value) {
            return value;
        }
        String newValue = value;
        int openBrace = 0;
        if (-1 != (openBrace = value.indexOf("{", openBrace))) {
            int closeBrace = value.indexOf("}", openBrace);
            if (closeBrace > (openBrace + 1)) {
                String name = value.substring(openBrace + 1, closeBrace);
                if (name.length() > 0) {
                    newValue = value.substring(0, openBrace) + getProperty(name)
                            + value.substring(closeBrace + 1);

                }
            }
        }
        if (newValue.equals(value)) {
            return value;
        } else {
            return replace(newValue);
        }
    }

    public static boolean getDebug() {
        return getBoolean("jike.debug");

    }
}
