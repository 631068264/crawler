package com.web.controller;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.web.crawler.BaseCrawler;
import com.web.entity.CrawlerData;
import com.web.utils.RegexRule;

@Controller
public class GetDataAction extends BaseCrawler {

	public GetDataAction() {
		RegexRule regexRule = new RegexRule();
		regexRule.addRule("http://gz.jiajiao114.com/xueyuan/d\\-\\d+.html");
		// regexRule.addRule("-.*jpg.*");
		this.setRegexRules(regexRule);
		this.addSeed("http://gz.jiajiao114.com/xueyuan/d-127866.html");
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
		/*
		 * // 抽取文章标题 String title = document.select("body h2").get(0).text();
		 * 
		 * System.out.println(title); // 抽取文章附加信息，如发布时间和来源等 String extra_imfo =
		 * document.select("p[class=con_info]").text()
		 * .replaceAll(Jsoup.parse("&nbsp;").text(), " ");
		 * System.out.println(extra_imfo); // 抽取文章正文内容 String content =
		 * document.select("div[class=TRS_Editor]").html(); //
		 * System.out.println(content);
		 */

		Elements tables = document.select("table.xue_table tr td");
		int i = 0;
		StringBuilder str = new StringBuilder();
		for (Element table : tables) {
			str.append(table.text().replaceAll(Jsoup.parse("&nbsp;").text(), " ") + ",");
			i++;
			if (i > 22) {
				break;
			}
		}
		String[] src = str.toString().split(",");

		CrawlerData data = new CrawlerData();
		String[] t = src[5].split(" ");
		data.setStu_sex(t[0]);
		data.setGrade(t[1]);
		data.setSubject(src[3]);
		data.setStu_des(src[19]);
		data.setPay(src[7]);
		data.setAddress(src[15]);
		data.setUrl(url);
		data.setTea_des("教员性别：" + src[11] + " 教员要求：" + src[21]);
		data.setTime(src[17]);

		list.add(data);

	}

	@RequestMapping("/getData")
	public @ResponseBody
	List<CrawlerData> getData() {
		GetDataAction test = new GetDataAction();
		test.start(1);
		List<CrawlerData> datas = test.getList();
		return datas;
	}

}
