package mcoc;

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
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChampionLogger {

    private static Map<String, Logger> loggers = new HashMap<>();

    public static void addLoggerWithResetAndLog(String logString,String fileName) throws IOException {
          System.out.println(logString);

          Logger logger =loggers.get(fileName);
          if(logger==null)
          {

              System.out.println("Logger does not exist, creating a new one");

              ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

              //Created A ROLLING FILE APPENDER AND ADDED IT TO BUILDER
              ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
                      .addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
                      .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));
              AppenderComponentBuilder appenderBuilder = builder.newAppender("rolling", "RollingFile")
                      .addAttribute("fileName", "logs/"+fileName+".log")
                      .addAttribute("filePattern", "target/archive/rolling-%d{MM-dd-yy}.log.gz")
                      .add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"))
                      .addComponent(triggeringPolicy);
              builder.add(appenderBuilder);

              //Creating a Logger Component and adding it to Logger
              LoggerComponentBuilder loggerComponent = builder.newLogger(fileName, Level.TRACE);
              loggerComponent.add(builder.newAppenderRef("rolling"));
              loggerComponent.addAttribute("additivity", false);
              builder.add(loggerComponent);

              //just to check xml configuration
              builder.writeXmlConfiguration(System.out);

              //This is gonna reset the current configuration with the one we just created
              Configurator.initialize(builder.build());
              //This also has the same effect
              //Configurator.reconfigure(builder.build());

              //get the logger back, now that it was added and log something to it
              logger = LogManager.getLogger(fileName);
              logger.log(logger.getLevel(),logString);

              //store it in key value pair
              loggers.put(fileName,logger);

          }
          else
          {
              System.out.println("Logger exists, reusing the previously created Logger Configuration");
              logger.log(logger.getLevel(), logString);

          }


    }

    public static void addLoggerWithModificationAndLog(String logString,String fileName) throws IOException {
        System.out.println(logString);

        Logger logger =loggers.get(fileName);
        if(logger==null)
        {
            System.out.println("Logger does not exist, creating a new one");

            //We use LoggerContext Here
            final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            final Configuration config = ctx.getConfiguration();

            //RollingFileAppender Policies,Strategy and Layout
            TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy.newBuilder().withInterval(1).withModulate(true).build();
            SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy.createPolicy("200");
            //DefaultRolloverStrategy   defaultRolloverStrategy  = DefaultRolloverStrategy.newBuilder().withMax("2").build();

            PathCondition[] nestedConditions  = PathCondition.copy(IfAccumulatedFileCount.createFileCountCondition(3));
            //for depth 1, under base folder
//            PathCondition[] pathConditions = PathCondition.copy(IfFileName.createNameCondition(fileName+"-*.log.gz" ,null,nestedConditions));

            //For sub-folders with depth-2
            PathCondition[] pathConditions = PathCondition.copy(IfFileName.createNameCondition("*/"+fileName+"-*.log.gz" ,null,nestedConditions));

            Action deleteAction =DeleteAction.createDeleteAction("logs/",false,2,false,null,pathConditions,null,config);
            Action[] actions = new Action[] {deleteAction};
            DefaultRolloverStrategy defaultRolloverStrategy =DefaultRolloverStrategy.newBuilder().withMax("2").withCustomActions(actions).build();

            PatternLayout layout = PatternLayout.newBuilder().withPattern("%d %p %c{1.} [%t] %m%n").build();

            //The actual RollingFileAppender construction
            final RollingFileAppender rollingFileAppender = RollingFileAppender.newBuilder()
                    .setName("RollingFileAppenderFor"+fileName)
                    .withFileName("logs/"+ fileName+".log")
                   .withFilePattern("logs/%d{yyyy-MM-dd-HH-mm}/"+ fileName +"-%d{yyyy-MM-dd-HH-mm}-%i.log.gz")
                  //  .withFilePattern("logs/"+ fileName +"-%d{yyyy-MM-dd-HH-mm}.log.%i.gz")
                    .withAppend(true)
                    .setConfiguration(config)
                    .setLayout(layout)
                    .withPolicy(CompositeTriggeringPolicy.createPolicy(timeBasedTriggeringPolicy,sizeBasedTriggeringPolicy))
                    .withStrategy(defaultRolloverStrategy)
                    .build();


            //Add the RollingFileAppender just created to the current config(Modifying the XML config)
            rollingFileAppender.start();
            config.addAppender(rollingFileAppender);

            //Create Appender Reference Array, add the RollingFileAppender Name just created to it
            AppenderRef ref = AppenderRef.createAppenderRef("RollingFileAppenderFor"+fileName, null, null);
            AppenderRef[] refs = new AppenderRef[] {ref};

            //Create the Logger for the specific Name
            LoggerConfig loggerConfig = LoggerConfig.createLogger(false,Level.INFO,fileName,"false",refs,null,config,null);
            loggerConfig.addAppender(rollingFileAppender, null, null);
            config.addLogger(fileName,loggerConfig);
            ctx.updateLoggers();

//            AsyncAppender asyncAppender = AsyncAppender.newBuilder().setName("AsyncAppenderFor"+fileName)
//                    .setBufferSize(500)
//                    .setConfiguration(config)
//                    .setAppenderRefs(refs).build();
//
//            asyncAppender.start();
//            config.addAppender(asyncAppender);

            //Get the logger back from LogManager and use it to log
            logger = LogManager.getLogger(fileName);
            logger.log(logger.getLevel(),logString);

            //store it in key value pair
            loggers.put(fileName,logger);

        }
        else
        {
            System.out.println("Logger exists, reusing the previously created Logger Configuration");
            logger.log(logger.getLevel(), logString);

        }


    }
}
