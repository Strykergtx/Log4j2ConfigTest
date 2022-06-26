package com.thereactiveweb.esp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.*;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * @author dotan
 *
 */
public class LoggerUtil {

	private static Map<String, Logger> loggers = new HashMap<String, Logger>();
	private static String maxFileSize = "10MB";

	public static Logger getLogger(String fileDir, String fileName, boolean isAsync) throws Exception {
		// make sure path ends with '/'
		fileDir = !fileDir.endsWith("/")? fileDir + "/": fileDir;
		String filePath = fileDir + fileName;
		// get existing Logger
		Logger res = loggers.get(filePath);
		// if no logger found create and store new Logger
		if (res == null) {
			res = createNewLogger(fileDir, fileName, isAsync);
			loggers.put(filePath, res);
		}
		// return the Logger
		return res;
	}

	private static synchronized Logger createNewLogger(String fileDir, String fileName, boolean isAsync) throws Exception {
		// make sure directory exists, do not create new directories
		File folder = new File(fileDir);
		if (!folder.exists()) {
			throw new FileNotFoundException("folder doesn't exisits " + folder.getName());
		}

		// get the full path
		String filePath = fileDir + fileName;
		// if logger already exists return the logger
		// since getLogger is not synchronized this can happen
		Logger res = loggers.get(filePath);
		if (res != null) {
			return res;
		}
		// create the logger
		//log4j2 modified
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy.newBuilder().withInterval(1).withModulate(true).build();
		SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy.createPolicy(maxFileSize);
		PathCondition[] nestedConditions  = PathCondition.copy(IfAccumulatedFileCount.createFileCountCondition(1000));
		PathCondition[] pathConditions = PathCondition.copy(IfFileName.createNameCondition(fileName+"-*.log.gz" ,null,nestedConditions));

		Action deleteAction = DeleteAction.createDeleteAction(fileDir,false,1,false,null,pathConditions,null,config);
		Action[] actions = new Action[] {deleteAction};
		DefaultRolloverStrategy defaultRolloverStrategy =DefaultRolloverStrategy.newBuilder().withCustomActions(actions).build();

		PatternLayout layout = PatternLayout.newBuilder().withPattern("%m%n").build();

		//The actual RollingFileAppender construction
		final RollingFileAppender rollingFileAppender = RollingFileAppender.newBuilder()
				.setName("RollingFileAppenderFor"+fileName)
				.withFileName(fileDir + fileName)
				.withFilePattern(fileDir + fileName.substring(0,fileName.length()-4) + "-%d{yyyy-MM-dd}-%i.log.gz")
				.withAppend(true)
				.setConfiguration(config)
				.setLayout(layout)
				.withPolicy(CompositeTriggeringPolicy.createPolicy(timeBasedTriggeringPolicy,sizeBasedTriggeringPolicy))
				.withStrategy(defaultRolloverStrategy)
				.build();

		//Add the RollingFileAppender just created to the current config(Modifying the XML config)
		rollingFileAppender.start();
		config.addAppender(rollingFileAppender);
		//Create Appender Reference Array, add the RollingFileAppender name to it
		AppenderRef ref = AppenderRef.createAppenderRef("RollingFileAppenderFor"+fileName, null, null);
		AppenderRef[] refs = new AppenderRef[] {ref};
		//Create the Logger for the specific fileName
		LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.TRACE,fileName,"false",refs,null,config,null);
		loggerConfig.addAppender(rollingFileAppender, null, null);
		config.addLogger(fileName,loggerConfig);

		if (isAsync) {
			AsyncAppender asyncAppender = AsyncAppender.newBuilder().setName("AsyncAppenderFor"+fileName)
					.setBufferSize(500)
					.setConfiguration(config)
					.setAppenderRefs(refs).build();

			asyncAppender.start();
			config.addAppender(asyncAppender);
		}

		ctx.updateLoggers();
		// return the Logger
		return LogManager.getLogger(fileName);
	}

	public static String getMaxFileSize() {
		return maxFileSize;
	}

	public static void setMaxFileSize(String maxFileSize) {
		LoggerUtil.maxFileSize = maxFileSize;
	}

}
