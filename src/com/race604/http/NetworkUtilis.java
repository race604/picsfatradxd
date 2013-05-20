package com.race604.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;

public class NetworkUtilis {
	public static final int TIMEOUT_MILLISEC = 5000; // = 5 seconds
	public static final int BUFFER_SIZE = (1 << 10) * 4; // 4 K

	private NetworkUtilis() {
	}

	public static HttpResponse getHttpResponse(String url, JSONObject json)
			throws IOException {
		String str = null;
		if (json != null) {
			str = json.toString();
		}
		return getHttpResponse(url, str);
	}

	public static HttpResponse getHttpResponse(String url, String json)
			throws IOException {

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
		HttpClient client = new DefaultHttpClient(httpParams);

		HttpRequestBase request = null;
		if (json == null) {
			request = new HttpGet(url);
		} else {
			// String enJson = new String(Encryption.encrypty(json));
			String enJson = json;
			HttpPost post = new HttpPost(url);
			StringEntity se = null;
			se = new StringEntity(enJson, "UTF-8");
			post.setEntity(se);
			post.setHeader("Accept", "application/json;charset=UTF-8");
			post.setHeader("Content-type", "application/json;charset=UTF-8");

			request = post;
		}

		HttpResponse response = null;

		response = client.execute(request);

		return response;
	}

	public static int getHttpOutStream(String url, JSONObject params,
			OutputStream out) throws NetworkErrorException, IOException {
		HttpResponse response = getHttpResponse(url, params);

		int ret = response.getStatusLine().getStatusCode();
		InputStream in = null;
		if (ret == HttpURLConnection.HTTP_OK) {
			final HttpEntity res = response.getEntity();
			if (res != null) {
				try {
					in = res.getContent();
				} catch (Exception e) {
					throw new NetworkErrorException(e);
				}
			} else {
				return -1;
			}
		}

		if (in == null) {
			return -1;
		}

		int length = 0;
		Header[] headers = response.getAllHeaders();
		for (Header h : headers) {
			String name = h.getName();
			if (name.equalsIgnoreCase("content-length")) {
				String val = h.getValue();
				length = Integer.valueOf(val);
				break;
			}
		}

		byte[] buf = new byte[BUFFER_SIZE];
		int read = 0;
		do {

			int size = in.read(buf);
			if (size <= 0) {
				break;
			}
			out.write(buf, 0, size);
			read += size;

		} while (true);

		return read;

	}

	public static String getHttpString(String url, JSONObject params)
			throws IOException {
		HttpResponse response = getHttpResponse(url, params);
		// int ret = response.getStatusLine().getStatusCode();
		// if (ret == HttpURLConnection.HTTP_OK) {
		// final HttpEntity res = response.getEntity();
		// if (res != null) {
		// InputStream is = null;
		// try {
		// is = res.getContent();
		// } catch (IllegalStateException e) {
		// throw new JKException(e);
		// }
		// InputStreamReader reader = new InputStreamReader(is);
		// StringBuilder sb = new StringBuilder();
		// char[] buf = new char[BUFFER_SIZE];
		// int size = 0;
		// while ((size = reader.read(buf)) > 0) {
		// sb.append(buf, 0, size);
		// }
		// is.close();
		// // 暂时去掉加密
		// // String deJson = Encryption.decrypt(sb.toString());
		// String deJson = sb.toString();
		// return deJson;
		// }
		// }

		return getStringFromHttpResponse(response);
	}

	private static String getStringFromHttpResponse(HttpResponse response)
			throws IOException {
		int ret = response.getStatusLine().getStatusCode();
		if (ret == HttpURLConnection.HTTP_OK) {
			final HttpEntity res = response.getEntity();
			if (res != null) {
				InputStream is = null;
				is = res.getContent();
				InputStreamReader reader = new InputStreamReader(is);
				StringBuilder sb = new StringBuilder();
				char[] buf = new char[BUFFER_SIZE];
				int size = 0;
				while ((size = reader.read(buf)) > 0) {
					sb.append(buf, 0, size);
				}
				is.close();
				// 暂时去掉加密
				// String deJson = Encryption.decrypt(sb.toString());
				String deJson = sb.toString();
				return deJson;
			}
		}
		return null;
	}

	public static int[] getIntArray(JSONArray json) {
		if (json == null) {
			return null;
		}

		int[] ids = new int[json.length()];
		for (int i = 0; i < json.length(); ++i) {
			ids[i] = json.optInt(i);
		}
		return ids;
	}

}
