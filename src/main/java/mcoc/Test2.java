package mcoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Test2 {

    public static void main(String[] args) throws IOException {

        //Example 2 -Modifying current config , NO RESET
        ChampionLogger.addLoggerWithModificationAndLog(Mephisto.logString,"Mephisto");
        ChampionLogger.addLoggerWithModificationAndLog(Collosus.logString,"Collosus");
        ChampionLogger.addLoggerWithModificationAndLog(Hyperion.logString,"Hyperion");
        ChampionLogger.addLoggerWithModificationAndLog(Thor.logString,"Thor");

        //use the already existing loggers
        ChampionLogger.addLoggerWithModificationAndLog(Collosus.logString,"Collosus");
        ChampionLogger.addLoggerWithModificationAndLog(Hyperion.logString,"Hyperion");

        //Notice what happens when you do this -
        //The logger for mcoc.Mephisto was never configured in XML, but here it gets configured and the future calls use this
        ChampionLogger.addLoggerWithModificationAndLog(Mephisto.logString,"mcoc.Mephisto");

        Logger logger = LogManager.getLogger(Test2.class);
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
