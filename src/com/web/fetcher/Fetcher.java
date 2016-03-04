package com.web.fetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.web.config.JedisConfig;
import com.web.entity.CrawlerData;
import com.web.model.FetchQueue;
import com.web.model.FetchQueueItem;
import com.web.model.TempQueue;
import com.web.utils.CharacterTool;
import com.web.utils.RegexRule;

/**
 * Created by Bin on 2015/3/2. Fetcher 抓取器类 Fetcher 抓取器线程类
 */
public class Fetcher {
	// 日志记录
	public static final Logger log = LoggerFactory.getLogger(Fetcher.class);
	// 抓取器启动线程数
	private int threads;
	// 抓取失败重试次数
	private int retry;
	// 当前抓取所在的深度
	private int depth;
	// 任务队列
	private FetchQueue fetchQueue;
	// 正则规则
	private RegexRule regexRule;
	// CustomizationFactory 定制器工厂
	CustomizationFactory customizationFactory = null;
	// CustomizationFactory 定制器
	Customization customization = null;

	private List<CrawlerData> list = new ArrayList<CrawlerData>();

	public List<CrawlerData> getList() {
		return list;
	}

	public void setList(List<CrawlerData> list) {
		this.list = list;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setRegexRule(RegexRule regexRule) {
		this.regexRule = regexRule;
	}

	public void setCustomizationFactory(CustomizationFactory customizationFactory) {
		this.customizationFactory = customizationFactory;
	}

	public void grab(int depth) {
		// 当前所在深度
		this.depth = depth;

		// 创建线程池
		ExecutorService threadPool = Executors.newFixedThreadPool(threads);
		FetcherThread[] fetcherThreads = new FetcherThread[threads];
		for (int i = 0; i < threads; i++) {
			fetcherThreads[i] = new FetcherThread();
			threadPool.execute(fetcherThreads[i]);// 未来调用线程
		}
		// 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。
		threadPool.shutdown();

		// 循环直到所有任务执行完
		while (true) {
			if (threadPool.isTerminated()) { // 如果关闭后所有任务都已完成，则返回 true。
				break;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				log.info("Exception: occur InterruptedException while executing Thread.sleep() in function Fetcher.grab()");
			}
		}

		// 将子目录队列tempQueue的fetchItem加入目录队列fetchQueue
		fetchQueue = FetchQueue.getFetchQueue();
		Iterator it = TempQueue.getFetchQueue().iterator();
		while (it.hasNext()) {
			fetchQueue.add((FetchQueueItem) it.next());
		}
	}

	private class FetcherThread extends Thread {

		// FetcherThread执行状态标志
		private boolean running;
		// 状态：执行中
		private static final boolean RUNNING = true;
		// 状态：停止
		private static final boolean STOPPED = false;
		// FetchQueue队列元素fetchQueueItem
		private FetchQueueItem fetchQueueItem = null;
		// 任务队列
		private FetchQueue fetchQueue = FetchQueue.getFetchQueue();
		// 临时队列（按所在深度构建）
		private TempQueue tempQueue = TempQueue.getFetchQueue();

		@Override
		public void run() {
			// 临时队列
			tempQueue = TempQueue.getFetchQueue();
			// 创建redis操作实例
			JedisPool jedisPool = new JedisPool(JedisConfig.address, JedisConfig.port);
			Jedis jedis = jedisPool.getResource();

			running = true;
			while (running) {
				fetchQueueItem = fetchQueue.poll();// 取头
				if (fetchQueueItem == null) {
					/**
					 * 另外的解决方案：也许存在优化空间，限制待执行任务队列长度，防止内存溢出 if(添种线程池任务执行中)
					 * Thread.sleep(time); else return;
					 */
					try {
						this.sleep(2000);
					} catch (InterruptedException e) {
						log.info("Exception:occur InterruptedException while executing Thread.sleep() in function FetcherThread.run()");
					}
					fetchQueueItem = fetchQueue.poll();
					if (fetchQueueItem == null)
						return;
				}
				// 通过url访问得到响应，分析响应取得doc和link，link线程入队
				HttpClient httpClient = new DefaultHttpClient();
				String url = null;
				try {
					HttpParams params = httpClient.getParams();
					HttpConnectionParams.setConnectionTimeout(params, 10000);
					HttpConnectionParams.setSoTimeout(params, 10000);
					url = fetchQueueItem.getUrl();
					jedis.set(url, JedisConfig.BEEN_GRABBED);
					HttpGet httpget = new HttpGet(url);
					HttpResponse response = httpClient.execute(httpget);
					HttpEntity entity = response.getEntity();
					String html = EntityUtils.toString(entity);

					// html解决中文乱码
					html = CharacterTool.reEncoding(html);
					Document doc = Jsoup.parse(html, url);

					// 提取网页中的链接（通过redis完成去重）
					Elements as = doc.select("a[href]");
					for (Element a : as) {
						String href = a.attr("abs:href");
						if (regexRule.satisfy(href)) {
							// 得到满足正则的url，接下来要验证redis数据库中是否已经存在，无则插redis并入队，有则抛弃
							if (!jedis.exists(href)) {
								jedis.set(href, JedisConfig.UN_GRABBED);
								tempQueue.add(new FetchQueueItem(href, depth));
							}
						}
					}
					customization = customizationFactory.createCustomization();
					if (customization != null) {
						// 调用个性化函数
						customization.customize(url, doc, list);

					}
				} catch (ClientProtocolException e) {
					log.info("exception: occur ClientProtocolException while visiting " + url);
					e.printStackTrace();
				} catch (IOException e) {
					log.info("exception: occur IOException while visiting " + url);
					e.printStackTrace();
				} catch (Exception e) {
					log.info("exception: occur UnknownException while visiting " + url
							+ " Please check Fetcher.java.");
					e.printStackTrace();
				} finally {
					httpClient.getConnectionManager().shutdown();
				}
			}
		}
	}
}
