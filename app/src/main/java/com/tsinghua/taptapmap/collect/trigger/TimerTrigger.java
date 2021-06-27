package com.tsinghua.taptapmap.collect.trigger;

import android.content.Context;

import com.tsinghua.taptapmap.collect.collector.Collector;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTrigger extends Trigger {
    public TimerTrigger(Context context, List<CollectorType> types) {
        super(context, types);
    }

    @Override
    public void trigger() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Collector collector: collectors) {
                    collector.collect();
                }
            }
        }, 5000, 600000);
    }
}
