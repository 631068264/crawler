package com.web.crawler;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.web.config.JedisConfig;
import com.web.entity.CrawlerData;
import com.web.fetcher.Customization;
import com.web.fetcher.CustomizationFactory;
import com.web.fetcher.Fetcher;
import com.web.model.FetchQueue;
import com.web.model.FetchQueueItem;
import com.web.utils.RegexRule;

/**
 * Created by Bin on 2015/3/2.
 */
public class BaseCrawler implements Customization, CustomizationFactory {
	// 日志记录
	public static final Logger log = LoggerFactory.getLogger(BaseCrawler.class);
	// 爬虫入口种子
	private ArrayList<String> seeds = new ArrayList<String>();
	// 爬虫目录种子（爬取时无条件入队）
	private ArrayList<String> catalogSeeds = new ArrayList<String>();
	// 爬虫正则规则（含正正则和负正则）
	private RegexRule regexRules = new RegexRule();
	// 爬虫开启线程数
	private int threads;
	// 网页抓取器
	private Fetcher fetcher;
	// 定时任务标志（true则保持非关系数据库缓存）
	private boolean timingTask;
	// redis主机地址（默认127.0.0.1）
	private String jedisAddress;
	// redis端口号（默认6379）
	private String port;
	// 爬虫状态
	private boolean status;
	// 失败重试次数（默认为3次）
	private int retry = 3;
	// CustomizationFactory 定制器工厂
	CustomizationFactory customizationFactory = this;

	// 状态：执行中
	private static final boolean RUNNING = true;
	// 状态：停止
	private static final boolean STOPPED = false;

	private List<CrawlerData> list = new ArrayList<CrawlerData>();

	public List<CrawlerData> getList() {
		return list;
	}

	public void setList(List<CrawlerData> list) {
		this.list = list;
	}

	public void setSeeds(ArrayList<String> seeds) {
		this.seeds = seeds;
	}

	public void setCatalogSeeds(ArrayList<String> catalogSeeds) {
		this.catalogSeeds = catalogSeeds;
	}

	public void setRegexRules(RegexRule regexRules) {
		this.regexRules = regexRules;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void setTimingTask(boolean timingTask) {
		this.timingTask = timingTask;
	}

	public void setJedisAddress(String jedisAddress) {
		this.jedisAddress = jedisAddress;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public void addSeed(String seed) {
		seeds.add(seed);
	}

	@Override
	public Customization createCustomization() {
		return this;
	}

	@Override
	public void customize(String url, Document document, List<CrawlerData> list) {
		return;
	}

	/**
	 * 
	 * @param depth
	 *            遍历深度
	 */
	public void start(int depth) {
		// 创建网络连接池
		JedisPool jedisPool = new JedisPool(JedisConfig.address, JedisConfig.port);
		Jedis jedis = jedisPool.getResource();
		// 爬虫启动需要至少一个种子url
		if (seeds.isEmpty() && catalogSeeds.isEmpty()) {
			log.info("error: you must add at least one seed.");
			return;
		}
		// 创建任务队列，注入目录种子和普通种子
		FetchQueue fetchQueue = FetchQueue.getFetchQueue();
		if (!catalogSeeds.isEmpty()) {
			for (String catalogSeed : catalogSeeds) {// 遍历URL目录
				FetchQueueItem fetchQueueItem = new FetchQueueItem(catalogSeed);
				fetchQueue.add(fetchQueueItem);
				// 标记目录
				jedis.set(fetchQueueItem.getUrl(), JedisConfig.CATALOG_SEED);
			}
		}
		if (!seeds.isEmpty()) {
			for (String seed : seeds) {
				FetchQueueItem fetchQueueItem = new FetchQueueItem(seed);
				if (!jedis.exists(fetchQueueItem.getUrl())) {
					jedis.set(fetchQueueItem.getUrl(), JedisConfig.COMMON_SEED);
					fetchQueue.add(fetchQueueItem);
				}
			}
		}
		status = RUNNING;
		for (int i = 0; i < depth; i++) {
			if (status == STOPPED) {
				break;
			}
			log.info("BaseCrawler starts grabbing in depth " + (i + 1));
			Fetcher fetcher = new Fetcher();
			fetcher.setRetry(retry);
			fetcher.setThreads(threads);
			fetcher.setRegexRule(regexRules);
			fetcher.setCustomizationFactory(customizationFactory);
			fetcher.grab(i + 1);

			List<CrawlerData> datas = fetcher.getList();

			for (CrawlerData data : datas) {
				list.add(data);

			}

			log.info("BaseCrawler finished grabbing in depth " + (i + 1));
		}
		log.info("Grabbing Task Is Completed.");
	}
}
