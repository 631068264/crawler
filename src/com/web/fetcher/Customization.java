package com.web.fetcher;

import java.util.List;

import org.jsoup.nodes.Document;

import com.web.entity.CrawlerData;

/**
 * Created by Bin on 2015/3/7. Customization 定制类，根据需要对抓取的内容进行定制，精确抽取
 */
public interface Customization {
	/**
	 * 个性化定制内容，可以将实现精确抽取的代码写在此函数内
	 */
	public void customize(String url, Document document, List<CrawlerData> list);
}
