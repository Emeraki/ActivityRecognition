package com.uestc.zl427.newPhone2.Data;

/**
 * Created by Administrator on 2017/5/17 0017.
 */

import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by 47633 on 2017/4/11.
 */

public class AccData {

    public List<Double> time = new ArrayList<Double>();
    public List<Double> x = new ArrayList<Double>();
    public List<Double> y = new ArrayList<Double>();
    public List<Double> z = new ArrayList<Double>();
    public List<Double> g_x = new ArrayList<Double>();
    public List<Double> g_y = new ArrayList<Double>();
    public List<Double> g_z = new ArrayList<Double>();
    public List<Double> acc_squar = new ArrayList<Double>();


    public void clear_AccData(int start, int end) {
        time.subList(start, end).clear();
        x.subList(start, end).clear();
        y.subList(start, end).clear();
        z.subList(start, end).clear();
        g_x.subList(start, end).clear();
        g_y.subList(start, end).clear();
        g_z.subList(start, end).clear();
        acc_squar.subList(start, end).clear();

    }

    public void clear_AccData() {
        time.clear();
        x.clear();
        y.clear();
        z.clear();
        g_x.clear();
        g_y.clear();
        g_z.clear();
        acc_squar.clear();
    }

    public AccData copyOf(int start, int end) {
        AccData test_accData = new AccData();

        test_accData.time = new ArrayList<Double>(time.subList(start, end));
        test_accData.x = new ArrayList<Double>(x.subList(start, end));
        test_accData.y = new ArrayList<Double>(y.subList(start, end));
        test_accData.z = new ArrayList<Double>(z.subList(start, end));
        test_accData.g_x = new ArrayList<Double>(g_x.subList(start, end));
        test_accData.g_y = new ArrayList<Double>(g_y.subList(start, end));
        test_accData.g_z = new ArrayList<Double>(g_z.subList(start, end));
        test_accData.acc_squar = new ArrayList<Double>(acc_squar.subList(start, end));

        return test_accData;
    }

    public int size() {
        return time.size();
    }

    public void writeIn() throws Exception {


        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Calendar calendar = Calendar.getInstance();
        String currentTime = dateformat.format(calendar.getTime()).replace(" ", "-");
        String accPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/phoneData/acc/";
        String gravityPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/phoneData/gravity/";
        makeRootDirectory(accPath);
        makeRootDirectory(gravityPath);


        String accFilename = accPath + "accel_" + currentTime + ".log";
        String gravityFilename = gravityPath + "gravity_" + currentTime + ".log";

//        Log.d("MainActivity","目录创建成功");
        BufferedWriter mLogAcc = new BufferedWriter(new FileWriter(accFilename, true));
        BufferedWriter mLogGravity = new BufferedWriter(new FileWriter(gravityFilename, true));
//        Log.d("MainActivity","字符输出流创建成功");

        for (int i = 0; i < time.size(); i++) {
            int accCount = i + 1;
            mLogAcc.write(Integer.toString(accCount) + " " + Double.toString(time.get(i)) + " " +
                    Double.toString(x.get(i)) + " " + Double.toString(y.get(i)) + " " + Double.toString(z.get(i)) + "\r\n");
            mLogGravity.write(Integer.toString(accCount) + " " + Double.toString(time.get(i)) + " " +
                    Double.toString(g_x.get(i)) + " " + Double.toString(g_y.get(i)) + " " + Double.toString(g_z.get(i)) + "\r\n");
        }
        Log.d("MainActivity", "写入完成");
        mLogAcc.close();
        mLogGravity.close();
    }
//    //生成文件
//    public File makeFilePath(String filePath,String fileName){
//        File file = null;
//        makeRootDirectory(filePath);
//        try {
//            file = new File(filePath+fileName);
//            if (!file.exists()){
//                file.createNewFile();
//                Log.e("cacacaca","新建文件成功,filePath：" + filePath +"fileName:"+fileName);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return file;
//    }

    //生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {//判断指定的路径或者指定的目录文件是否已经存在。
                file.mkdirs();//建立文件夹
                Log.e("cacacaca", "新建文件夹成功,路径为：" + filePath);
            }
        } catch (Exception e) {
            Log.e("error:", e + "");
        }
    }


}
