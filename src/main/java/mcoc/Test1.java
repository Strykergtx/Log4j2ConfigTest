package mcoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Test1 {


    public static void main(String[] args) throws IOException {


        //Example 1  - Doing it in this order, you see the logs after addLoggerWithResetAndLog do not work as configured in the log4j2.xml
        //Because the Configurator.reconfigure/initialize will replace the current configuration
       // ChampionLogger.addLoggerWithResetAndLog(Mephisto.logString, "Mephisto");
        Logger logger = LogManager.getLogger(Test1.class);

        logger.info("INFO FROM Accumulator");
        logger.debug("DEBUG FROM Accumulator");
        logger.fatal("FATAL FROM Accumulator");
        logger.error("ERROR FROM Accumulator");
        logger.warn("WARN FROM Accumulator");

        System.out.println(logger.getName());

        //Normal logging example
        Mephisto.run();
        Hyperion.run();
        Collosus.run();
        Thor.run();
    }
}
