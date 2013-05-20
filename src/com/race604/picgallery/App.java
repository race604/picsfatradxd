package com.race604.picgallery;

import android.app.Application;

import com.race604.http.HttpClient;

public class App extends Application {

	private static App mTheApp;

	public App() {
		super();
		mTheApp = this;
	}

	public static App get() {
		return mTheApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HttpClient.createInstance(this);
		// VolleyHelper.init(this);
	}

}
