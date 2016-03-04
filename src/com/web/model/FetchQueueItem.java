package com.web.model;

/**
 * Created by Bin on 2015/3/2.
 */
public class FetchQueueItem {
	// 网页URL地址
	private String url;
	// 网页所处深度，目录种子catalogSeeds深度设为0
	private int layer;

	public FetchQueueItem() {

	}

	public FetchQueueItem(String url) {
		this.setUrl(url);
		this.setLayer(0);
	}

	public FetchQueueItem(String url, int layer) {
		this.setUrl(url);
		this.setLayer(layer);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}
}
