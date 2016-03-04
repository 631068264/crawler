package com.web.controller;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.web.crawler.BaseCrawler;
import com.web.entity.CrawlerData;
import com.web.utils.RegexRule;

@Controller
public class getGate extends BaseCrawler {

	public getGate() {
		RegexRule regexRule = new RegexRule();
		regexRule.addRule("http://www.gdagri.gov.cn/zwgk/tzgg/[0-9]+/t[0-9]+_[0-9]+.html");
		regexRule.addRule("-.*jpg.*");
		this.setRegexRules(regexRule);
		this.addSeed("http://www.gdagri.gov.cn/zwgk/tzgg/201503/t20150311_473756.html");
		this.setThreads(3);

	}

	/**
	 * 个性化定制内容，可以将实现精确抽取的代码写在此函数内（抽取规则参见Jsoup）
	 * 
	 * @param url
	 * @param document
	 */
	@Override
	public void customize(String url, Document document, List<CrawlerData> list) {
		// 抽取文章标题
		String title = document.select("body h2").get(0).text();

		System.out.println(title);
		// 抽取文章附加信息，如发布时间和来源等
		String extra_imfo = document.select("p[class=con_info]").text()
				.replaceAll(Jsoup.parse("&nbsp;").text(), " ");
		System.out.println(extra_imfo);
		// 抽取文章正文内容
		String content = document.select("div[class=TRS_Editor]").html();
		// System.out.println(content);

		// 获取信息入库
		/*
		 * CrawlerData gdagriCrawlerData = new CrawlerData();
		 * gdagriCrawlerData.setTitle(title);
		 * gdagriCrawlerData.setExtra_imfo(extra_imfo);
		 * gdagriCrawlerData.setContent(content);
		 * gdagriCrawlerData.setSource_url(url); list.add(gdagriCrawlerData);
		 */
		// dataService.save(gdagriCrawlerData);

	}

	@RequestMapping("/jj")
	public @ResponseBody
	List<CrawlerData> getData() {
		getGate test = new getGate();
		test.start(1);
		List<CrawlerData> datas = test.getList();
		return datas;
	}

}
