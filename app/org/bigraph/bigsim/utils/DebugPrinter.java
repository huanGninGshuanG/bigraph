package org.bigraph.bigsim.utils;

import org.slf4j.Logger;

public class DebugPrinter {
    private static boolean debug = true;

    public static void print(Logger logger, String info) {
        if (debug) {
            logger.debug(info);
        }
    }

    public static void err(Logger logger, String info) {
        if (debug) {
            logger.error(info);
        }
    }
}
