package com.bohutskyi.logloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSDetector.class);
    private static boolean isWindows = false;
    private static boolean isLinux = false;
    private static boolean isMac = false;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        isWindows = os.contains("win");
        isLinux = os.contains("nux") || os.contains("nix");
        isMac = os.contains("mac");
        LOGGER.info("OS detected as win:[{}], nix: [{}], mac: [{}]", isWindows, isLinux, isMac);
    }

    public static boolean isWindows() {
        return isWindows;
    }

    public static boolean isLinux() {
        return isLinux;
    }

    public static boolean isMac() {
        return isMac;
    }

}