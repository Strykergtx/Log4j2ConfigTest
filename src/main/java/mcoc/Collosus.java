package mcoc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Collosus  {

    static String logString = "Collosus has 10 armour up buffs active, holding block";

    public static void run() {

        Logger logger = LogManager.getLogger(Collosus.class);

        logger.info("INFO FROM COLLOSUS");
        logger.debug("DEBUG FROM COLLOSUS");
        logger.fatal("FATAL FROM COLLOSUS");
        logger.error("ERROR FROM COLLOSUS");
        logger.warn("WARN FROM COLLOSUS");

        System.out.println(logger.getName());


    }

}
