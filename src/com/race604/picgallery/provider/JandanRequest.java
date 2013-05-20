package com.race604.picgallery.provider;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.race604.picgallery.Utils;

public class JandanRequest extends Request<NetworkData >{
	
	private static final String TAG = "JandanRequest";
	
	public static final String HOST = "http://jandan.net/ooxx";
	public static final String PAGE_URL = HOST + "/page-%d";
	
	private Listener<NetworkData > mListener;

	public JandanRequest(int method, String url, Listener<NetworkData> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
	}

	@Override
	protected Response<NetworkData> parseNetworkResponse(
			NetworkResponse response) {
		
		String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        
        return Response.success(parse(parsed), HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(NetworkData response) {
		mListener.onResponse(response);
	}
	
	private NetworkData parse(String html) {
		if (TextUtils.isEmpty(html)) {
			return null;
		}
		
		int page;
		MyNodeVisitor visitor = new MyNodeVisitor();

		Parser parser = new Parser();
		long time;

		try {
			time = System.currentTimeMillis();
			visitor.reset();
			parser.setInputHTML(html);
			parser.visitAllNodesWith(visitor);
			
			NetworkData data = new NetworkData();
			
			data.page = visitor.getPage();
			data.imgs = visitor.getImages();
			return data;
		} catch (ParserException e) {
			Log.e(TAG, "Parse html failed");
		}

		return null;
	}

	
	private class MyNodeVisitor extends NodeVisitor {

		private static final int STATE_NONE = 0;
		private static final int STATE_BEGAIN = 1;
		private static final int STATE_PAGE = 2;
		private static final int STATE_PAGE_DONE = 3;
		private static final int STATE_PAGE_IMG = 4;
		private static final int STATE_END = 5;

		private int mState = STATE_NONE;

		private int mPage;
		private List<ImageMeta> mImages = new ArrayList<ImageMeta>();

		public void reset() {
			mState = STATE_NONE;
			mPage = -1;
			mImages.clear();
		}

		public int getPage() {
			return mPage;
		}

		public List<ImageMeta> getImages() {
			return mImages;
		}

		@Override
		public void visitTag(Tag tag) {
			super.visitTag(tag);
			if (mState == STATE_NONE) {
				if (tag.getTagName().equals("DIV")
						&& "comments".equalsIgnoreCase(tag.getAttribute("id"))) {
					mState = STATE_BEGAIN;
				}
			} else if (mState == STATE_BEGAIN) {
				if (tag.getTagName().equals("SPAN")
						&& "current-comment-page".equalsIgnoreCase(tag
								.getAttribute("class"))) {
					mState = STATE_PAGE;
				}
			} else if (mState == STATE_PAGE_DONE) {
				if (tag.getTagName().equals("OL")
						&& "commentlist".equalsIgnoreCase(tag
								.getAttribute("class"))) {
					mState = STATE_PAGE_IMG;
				}
			} else if (mState == STATE_PAGE_IMG) {
				if (tag.getTagName().equals("IMG")
						&& tag.getAttributeEx("class") == null) {
					String url = tag.getAttribute("src");
					if (!TextUtils.isEmpty(url)) {
						mImages.add(new ImageMeta(tag.getAttribute("src")));
					}
				}
			}
		}

		@Override
		public void visitStringNode(Text string) {
			super.visitStringNode(string);
			if (mState == STATE_PAGE) {
				mPage = Integer.valueOf(Utils.extractDigits(string.getText()));
				mState = STATE_PAGE_DONE;
				Log.d(TAG, "state PAGE: " + mPage);
			}
		}

		@Override
		public void visitEndTag(Tag tag) {
			super.visitEndTag(tag);
			if (mState == STATE_PAGE_IMG) {
				if (tag.getTagName().equals("OL")) {
					mState = STATE_END;
				}
			}
		}

	}

}
