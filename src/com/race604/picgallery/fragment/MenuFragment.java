package com.race604.picgallery.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.race604.picgallery.R;
import com.race604.picgallery.provider.CollectionsInDB;
import com.race604.picgallery.provider.JandanOOXX;
import com.race604.picgallery.ui.ImageGridActivity;

public class MenuFragment extends ListFragment {
	
	private static final int[] ICONS = {R.drawable.collections_cloud, R.drawable.collections_view_as_grid};
	
	private static class MenuItem {
		public String name;
		public int iconRes;
		
		public MenuItem(String name, int iconResId) {
			this.name = name;
			this.iconRes = iconResId;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] items = getResources().getStringArray(R.array.menu_items);
		MenuAdapter adapter = new MenuAdapter(getActivity());
		int len = items.length;
		for (int i = 0; i < len; i++) {
			adapter.add(new MenuItem(items[i], ICONS[i]));
		}
		setListAdapter(adapter);
	}
	
	public class MenuAdapter extends ArrayAdapter<MenuItem> {

		public MenuAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).name);
			return convertView;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if (getActivity() instanceof ImageGridActivity) {
			ImageGridActivity activity = (ImageGridActivity) getActivity();
			switch (position) {
			case 0:
				activity.setImageProvider(JandanOOXX.getInstance());
				activity.showContent();
				break;
			case 1:
				activity.setImageProvider(CollectionsInDB.getInstance());
				activity.showContent();
				break;
			}
		}
	}

}
