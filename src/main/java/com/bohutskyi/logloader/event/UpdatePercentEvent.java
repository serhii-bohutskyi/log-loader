package com.bohutskyi.logloader.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author Serhii Bohutskyi
 */
public class UpdatePercentEvent {
    private long currentPercent;

    public UpdatePercentEvent(long currentPercent) {
        this.currentPercent = currentPercent;
    }

    public long getCurrentPercent() {
        return currentPercent;
    }
}
