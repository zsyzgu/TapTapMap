package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;

import com.tsinghua.taptapmap.collect.file.Saver;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Collector {
    public String queryTime;
    public String responseTime;

    protected Context mContext;
    protected Saver saver;

    public Collector(Context context) {
        queryTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date());
        mContext = context;
        saver = new Saver(mContext, getSaveFolderName());
        initialize();
    }

    public abstract void initialize();

    public abstract void collect();

    public abstract void close();

    protected abstract String getSaveFolderName();

    /*
    protected void save() {
        responseTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date());
        String filePath = "/storage/emulated/0/";
        String fileName = getClass().getName() + ".json";
        String result = (new Gson()).toJson(this);
//            mTvDataCollection.setText(mTvDataCollection.getText() + "\n" + result);

        try {
            File file = new File(filePath);
            if (!file.exists())
                file.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File file = new File(filePath + fileName);
            if (!file.exists())
                file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File file = new File(filePath, fileName);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true))); // true -- append, false -- overwrite
            bufferedWriter.write(result);
            bufferedWriter.newLine();
            Log.d("taptap", "abc");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(bufferedWriter != null){
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
     */
}
