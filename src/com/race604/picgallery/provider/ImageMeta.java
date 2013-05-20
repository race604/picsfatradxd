package com.race604.picgallery.provider;

import java.io.Serializable;

public class ImageMeta implements Serializable{
	
	public String url; 
	
	public ImageMeta() {}
	
	public ImageMeta(String url) {
		this.url = url;
	}
}
