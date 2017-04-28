package com.bohutskyi.logloader.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author Serhii Bohutskyi
 */
public class StatusUpdateEvent {
    private String text;

    public StatusUpdateEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
