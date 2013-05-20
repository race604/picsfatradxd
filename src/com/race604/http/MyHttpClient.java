package com.race604.http;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

public class MyHttpClient {
	
	private static final String TAG = "HttpClient";

	private static final int BUFFER_SIZE = 1024; // in bytes
	private static OkHttpClient client;
	private static byte[] buffer = new byte[BUFFER_SIZE];

	private static OkHttpClient getClient() {
		if (client == null) {
			client = new OkHttpClient();
			buffer = new byte[BUFFER_SIZE];
		}
		return client;
	}

	public static String get(String url) throws IOException {
		HttpURLConnection connection = getClient().open(new URL(url));
		InputStream in = null;
		try {
			// Read the response.
			in = connection.getInputStream();
			byte[] response = readFully(in);
			return new String(response, "UTF-8");
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static String post(String url, JSONObject json) throws IOException {
		return post(url, json.toString().getBytes("UTF-8"));
	}

	public static String post(String url, byte[] body) throws IOException {
		HttpURLConnection connection = getClient().open(new URL(url));
		OutputStream out = null;
		InputStream in = null;
		try {
			// Write the request.
			connection.setRequestMethod("POST");
			out = connection.getOutputStream();
			out.write(body);
			out.close();

			// Read the response.
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("Unexpected HTTP response: "
						+ connection.getResponseCode() + " "
						+ connection.getResponseMessage());
			}
			in = connection.getInputStream();
			byte[] response = readFully(in);
			return new String(response, "UTF-8");
		} finally {
			// Clean up.
			if (out != null)
				out.close();
			if (in != null)
				in.close();
		}
	}

	public static void downloadToFile(String url, String file)
			throws IOException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			downloadToStream(url, fout);
		} finally {
			if (fout != null)
				fout.close();
		}
	}

	public static void downloadToStream(String url, OutputStream out)
			throws IOException {
		HttpURLConnection connection = getClient().open(new URL(url));
		InputStream in = null;
		try {
			// Read the response.
			in = connection.getInputStream();
			int count;
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private static byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int count; (count = in.read(buffer)) != -1;) {
			out.write(buffer, 0, count);
		}
		return out.toByteArray();
	}

}
