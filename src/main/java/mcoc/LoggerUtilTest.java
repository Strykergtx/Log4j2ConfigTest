package com.thereactiveweb.esp.util;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.activeinsight.common.util.LogUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author dotan
 *
 */
public class LoggerUtilTest {

	private static final Logger logger = LogManager.getLogger(LoggerUtilTest.class);

	private static final int MAX_WORKERS = 10;
	private static final int MAX_HITS_PER_WORKER = 10543;
	private static final String OUTPUT_FOLDER = "/tmp/";
	private static final String LOG_FILE_NAME_SUFFIX = "_data.log";

	@SuppressWarnings("deprecation")
	@BeforeClass
	public static void beforeClass() {
		LogUtils.configure(Level.DEBUG);
		LoggerUtil.setMaxFileSize("2KB");
	}
	
	@Before
	public void before() {
		cleanLogFiles();
	}

	@After
	public void after() {
		cleanLogFiles();
	}

	@Test
	public void loadTest() throws Exception {
		int totalHits = MAX_HITS_PER_WORKER * MAX_WORKERS;
		ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);
		CountDownLatch latch = new CountDownLatch(totalHits);

		int workerIndex = 0;
		for (int hitsCounter = 0; hitsCounter < totalHits; hitsCounter++) {
			LogWorker worker = new LogWorker(latch, OUTPUT_FOLDER, workerIndex + LOG_FILE_NAME_SUFFIX);
			workerIndex++;
			if (workerIndex >= MAX_WORKERS) {
				workerIndex = 0;
			} 
			
			executor.execute(worker);
		}

		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.MINUTES);
		Assert.assertTrue("some workers did not complete", latch.getCount() == 0);

		for (int logIndex=0; logIndex < MAX_WORKERS; logIndex++) {
			Assert.assertEquals("log " + logIndex + " count error", MAX_HITS_PER_WORKER, getLogFileDataCount(logIndex));
		}

	}

	private int getLogFileDataCount(int logIndex) {
		File folder = new File(OUTPUT_FOLDER);
		File[] logFiles = folder.listFiles(new LogFileNameFilter(logIndex));
		logger.debug("found " + logFiles.length + " log files for log index " + logIndex);
		int totalDataCount = 0;
		for (File f: logFiles) {
			int fileDataCount = getLogFileDataCount(f);
			logger.debug("file " + f.getName() + " contains " + fileDataCount + " lines");
			totalDataCount += fileDataCount;
		}
		logger.debug("found " + logFiles.length + " log files for log index " + logIndex + " with " + totalDataCount + " lines");
		return totalDataCount;
	}
	
	private int getLogFileDataCount(File f) {
		try {
			LineNumberReader  lnr = new LineNumberReader(new FileReader(f));
			int numberOfRecords = 0;
			String line = null;
			while (((line = lnr.readLine())) != null){
				if(!line.trim().isEmpty()){
					numberOfRecords++;
				}
			}
			lnr.close();
			return numberOfRecords;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private int getLogFilesCount() {
		File folder = new File(OUTPUT_FOLDER);
		File[] logFiles = folder.listFiles(new LogFileNameFilter());

		Set<String> uniqueLogFileName = new HashSet<String>();
		for (File f : logFiles) {
			String name = f.getName().substring(0, f.getName().indexOf("."));
			uniqueLogFileName.add(name);
		}

		return uniqueLogFileName.size();
	}

	private void cleanLogFiles() {
		File folder = new File(OUTPUT_FOLDER);
		File[] logFiles = folder.listFiles(new LogFileNameFilter());
		
		for (File f : logFiles) {
			try {
				Files.delete(f.toPath());
			} catch (IOException e) {
				logger.error("faled to delete " + f.getPath() + " " + e);
			}
		}
	}

	private class LogFileNameFilter implements FilenameFilter {

		private int logIndex = -1;

		public LogFileNameFilter(int logIndex) {
			this.logIndex = logIndex;
		}

		public LogFileNameFilter() {
		}

		@Override
		public boolean accept(File dir, String name) {
			
			return logIndex < 0? name.contains(LOG_FILE_NAME_SUFFIX): name.contains(logIndex + LOG_FILE_NAME_SUFFIX);
		}

	}
}
