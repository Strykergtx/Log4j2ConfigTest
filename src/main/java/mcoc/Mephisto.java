package mcoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mephisto {

    static String logString = "Mephisto has incineration aura active and is melting opponent in radius 5 (in-game-distance)";

    public static void run() {

        Logger logger = LogManager.getLogger(Mephisto.class);

        logger.info("INFO FROM MEPHISTO");
        logger.debug("DEBUG FROM MEPHISTO");
        logger.fatal("FATAL FROM MEPHISTO");
        logger.error("ERROR FROM MEPHISTO");
        logger.warn("WARN FROM MEPHISTO");

        System.out.println(logger.getName());

    }
}
