package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;

import com.tsinghua.taptapmap.collect.file.Saver;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Collector {
    protected Context mContext;
    protected Saver saver;

    public Collector(Context context, String triggerFolder) {
        mContext = context;
        saver = new Saver(mContext, triggerFolder, getSaveFolderName());
        initialize();
    }

    public abstract void initialize();

    public abstract void collect();

    public abstract void close();

    protected abstract String getSaveFolderName();
}
