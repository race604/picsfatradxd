package com.race604.picgallery.provider;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import android.text.TextUtils;
import android.util.Log;

import com.race604.http.HttpClient;
import com.race604.http.HttpException;
import com.race604.picgallery.Utils;

public class JandanOOXX implements IProvider {
	private static final String TAG = "JandanOOXX";

	public static final String HOST = "http://jandan.net/ooxx";
	private static final String PAGE_URL = HOST + "/page-%d";

	private int mPage;
	private MyNodeVisitor mVisitor = new MyNodeVisitor();

	@Override
	public List<ImageMeta> refresh() {
		try {
			
			String html = HttpClient.getInstance().get(HOST).asString();
			mPage = -1;
			List<ImageMeta> imgs = new ArrayList<ImageMeta>();
			List<ImageMeta> list = parse(html);
			if (list != null) {
				imgs.addAll(list);
			}
			
			list = next();
			if (list != null) {
				imgs.addAll(list);
			}
			return imgs;
		} catch (HttpException e) {
			Log.d("TAG", "get web page error");
		}

		return null;
	}

	@Override
	public List<ImageMeta> next() {

		try {
			String url = String.format(PAGE_URL, mPage - 1);
			Log.d(TAG, "url = " + url);
			String html = HttpClient.getInstance().get(url).asString();
			// String html = NetworkUtilis.getHttpString(
			// String.format(PAGE_URL, mPage - 1), null);
			return parse(html);
		} catch (HttpException e) {
			Log.d("TAG", "get web page error");
		}

		return null;
	}

	synchronized private List<ImageMeta> parse(String html) {
		if (TextUtils.isEmpty(html)) {
			return null;
		}

		Parser parser = new Parser();
		long time;

		try {
			time = System.currentTimeMillis();
			mVisitor.reset();
			parser.setInputHTML(html);
			parser.visitAllNodesWith(mVisitor);
			int page = mVisitor.getPage();
			if (mPage > 0 && mPage < page) {
				return null;
			}
			mPage = page;
			Log.d(TAG, "Timer: " + (System.currentTimeMillis() - time));
			return mVisitor.getImages();
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
