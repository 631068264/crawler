package com.web.utils;

import java.io.UnsupportedEncodingException;

import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Bin on 2015/3/12. 网页字符集嗅探器，用于猜测网页编码所使用的字符集
 */
public class CharacterTool {
	private static final Logger log = LoggerFactory.getLogger(CharacterTool.class);

	public static String guessEncoding(byte[] bytes) {
		String DEFAULT_ENCODING = "utf8";
		UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(bytes, 0, bytes.length);
		detector.dataEnd();
		String encoding = detector.getDetectedCharset();
		detector.reset();
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		}
		return encoding;
	}

	public static String reEncoding(String originString) {
		// html解决中文乱码
		try {
			byte[] htmlBytes = originString.getBytes("ISO-8859-1");
			String charset = CharacterTool.guessEncoding(htmlBytes);

			originString = new String(htmlBytes, charset);
		} catch (UnsupportedEncodingException ex) {
			log.info("exception:occur UnsupportedEncodingException while solving the problem of Chinese garbled");
		}
		return originString;
	}
}
