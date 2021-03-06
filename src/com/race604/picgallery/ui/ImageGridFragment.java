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

import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.race604.bitmapcache.ImageCache.ImageCacheParams;
import com.race604.bitmapcache.ImageFetcher;
import com.race604.picgallery.BuildConfig;
import com.race604.picgallery.R;
import com.race604.picgallery.Utils;
import com.race604.picgallery.provider.IProvider;
import com.race604.picgallery.provider.ImageMeta;
import com.race604.picgallery.provider.Images;
import com.race604.picgallery.provider.JandanOOXX;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends Fragment implements
		AdapterView.OnItemClickListener, OnRefreshListener2<GridView> {
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private static final int REQUEST_CODE = 1000;

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	
	private IProvider mImageProvider = JandanOOXX.getInstance();

	private GetImageTask mTask;
	private PullToRefreshGridView mPullRefreshGridView;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public ImageGridFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		mAdapter = new ImageAdapter(getActivity());

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
				cacheParams);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.image_grid_fragment,
				container, false);
		mPullRefreshGridView = (PullToRefreshGridView) v
				.findViewById(R.id.gridView);
		mPullRefreshGridView.setOnRefreshListener(this);
		mPullRefreshGridView.setAdapter(mAdapter);
		mPullRefreshGridView.setOnItemClickListener(this);
		mPullRefreshGridView
				.setOnScrollListener(new AbsListView.OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsListView absListView,
							int scrollState) {
						// Pause fetcher to ensure smoother scrolling when
						// flinging
						if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
							mImageFetcher.setPauseWork(true);
						} else {
							mImageFetcher.setPauseWork(false);
						}
					}

					@Override
					public void onScroll(AbsListView absListView,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
					}
				});

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		mPullRefreshGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math
									.floor(mPullRefreshGridView.getWidth()
											/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (mPullRefreshGridView
										.getWidth() / numColumns)
										- mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
								if (BuildConfig.DEBUG) {
									Log.d(TAG,
											"onCreateView - numColumns set to "
													+ numColumns);
								}
							}
						}
					}
				});
		loadImage(true);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@TargetApi(16)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
		if (Utils.hasJellyBean()) {
			// makeThumbnailScaleUpAnimation() looks kind of ugly here as the
			// loading spinner may
			// show plus the thumbnail image in GridView is cropped. so using
			// makeScaleUpAnimation() instead.
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v,
					0, 0, v.getWidth(), v.getHeight());
			getActivity().startActivityForResult(i, REQUEST_CODE, options.toBundle());
		} else {
			startActivityForResult(i, REQUEST_CODE);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			mImageFetcher.clearCache();
			Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && data != null) {
			int position = data.getIntExtra(ImageDetailActivity.EXTRA_IMAGE, -1);
			if (position >= mAdapter.getCount()) {
				mAdapter.notifyDataSetChanged();
			}
			if (position >= 0) {
				mPullRefreshGridView.getRefreshableView().smoothScrollToPosition(position);
				mAdapter.notifyDataSetChanged();
				mPullRefreshGridView.requestLayout();
			}
		}
	}

	/**
	 * The main adapter that backs the GridView. This is fairly standard except
	 * the number of columns in the GridView is used to create a fake top row of
	 * empty views as we use a transparent ActionBar and don't want the real top
	 * row of images to start off covered by it.
	 */
	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			// Calculate ActionBar height
			TypedValue tv = new TypedValue();
			
			if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
			
			if (mActionBarHeight == 0 && context.getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize,
					tv, true)) {
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
			}
		}

		@Override
		public int getCount() {
			// Size + number of columns for top empty row
			return Images.get().getCount() + mNumColumns;
		}

		@Override
		public Object getItem(int position) {
			return position < mNumColumns ? null : Images.get().getImage(
					position - mNumColumns);
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			// First check if this is the top row
			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height of ActionBar
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
				return convertView;
			}

			// Now handle the main ImageView thumbnails
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				imageView = new RecyclingImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else { // Otherwise re-use the converted view
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs
			mImageFetcher.loadImage(
					Images.get().getImage(position - mNumColumns).url,
					imageView);
			return imageView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}

	private void loadImage(boolean refresh) {
		if (mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED) {
			mTask = new GetImageTask();
			mTask.execute(refresh);
		}

		// String url;
		// if (refresh) {
		// mPage = -1;
		// url = JandanRequest.HOST;
		// } else {
		// url = String.format(JandanRequest.PAGE_URL, mPage - 1);
		// }
		//
		// VolleyHelper.getRequestQueue().add(
		// new JandanRequest(Method.GET, url, new Listener<NetworkData>() {
		//
		// @Override
		// public void onResponse(NetworkData response) {
		// final int page = response.page;
		// mPage = page;
		//
		// Images.get().addImages(response.imgs);
		// mAdapter.notifyDataSetChanged();
		//
		// mPullRefreshGridView.onRefreshComplete();
		// }
		// }, new ErrorListener() {
		//
		// @Override
		// public void onErrorResponse(VolleyError error) {
		// mPullRefreshGridView.onRefreshComplete();
		// }
		// }));
	}

	private class GetImageTask extends
			AsyncTask<Boolean, Void, List<ImageMeta>> {

		private boolean mIsRefresh = true;

		@Override
		protected List<ImageMeta> doInBackground(Boolean... params) {
			if (params.length > 0) {
				mIsRefresh = params[0];
			}
			if (mIsRefresh) {
				return mImageProvider.refresh();
			} else {
				return mImageProvider.next();
			}
		}

		@Override
		protected void onPostExecute(List<ImageMeta> result) {
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				if (mIsRefresh) {
					Images.get().clear();
				}
				Images.get().addImages(result);
				mAdapter.notifyDataSetChanged();
			}
			mPullRefreshGridView.onRefreshComplete();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mPullRefreshGridView.setRefreshing();
		}

	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
		loadImage(true);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
		loadImage(false);
	}
	
	public void setImageProvider(IProvider provider) {
		if (provider != null && mImageProvider != provider) {
			mImageProvider = provider;
			loadImage(true);
		}
	}
}
