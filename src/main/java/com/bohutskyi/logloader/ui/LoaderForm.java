/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bohutskyi.logloader.ui;

import com.bohutskyi.logloader.event.StartButtonDisableEvent;
import com.bohutskyi.logloader.event.StopButtonDisableEvent;
import com.bohutskyi.logloader.event.StartButtonEnableEvent;
import com.bohutskyi.logloader.event.StopButtonEnableEvent;
import com.bohutskyi.logloader.event.OpenButtonClickEvent;
import com.bohutskyi.logloader.event.OpenButtonDisableEvent;
import com.bohutskyi.logloader.event.OpenButtonEnableEvent;
import com.bohutskyi.logloader.event.StartButtonClickEvent;
import com.bohutskyi.logloader.event.StatusUpdateEvent;
import com.bohutskyi.logloader.event.StopButtonClickEvent;
import com.bohutskyi.logloader.event.UpdatePercentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Serhii_Bohutskyi
 */
public class LoaderForm extends LoaderFrame {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${host:#{null}}")
    private String hostParam;
    @Value("${port:22}")
    private String portParam;
    @Value("${username:#{null}}")
    private String usernameParam;
    @Value("${password:#{null}}")
    private String passwordParam;
    @Value("${serverLogPath:#{null}}")
    private String serverLogPathParam;
    @Value("${serverResultLogPath:#{null}}")
    private String serverResultLogPathParam;
    @Value("${localDirPath:#{null}}")
    private String localDirPathParam;

    /**
     * Creates new form LoaderForm
     */
    public LoaderForm() {
        super();

        stopButton.setEnabled(false);
        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum(100);
        status.setText(" ");
        handle4Kmonitor();
    }

    @PostConstruct
    private void init() {
        host.setText(hostParam);
        port.setText(portParam);
        username.setText(usernameParam);
        password.setText(passwordParam);
        logPath.setText(serverLogPathParam);
        localDirPath.setText(localDirPathParam);
        resultLogPath.setText(serverResultLogPathParam);
        if (serverResultLogPathParam != null) {
            useTempLogFile.setSelected(false);
            resultLogPath.setEnabled(true);
        } else {
            useTempLogFile.setSelected(true);
            resultLogPath.setEnabled(false);
        }

        pack();
        setVisible(true);
    }

    private void handle4Kmonitor() {
        if (is4K()) {
            updateFont(new Font("Arial", Font.PLAIN, 28));
        }
    }

    private boolean is4K() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        return width > 3000 && height > 2000;
    }

    private void updateFont(Font font) {
        for (Component component : getAllComponents(jPanel1)) {
            component.setFont(font);
        }
    }

    private List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container)
                compList.addAll(getAllComponents((Container) comp));
        }
        return compList;
    }

    @Override
    protected void useTempLogFileActionPerformed(ActionEvent evt) {
        if (useTempLogFile.isSelected()) {
            resultLogPath.setEnabled(false);
        } else {
            resultLogPath.setEnabled(true);
        }
    }

    protected void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        jProgressBar1.setValue(0);
        publisher.publishEvent(createStartButtonClickEvent());
    }//GEN-LAST:event_startButtonActionPerformed

    @Override
    protected void stopButtonActionPerformed(ActionEvent evt) {
        publisher.publishEvent(new StopButtonClickEvent());
    }

    private StartButtonClickEvent createStartButtonClickEvent() {
        StartButtonClickEvent event = new StartButtonClickEvent();
        event.setHost(host.getText());
        event.setPort(port.getText());
        event.setUsername(username.getText());
        event.setPassword(new String(password.getPassword()));
        event.setLogPath(logPath.getText());
        event.setResultLogPath(resultLogPath.getText());
        event.setUseTempResultFile(useTempLogFile.isSelected());
        event.setLocalDirPath(localDirPath.getText());
        return event;
    }

    @EventListener
    public void onOpenButtonEnableEvent(OpenButtonEnableEvent event) {
        openButton.setEnabled(true);
    }

    @EventListener
    public void onOpenButtonDisableEvent(OpenButtonDisableEvent event) {
        openButton.setEnabled(false);
    }

    @Override
    protected void openButtonActionPerformed(ActionEvent evt) {
        publisher.publishEvent(new OpenButtonClickEvent());
    }

    @EventListener
    public void onDisableStopButtonEvent(StopButtonDisableEvent event) {
        stopButton.setEnabled(false);
    }

    @EventListener
    public void onEnableStartButtonEvent(StartButtonEnableEvent event) {
        startButton.setEnabled(true);
    }

    @EventListener
    public void onUpdatePercentEvent(UpdatePercentEvent event) {
        System.out.println(event.getCurrentPercent() + "%");
        jProgressBar1.setValue((int) event.getCurrentPercent());
    }

    @EventListener
    public void onStatusUpdateEvent(StatusUpdateEvent event) {
        status.setText(event.getText());
    }

    @EventListener
    public void onDisableStartButtonEvent(StartButtonDisableEvent event) {
        startButton.setEnabled(false);
    }

    @EventListener
    public void onEnableStopButtonEvent(StopButtonEnableEvent event) {
        stopButton.setEnabled(true);
    }


}
