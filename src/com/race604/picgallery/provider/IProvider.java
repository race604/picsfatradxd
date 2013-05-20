package com.race604.picgallery.provider;

import java.util.List;

public interface IProvider {
	public List<ImageMeta> refresh();
	public List<ImageMeta> next();
}
