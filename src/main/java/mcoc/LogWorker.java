package com.thereactiveweb.esp.util;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author dotan
 *
 */
public class LogWorker implements Runnable {

	private CountDownLatch latch = null;
	private String fileDir = null;
	private String fileName = null;

	public LogWorker(CountDownLatch latch, String fileDir, String fileName) {
		this.latch = latch;
		this.fileDir = fileDir;
		this.fileName = fileName;
	}

	@Override
	public void run() {
		try {
			Logger logger = LoggerUtil.getLogger(fileDir, fileName, false);
			logger.trace("writing " + System.currentTimeMillis() + " to " + logger.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			latch.countDown();
		}
		
	}
}
