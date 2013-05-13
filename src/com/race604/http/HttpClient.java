package com.race604.http;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.squareup.okhttp.OkHttpClient;

public class HttpClient {

	private static final int BUFFER_SIZE = 1024; // in bytes
	private static OkHttpClient client;
	private static byte[] buffer;

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

	String bowlingJson(String player1, String player2) {
		return "{'winCondition':'HIGH_SCORE'," + "'name':'Bowling',"
				+ "'round':4," + "'lastSaved':1367702411696,"
				+ "'dateStarted':1367702378785," + "'players':[" + "{'name':'"
				+ player1
				+ "','history':[10,8,6,7,8],'color':-13388315,'total':39},"
				+ "{'name':'" + player2
				+ "','history':[6,10,5,10,10],'color':-48060,'total':41}"
				+ "]}";
	}

}
