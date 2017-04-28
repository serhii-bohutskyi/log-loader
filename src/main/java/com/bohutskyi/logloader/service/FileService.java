package com.bohutskyi.logloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;

/**
 * @author Serhii Bohutskyi
 */
@Component
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public boolean open(File file) {
        try {
            if (OSDetector.isWindows()) {
                Runtime.getRuntime().exec(new String[]
                        {"rundll32", "url.dll,FileProtocolHandler",
                                file.getAbsolutePath()});
                return true;
            } else if (OSDetector.isLinux() || OSDetector.isMac()) {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open",
                        file.getAbsolutePath()});
                return true;
            } else {
                // Unknown OS, try with desktop
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("File system not accessible!", e);
            return false;
        }
    }
}
