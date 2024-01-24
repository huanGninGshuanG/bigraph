package org.bigraph.bigsim.utils;

import org.slf4j.Logger;

public class DebugPrinter {
    private static boolean debug = true;
    private static boolean info = true;

    public static void print(Logger logger, String info) {
        if (debug) {
            logger.debug(info);
        }
    }

    public static void err(Logger logger, String info) {
        logger.error(info);
    }

    public static void printInfo(Logger logger, String str) {
        if (info) logger.info(str);
    }
}
