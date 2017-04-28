package com.bohutskyi.logloader.event;

/**
 * @author Serhii Bohutskyi
 */
public class StartButtonClickEvent {

    private String host;
    private String port;
    private String username;
    private String password;
    private String logPath;
    private String resultLogPath;
    private boolean useTempResultFile;
    private String localDirPath;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getResultLogPath() {
        return resultLogPath;
    }

    public void setResultLogPath(String resultLogPath) {
        this.resultLogPath = resultLogPath;
    }

    public boolean isUseTempResultFile() {
        return useTempResultFile;
    }

    public void setUseTempResultFile(boolean useTempResultFile) {
        this.useTempResultFile = useTempResultFile;
    }

    public String getLocalDirPath() {
        return localDirPath;
    }

    public void setLocalDirPath(String localDirPath) {
        this.localDirPath = localDirPath;
    }
}
