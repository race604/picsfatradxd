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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;

import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.race604.picgallery.R;
import com.race604.picgallery.fragment.MenuFragment;
import com.race604.picgallery.provider.IProvider;
import com.race604.picgallery.provider.Images;
import com.race604.picgallery.provider.JandanOOXX;
import com.umeng.analytics.MobclickAgent;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not
 * much else.
 */
public class ImageGridActivity extends SlidingFragmentActivity {
	private static final String TAG = "ImageGridActivity";

	private ListFragment mMenuFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		
		setContentView(R.layout.image_grid);
		
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.grid, new ImageGridFragment(), TAG);
            ft.commit();
        }

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			mMenuFragment = new MenuFragment();
			t.replace(R.id.menu_frame, mMenuFragment);
			t.commit();
		} else {
			mMenuFragment = (ListFragment) this.getSupportFragmentManager()
					.findFragmentById(R.id.menu_frame);
		}

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Images.get().restoreFromCache(this);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Images.get().saveToCache(this);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ImageGridFragment fg = (ImageGridFragment) getSupportFragmentManager().findFragmentByTag(TAG);
		if (fg != null) {
			fg.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	public void setImageProvider(IProvider provider) {
		ImageGridFragment fg = (ImageGridFragment) getSupportFragmentManager().findFragmentByTag(TAG);
		if (fg != null) {
			fg.setImageProvider(provider);
		}
	}
	
}
