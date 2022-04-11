package mcoc;

import java.io.IOException;

public class Test3 {

    public static void main(String[] args) throws IOException {
        ChampionLogger.addLoggerWithModificationAndLog(Mephisto.logString,"Mephisto");
        Mephisto.run();
    }
}
