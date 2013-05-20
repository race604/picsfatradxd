package com.race604.picgallery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;

import com.race604.bitmapcache.ImageCache;

public class Utils {

	private Utils() {
	}

	@TargetApi(11)
	public static void enableStrictMode() {
		// if (Utils.hasGingerbread()) {
		// StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
		// new StrictMode.ThreadPolicy.Builder()
		// .detectAll()
		// .penaltyLog();
		// StrictMode.VmPolicy.Builder vmPolicyBuilder =
		// new StrictMode.VmPolicy.Builder()
		// .detectAll()
		// .penaltyLog();
		//
		// if (Utils.hasHoneycomb()) {
		// threadPolicyBuilder.penaltyFlashScreen();
		// vmPolicyBuilder
		// .setClassInstanceLimit(ImageGridActivity.class, 1)
		// .setClassInstanceLimit(ImageDetailActivity.class, 1);
		// }
		// StrictMode.setThreadPolicy(threadPolicyBuilder.build());
		// StrictMode.setVmPolicy(vmPolicyBuilder.build());
		// }
	}

	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}

	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	public static String extractDigits(String src) {
		StringBuilder builder = new StringBuilder();
		int len = src.length();
		for (int i = 0; i < len; i++) {
			char c = src.charAt(i);
			if (Character.isDigit(c)) {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	public static File getSaveImageDir() {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !ImageCache.isExternalStorageRemovable()) {
			return new File(Environment.getExternalStorageDirectory()
					+ File.separator + App.get().getString(R.string.app_name));
		}

		return null;
	}

	public static String getFileName(String url) {
		int pos = url.lastIndexOf("/");
		if (pos == -1) {
			return url;
		} else {
			return url.substring(pos + 1);
		}
	}

	public static Uri copyImage(InputStream in, String filename)
			throws IOException {
		File dir = getSaveImageDir();
		if (dir == null) {
			throw new FileNotFoundException();
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File path = new File(dir, filename);
		FileOutputStream out = new FileOutputStream(path);
		boolean result = copyImage(in, out);
		out.close();

		if (!result) {
			return null;
		}

		Cursor cursor = App
				.get()
				.getContentResolver()
				.query(Images.Media.EXTERNAL_CONTENT_URI, null,
						Images.Media.DATA + "=?",
						new String[] { path.getPath() }, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			ContentValues values = new ContentValues(8);
			values.put(Images.Media.TITLE, filename);
			values.put(Images.Media.DISPLAY_NAME, filename);
			values.put(Images.Media.DATE_TAKEN, new Date().getTime());
			values.put(Images.Media.MIME_TYPE, "image/jpeg");
			// values.put(Images.Media.ORIENTATION, oritentationCode);
			values.put(Images.Media.ORIENTATION, 0);
			values.put(Images.Media.BUCKET_ID, dir.getPath().hashCode());
			values.put(Images.Media.BUCKET_DISPLAY_NAME, filename.toLowerCase());
			values.put(Images.Media.DATA, path.getPath());
			App.get().getContentResolver()
					.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		}
		return Uri.fromFile(path);
	}

	public static boolean copyImage(InputStream in, FileOutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}
		out.flush();
		return true;
	}

}
