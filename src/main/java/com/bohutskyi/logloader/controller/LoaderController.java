package com.bohutskyi.logloader.controller;

import com.bohutskyi.logloader.event.OpenButtonClickEvent;
import com.bohutskyi.logloader.event.OpenButtonDisableEvent;
import com.bohutskyi.logloader.event.OpenButtonEnableEvent;
import com.bohutskyi.logloader.event.StartButtonClickEvent;
import com.bohutskyi.logloader.event.StartButtonDisableEvent;
import com.bohutskyi.logloader.event.StartButtonEnableEvent;
import com.bohutskyi.logloader.event.StatusUpdateEvent;
import com.bohutskyi.logloader.event.StopButtonClickEvent;
import com.bohutskyi.logloader.event.StopButtonDisableEvent;
import com.bohutskyi.logloader.event.StopButtonEnableEvent;
import com.bohutskyi.logloader.event.UpdatePercentEvent;
import com.bohutskyi.logloader.service.FileService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Serhii Bohutskyi
 */
@Component
public class LoaderController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TEMP_DIR_PATH = "/tmp";
    private static final String LOG_EXT = ".log";
    private static final String ZIP_EXT = ".zip";
    private static final String SIGNAL_STOP = "2";// ctrl + c
    private static final String SFTP = "sftp";
    private static final String ZIP_COMMAND = "zip -j %s %s";
    private static final String EXEC_COMMAND = "exec";
    private static final String TAIL_COMMAND = "tail -f %s > %s";

    @Autowired
    private JSch jsch;
    @Autowired
    private FileService fileService;
    @Autowired
    private ApplicationEventPublisher publisher;

    private Session session;
    private ChannelExec channel;
    private StartButtonClickEvent startEvent;

    private String localFileName;
    private String localDirPath;
    private String localZipFileName;

    private String serverResultLogFileName;
    private String serverResultLogDirPath;

    private String serverZipFileName;
    private String serverZipDirPath;

    @Async
    @EventListener
    public void onStartClicked(StartButtonClickEvent event) throws JSchException, IOException {
        logger.debug("StartButtonClickEvent start processing...");
        initializePaths(event);

        publisher.publishEvent(new StartButtonDisableEvent());
        publisher.publishEvent(new OpenButtonDisableEvent());

        publisher.publishEvent(new StatusUpdateEvent("Connecting..."));

        connect(event);

        publisher.publishEvent(new StatusUpdateEvent("Connected"));

        tail();

        publisher.publishEvent(new StopButtonEnableEvent());
        publisher.publishEvent(new StatusUpdateEvent("Tailing..."));

        logger.debug("StartButtonClickEvent processed");
    }

    private void initializePaths(StartButtonClickEvent event) {
        logger.debug("Initializing paths");
        startEvent = event;
        File remoteResultLogPathFile = new File(event.getResultLogPath());
        if (event.isUseTempResultFile()) {
            serverResultLogDirPath = TEMP_DIR_PATH;
            serverResultLogFileName = String.valueOf(System.currentTimeMillis()) + LOG_EXT;
        } else {
            serverResultLogDirPath = remoteResultLogPathFile.getParent().replace("\\", "/");
            serverResultLogFileName = remoteResultLogPathFile.getName().replace("\\", "/");
        }

        serverZipFileName = serverResultLogFileName + ZIP_EXT;
        serverZipDirPath = serverResultLogDirPath;

        localFileName = serverResultLogFileName;
        localDirPath = event.getLocalDirPath();
        localZipFileName = serverZipFileName;

        logger.debug("Local file name: [{}]", localFileName);
        logger.debug("Local directory path: [{}]", localDirPath);
        logger.debug("Local zip file name: [{}]", localZipFileName);
        logger.debug("Server result log file name: [{}]", serverResultLogFileName);
        logger.debug("Server result log directory path: [{}]", serverResultLogDirPath);
        logger.debug("Server zip file name: [{}]", serverZipFileName);
        logger.debug("Server zip directory path: [{}]", serverZipDirPath);

        logger.debug("Local log file path: [{}]", getLocalLogFile().getPath());
        logger.debug("Local zip file path: [{}]", getLocalZipFilePath());
        logger.debug("Server result log file path: [{}]", getServerResultLogFilePath());
        logger.debug("Server zip file path: [{}]", getServerZipFilePath());
        logger.debug("Server log path: [{}]", getServerLogPath());
    }

    @PreDestroy
    public void destroy() {
        if (channel != null) {
            logger.debug("Closing opened channel!");
            channel.disconnect();
        }
        if (session != null) {
            logger.debug("Closing opened session!");
            session.disconnect();
        }
    }

    @Async
    @EventListener
    public void onStopButtonClickEvent(StopButtonClickEvent event) {
        logger.debug("StopButtonClickEvent start processing...");

        publisher.publishEvent(new StopButtonDisableEvent());

        stopTail();
        logger.debug("Remote tail stopped and channel disconnected.");

        publisher.publishEvent(new StatusUpdateEvent("Stop tailing..."));

        publisher.publishEvent(new StatusUpdateEvent("Zipping..."));
        zipFile();
        publisher.publishEvent(new StatusUpdateEvent("Copying..."));
        copyZip();
        session.disconnect();
        publisher.publishEvent(new StatusUpdateEvent("Disconnected!"));

        publisher.publishEvent(new StatusUpdateEvent("Done!"));
        publisher.publishEvent(new OpenButtonEnableEvent());

        publisher.publishEvent(new StatusUpdateEvent("Unzipping..."));
        unzip();
        publisher.publishEvent(new StatusUpdateEvent("Done!"));

        publisher.publishEvent(new StopButtonDisableEvent());
        publisher.publishEvent(new StartButtonEnableEvent());
    }

    private void stopTail() {
        try {
            channel.sendSignal(SIGNAL_STOP);
        } catch (Exception e) {
            logger.debug("Channel connection error, breaking...");
        }
        channel.disconnect();
    }


    @Async
    @EventListener
    public void onOpenButtonClickEvent(OpenButtonClickEvent event) {
        fileService.open(getLocalLogFile());
    }

    private void unzip() {
        logger.debug("Unzipping...");
        try {
            ZipFile zipFile = new ZipFile(getLocalZipFilePath());

            zipFile.extractAll(getLocalDirPath());
        } catch (ZipException e) {
            throw new RuntimeException("Unzip exception!", e);
        }
        logger.debug("Unzipped!");
    }

    private void copyZip() {
        logger.debug("Start copy zip.");
        boolean mkdirs = new File(getLocalDirPath()).mkdirs();
        logger.debug("Make dirs: [{}], success: [{}]", getLocalDirPath(), mkdirs);
        try {
            Channel channel = session.openChannel(SFTP);
            channel.connect();
            logger.debug("SFTP connected!");

            logger.debug("Copying zip...");
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get(getServerZipFilePath(), getLocalZipFilePath(), new SftpProgressMonitor() {
                private long finalCount;
                private long currentCount = 0;
                private long currentPercent = 0;

                @Override
                public void init(int op, String src, String dest, long max) {
                    finalCount = max;
                }

                @Override
                public boolean count(long count) {
                    currentCount += count;
                    int perc = (int) ((float) (currentCount * 100 / finalCount));
                    if (currentPercent != perc) {
                        currentPercent = perc;
//                        logger.debug("Copying zip: {}%", currentPercent);
                        publisher.publishEvent(new UpdatePercentEvent(currentPercent));
                    }
                    return true;
                }

                @Override
                public void end() {
                }
            });

            publisher.publishEvent(new StatusUpdateEvent("Remove zip from server..."));
            sftpChannel.rm(getServerZipFilePath());
            logger.debug("Server zip file removed.");

            if (startEvent.isUseTempResultFile()) {
                publisher.publishEvent(new StatusUpdateEvent("Remove temp log from server..."));
                sftpChannel.rm(getServerResultLogFilePath());
                logger.debug("Server result log removed.");
            }

            channel.disconnect();
            logger.debug("SFTP disconnected.");

        } catch (JSchException e) {
            throw new RuntimeException("Cannot connect to server!", e);
        } catch (SftpException e) {
            throw new RuntimeException("Cannot copy file from the server!", e);
        }

    }

    private void zipFile() {
        logger.debug("Start zip server result log file.");

        try {
            channel = (ChannelExec) session.openChannel(EXEC_COMMAND);
            channel.setPty(true);
            String command = String.format(ZIP_COMMAND, getServerZipFilePath(), getServerResultLogFilePath());
            logger.debug("Remote zip command: [{}]", command);
            channel.setCommand(command);
            channel.getOutputStream().close();

            channel.connect();

            while (true) {
                if (channel.isClosed()) {
                    int exitCode = channel.getExitStatus();
                    if (exitCode != 0) {
                        throw new IOException("Command failed with code " + exitCode);
                    }
                    break;
                }
                logger.debug("Waiting then zip is done...");

                Thread.sleep(100);
            }

            channel.disconnect();
        } catch (InterruptedException e) {
            logger.error("Thread is interrupted!", e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot access to local file or directory!", e);
        } catch (JSchException e) {
            throw new RuntimeException("Ssh connection is broken!", e);
        }
    }

    private void tail() throws IOException, JSchException {
        logger.debug("Start tailing...");

        channel = (ChannelExec) session.openChannel(EXEC_COMMAND);
        channel.setPty(true);
        String command = String.format(TAIL_COMMAND, getServerLogPath(), getServerResultLogFilePath());
        logger.debug("Tail command: [{}]", command);
        channel.setCommand(command);
        channel.connect();
    }


    private void connect(StartButtonClickEvent event) throws JSchException {
        logger.debug("Start connecting via ssh.");

        session = jsch.getSession(event.getUsername(), event.getHost(), Integer.parseInt(event.getPort()));
        session.setPassword(event.getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        logger.debug("Connected via ssh.");
    }

    private File getLocalLogFile() {
        return new File(localDirPath + File.separator + localFileName);
    }

    private String getServerResultLogFilePath() {
        return serverResultLogDirPath + "/" + serverResultLogFileName;
    }

    private String getLocalDirPath() {
        return localDirPath;
    }

    private String getServerZipFilePath() {
        return serverZipDirPath + "/" + serverZipFileName;
    }

    private String getLocalZipFilePath() {
        return localDirPath + File.separator + localZipFileName;
    }

    private String getServerLogPath() {
        return startEvent.getLogPath();
    }

}

