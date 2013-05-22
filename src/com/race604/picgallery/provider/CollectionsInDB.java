package com.race604.picgallery.provider;

import java.util.List;

import com.race604.picgallery.db.CollectionDao;

public class CollectionsInDB implements IProvider{
	
	private static final int NUM_PER_PAGE = 50;
	
	private int mIndex = 0;
	
	private static CollectionsInDB mInstance;
	
	private CollectionsInDB() {};
	
	public static CollectionsInDB getInstance() {
		if (mInstance == null) {
			mInstance = new CollectionsInDB();
		}
		return mInstance;
	}

	@Override
	public List<ImageMeta> refresh() {
		List<ImageMeta> list = CollectionDao.getInstance().getImages(0, NUM_PER_PAGE);
		mIndex = NUM_PER_PAGE;
		return list;
	}

	@Override
	public List<ImageMeta> next() {
		List<ImageMeta> list = CollectionDao.getInstance().getImages(mIndex, NUM_PER_PAGE);
		mIndex += NUM_PER_PAGE;
		return list;
	}

}
