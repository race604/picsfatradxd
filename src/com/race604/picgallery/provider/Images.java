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

package com.race604.picgallery.provider;

import java.util.ArrayList;
import java.util.List;

import com.race604.picgallery.FileUtils;

import android.content.Context;

/**
 * Some simple test data to use for this sample app.
 */
public class Images {

	private static final String CACHED_FILE_NAME = "provider.images";

	private static Images mInstance;

	private List<ImageMeta> mImages;

	private Images() {
	}

	public static Images get() {
		if (mInstance == null) {
			mInstance = new Images();
		}

		return mInstance;
	}

	public int getCount() {
		if (mImages == null) {
			return 0;
		}
		return mImages.size();
	}

	public ImageMeta getImage(int position) {
		if (position < getCount()) {
			return mImages.get(position);
		}
		return null;
	}

	public void addImages(List<ImageMeta> imgs) {
		if (mImages == null) {
			mImages = new ArrayList<ImageMeta>();
		}
		mImages.addAll(imgs);
	}

	public void clear() {
		if (mImages != null) {
			mImages.clear();
		}
	}

	public void saveToCache(Context ctx) {
		FileUtils.saveObjectToCache(ctx, CACHED_FILE_NAME, mImages);
	}

	public void restoreFromCache(Context ctx) {
		mImages = (List<ImageMeta>) FileUtils.readObjectFromCache(ctx,
				CACHED_FILE_NAME);
	}
}
