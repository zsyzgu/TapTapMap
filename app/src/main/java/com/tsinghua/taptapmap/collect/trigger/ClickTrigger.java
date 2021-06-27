package com.tsinghua.taptapmap.collect.trigger;

import android.content.Context;

import com.tsinghua.taptapmap.collect.collector.Collector;

import java.util.List;

public class ClickTrigger extends Trigger {
    public ClickTrigger(Context context, List<CollectorType> types) {
        super(context, types);
    }

    public ClickTrigger(Context context, CollectorType type) {
        super(context, type);
    }

    @Override
    public void trigger() {
        new Thread(() -> {
            for (Collector collector: collectors) {
                collector.collect();
            }
        }).start();
    }
}
