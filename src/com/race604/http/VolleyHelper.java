package com.race604.http;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyHelper {
	private static RequestQueue mRequestQueue;
	
	public static void init(Context context) {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(context);
		}
	}
	
	public static RequestQueue getRequestQueue() {
		return mRequestQueue;
	}
}
