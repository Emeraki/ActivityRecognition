package com.uestc.zl427.newPhone2.Data;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GrpoData {
    public List<Double> grpo_time = new ArrayList<Double>();
    public List<Double> grpo_x = new ArrayList<Double>();
    public List<Double> grpo_y = new ArrayList<Double>();
    public List<Double> grpo_z= new ArrayList<Double>();

    public void clear_GrpoData(int start, int end){
        grpo_time.subList(start,end).clear();
        grpo_x.subList(start,end).clear();
        grpo_y.subList(start,end).clear();
        grpo_z.subList(start,end).clear();
    }
    public void clear_AccData(){
        grpo_time.clear();
        grpo_x.clear();
        grpo_y.clear();
        grpo_z.clear();
    }

    public GrpoData copyOf(int start, int end){
        GrpoData test_grpoData = new GrpoData();

        test_grpoData.grpo_time = new ArrayList<Double>(grpo_time.subList(start, end));
        test_grpoData.grpo_x = new ArrayList<Double>(grpo_x.subList(start, end));
        test_grpoData.grpo_y = new ArrayList<Double>(grpo_y.subList(start, end));
        test_grpoData.grpo_z = new ArrayList<Double>(grpo_z.subList(start, end));
        return test_grpoData;
    }

    public int size(){
        return grpo_time.size();
    }

    public void writeIn() throws  Exception{
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Calendar calendar = Calendar.getInstance();
        String currentTime = dateformat.format(calendar.getTime()).replace(" ", "-");
//        String base = Context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
//        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String gyroPath = Environment.getExternalStorageDirectory().getAbsolutePath()+
                "/phoneData/gyro/";
        makeRootDirectory(gyroPath);

        String grpoFilename = gyroPath + "gyroscope_" + currentTime +".log";
//        FileOutputStream fout = new FileOutputStream(grpoFilename, Context.MODE_APPEND);
        BufferedWriter mLogGrpo = new BufferedWriter(new FileWriter(grpoFilename, true));

        for(int i = 0; i < grpo_time.size(); i++){
            int accCount = i + 1;
            mLogGrpo.write(Integer.toString(accCount) +" " + Double.toString(grpo_time.get(i)) + " " +
                    Double.toString(grpo_x.get(i)) + " " + Double.toString(grpo_y.get(i)) + " " + Double.toString(grpo_z.get(i)) + "\r\n");
        }
        mLogGrpo.close();

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
    public static void makeRootDirectory(String filePath){
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()){//判断指定的路径或者指定的目录文件是否已经存在。
                file.mkdirs();//建立文件夹
                Log.e("cacacaca","新建文件夹成功,路径为：" + filePath);
            }
        }catch (Exception e){
            Log.e("error:", e+"");
        }
    }
}
