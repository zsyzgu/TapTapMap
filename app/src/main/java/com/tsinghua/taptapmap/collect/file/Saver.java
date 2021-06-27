package com.tsinghua.taptapmap.collect.file;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Saver {
    private String saveFolder;
    private Executor pool;

    public Saver(Context context, String saveFolderName) {
        this.saveFolder = context.getExternalMediaDirs()[0].getAbsolutePath() + "/" + saveFolderName;
        this.pool = new ThreadPoolExecutor(1, 1,
                60, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    private void saveString(String data) {
        String fullPath = this.saveFolder + "/" + String.valueOf(System.currentTimeMillis()) + ".txt";
        try {
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(data);
            writer.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed:" + e.toString());
        }
    }

    public void save(Object object) {
        pool.execute(() -> saveString(JSON.toJSONString(object)));
    }

}
