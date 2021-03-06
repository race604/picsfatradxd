/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.race604.picgallery.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.race604.bitmapcache.ImageCache;
import com.race604.bitmapcache.ImageFetcher;
import com.race604.picgallery.BuildConfig;
import com.race604.picgallery.R;
import com.race604.picgallery.Utils;
import com.race604.picgallery.db.CollectionDao;
import com.race604.picgallery.provider.ImageMeta;
import com.race604.picgallery.provider.Images;
import com.umeng.analytics.MobclickAgent;

public class ImageDetailActivity extends SherlockFragmentActivity implements
		OnClickListener, OnPageChangeListener {
	private static final String IMAGE_CACHE_DIR = "images";
	public static final String EXTRA_IMAGE = "extra_image";

	private ImagePagerAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private ViewPager mPager;

	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);

		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// For this sample we'll use half of the longest width to resize our
		// images. As the
		// image scaling ensures the image is larger than this, we should be
		// left with a
		// resolution that is appropriate for both portrait and landscape. For
		// best image quality
		// we shouldn't divide by 2, but this will use more memory and require a
		// larger memory
		// cache.
		final int longest = (height > width ? height : width) / 2;

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, longest);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(false);

		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin((int) getResources().getDimension(
				R.dimen.image_detail_pager_margin));
		mPager.setOffscreenPageLimit(2);

		mPager.setOnPageChangeListener(this);

		// Set up activity to go full screen
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		// Enable some additional newer visibility and ActionBar features to
		// create a more
		// immersive photo viewing experience
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.collections_view_as_grid);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.hide();
		if (Utils.hasHoneycomb()) {
			// Hide and show the ActionBar as the visibility changes
			mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int vis) {
					if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
						actionBar.hide();
					} else {
						actionBar.show();
					}
				}
			});

			// Start low profile mode and hide ActionBar
			mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		}

		// Set the current item based on the extra passed in to this activity
		final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
		if (extraCurrentItem != -1) {
			mPager.setCurrentItem(extraCurrentItem);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.clear_cache:
			mImageFetcher.clearCache();
			Toast.makeText(this, R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.collect_image: {
			int idx = mPager.getCurrentItem();
			ImageMeta img = Images.get().getImage(idx);
			CollectionDao dao = CollectionDao.getInstance();
			dao.addOrUpdateCollection(img);
			Toast.makeText(this, R.string.collect_image_ok,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		case R.id.save_image: {
			int idx = mPager.getCurrentItem();
			String url = Images.get().getImage(idx).url;
			// String filename = Utils.getFileName(key);
			SavedTask task = new SavedTask();
			task.execute(url);
			return true;
		}
		case R.id.as_wallpaper: {
			int idx = mPager.getCurrentItem();
			String url = Images.get().getImage(idx).url;
			// String filename = Utils.getFileName(key);
			SetAsTask task = new SetAsTask();
			task.execute(url);
			return true;
		}
		case R.id.share_image: {
			int idx = mPager.getCurrentItem();
			String url = Images.get().getImage(idx).url;
			// String filename = Utils.getFileName(key);
			SendImageTask task = new SendImageTask();
			task.execute(url);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void finish() {
		int position = mPager.getCurrentItem();
		final Intent i = new Intent();
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, position);
		setResult(Activity.RESULT_OK, i);
		super.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.detail_menu, menu);
		return true;
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public ImagePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return Images.get().getCount();
		}

		@Override
		public Fragment getItem(int position) {
			if (position < Images.get().getCount()) {
				return ImageDetailFragment.newInstance(Images.get().getImage(
						position).url);
			} else {
				return ImageDetailFragment.newInstance(null);
			}
		}
	}

	/**
	 * Set on the ImageView in the ViewPager children fragments, to
	 * enable/disable low profile mode when the ImageView is touched.
	 */
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		if (Utils.hasHoneycomb()) {
			final int vis = mPager.getSystemUiVisibility();
			if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
				mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			} else {
				mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
		} else {
			ActionBar actionBar = getSupportActionBar();
			if (actionBar.isShowing()) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}

	class SaveImageTask extends AsyncTask<String, Void, Uri> {

		@Override
		protected Uri doInBackground(String... params) {
			if (params == null || params.length <= 0) {
				return null;
			}

			String filename = Utils.getFileName(params[0]);
			return mImageFetcher.saveImage(params[0], filename);
		}

	}

	class SavedTask extends SaveImageTask {

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);

			if (result != null) {
				String msg = getString(R.string.saved_image_toast)
						+ result.getPath();
				Toast.makeText(ImageDetailActivity.this, msg,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	class SetAsTask extends SaveImageTask {

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);

			if (result != null) {
				Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
				intent.setDataAndType(result, "image/jpg");
				intent.putExtra("mimeType", "image/jpg");
				startActivity(intent);

			}
		}
	}

	class SendImageTask extends SaveImageTask {

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);

			if (result != null) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/jpeg");
				intent.putExtra(Intent.EXTRA_STREAM, result);
				startActivity(intent);

			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		if (position >= Images.get().getCount()) {
		}

	}

}
