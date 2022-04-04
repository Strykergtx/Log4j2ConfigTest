package mcoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hyperion {

    static String logString = "Hyperion gains power till SP3 and unleashes final attack, opponent is knocked out";

    public static void run() {

        Logger logger = LogManager.getLogger(Hyperion.class);

        logger.info("INFO FROM HYPERION");
        logger.debug("DEBUG FROM HYPERION");
        logger.fatal("FATAL FROM HYPERION");
        logger.error("ERROR FROM HYPERION");
        logger.warn("WARN FROM HYPERION");

        System.out.println(logger.getName());

    }
}
