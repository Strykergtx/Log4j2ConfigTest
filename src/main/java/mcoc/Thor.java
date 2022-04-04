package mcoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Thor {

    static String logString = "Thor parried and has 2 furies active, sp1 activated";

    public static void run() {

        Logger logger = LogManager.getLogger(Thor.class);

        logger.info("INFO FROM THOR");
        logger.debug("DEBUG FROM THOR");
        logger.fatal("FATAL FROM THOR");
        logger.error("ERROR FROM THOR");
        logger.warn("WARN FROM THOR");

        System.out.println(logger.getName());

    }
}
