package com.uestc.zl427.newPhone2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.uestc.zl427.newPhone2.Data.AccData;
import com.uestc.zl427.newPhone2.Data.GrpoData;
import com.uestc.zl427.newPhone2.Data.Pretreat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class MyService extends Service implements SensorEventListener {
    private static final String TAG = "Sensor Data Collection";

    private SensorManager mgr;
    private Sensor accel;//加速度计
    private Sensor gyro;//陀螺仪
    private String id;

    AccData accData = new AccData();
    //当前加速度的一个副本
    AccData send_accData;
    GrpoData grpoData = new GrpoData();
    //当前陀螺仪数据副本
    GrpoData send_grpo;
    Pretreat pretreat;

    private double[] accelValues = new double[3];
    private double[] grpoValues = new double[3];
    private double accTime = 0;
    private double gypoTime = 0;
    PowerManager.WakeLock m_wklk;


    private long milliseconds = 0;
    private double[] gravity = new double[3];//加速度中抽离出重力的部分
    private double[] motion = new double[3];//加速度中抽离出动作的部分


    //创建线程池
    ExecutorService es = Executors.newFixedThreadPool(10);

    @Override
    public void onCreate() {
        super.onCreate();
        //申请锁屏后cpu不休眠
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        m_wklk = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MyService.class.getName());
        m_wklk.acquire();
        //获取设备id
        try {
            id=findId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //让Service位于前台运行
        startForeground();
        //获取传感器并对其监听
        mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        accel = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = mgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
        mgr.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);


    }
    /**
     * 启动前台服务
     */
    private void startForeground() {
        String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("kim.hsl", "ForegroundService");
        } else {
            channelId = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

    /**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    //服务被关闭时调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("wang", "onDestroy 服务关闭时");
        mgr.unregisterListener(this, gyro);
        mgr.unregisterListener(this, accel);
        if (m_wklk != null) {
            m_wklk.release();
            m_wklk = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        milliseconds = Calendar.getInstance().getTimeInMillis();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                accTime = milliseconds;
                for (int i = 0; i < 3; i++) {
                    accelValues[i] = event.values[i];//for accel
                }

                for (int i = 0; i < 3; i++) {
                    gravity[i] = 0.1 * event.values[i] + 0.9 * gravity[i]; //for gravity Data
                    motion[i] = event.values[i] - gravity[i];// for motion accel data
                    //无重力数据时实为0.9倍加速度
                }
                double acc_squar = Math.sqrt(Math.pow(motion[0], 2) + Math.pow(motion[1], 2) + Math.pow(motion[2], 2));

                accData.time.add(accTime);
                accData.g_x.add(gravity[0]);
                accData.g_y.add(gravity[1]);
                accData.g_z.add(gravity[2]);
                accData.x.add(motion[0]);
                accData.y.add(motion[1]);
                accData.z.add(motion[2]);
                accData.acc_squar.add(acc_squar);
                //当数据片段大于400时开始处理
                if (accData.size() >=400 && grpoData.size() > 400) {
                    AccData test_accData=accData.copyOf(0,400);
                    GrpoData test_grpoData = grpoData.copyOf(0, 400);
                    Log.d(TAG, "onSensorChanged: 赋值到test文件");
                    //在线程池里面取一个线程来寻找有效动作片段并发送
                    es.execute(new Runnable() {
                                   @Override
                                   public void run() {
                                       findFragmentTask(test_accData,
                                               test_grpoData);
                                   }
                               }
                    );
                    accData.clear_AccData(0,50);
                    grpoData.clear_GrpoData(0,50);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:

                gypoTime = milliseconds;
                for (int i = 0; i < 3; i++) {
                    grpoValues[i] = event.values[i];
                }
//                Log.d(TAG, "onSensorChanged: 陀螺仪调用");
                grpoData.grpo_time.add(gypoTime);
                grpoData.grpo_x.add(grpoValues[0]);
                grpoData.grpo_y.add(grpoValues[1]);
                grpoData.grpo_z.add(grpoValues[2]);
                break;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("MainActivity", "onAccuracyChanged()被调用");
    }

    private void findFragmentTask(AccData acc,GrpoData grpo){
        //预处理，检查动作
        pretreat = new Pretreat();
        pretreat.pret(acc);
        //获取动作片段开始和结束位置
        int segment_start = pretreat.getStart();
        int segment_end = pretreat.getEnd();
        //找到合适片段后对其进行处理
        if (segment_end != 0 && segment_end - segment_start >= 60) {
            int grpo_start=0;
            int grpo_end = 0;

            if (segment_end - segment_start < 400) {
                send_accData = acc.copyOf(segment_start, segment_end);
                //找到陀螺仪数据与加速度片段对应的开始和结束位置
                if (acc.time.get(segment_start)<grpo.grpo_time.get(0)){
                    grpo_start=0;
                }else{
                    for (int i = 0; i < 399; i++) {
                        if (acc.time.get(segment_start)<=grpo.grpo_time.get(i+1)&&acc.time.get(segment_start)>=grpo.grpo_time.get(i)){
                            grpo_start=i;
                            break;
                        }
                    }
                    for (int i = 0; i < 399; i++) {
                        if (acc.time.get(segment_end-1)<=grpo.grpo_time.get(i+1)&&acc.time.get(segment_end-1)>=grpo.grpo_time.get(i)){
                            grpo_end=i;
                            break;
                        }
                    }
                    if(grpo_end==0){
                        grpo_end=400;
                    }
                }
                send_grpo=grpo.copyOf(grpo_start,grpo_end);
//                对要发送的加速度和陀螺仪片段进行填0处理
                for (int i = send_accData.size(); i < 400; i++) {
                    send_accData.x.add(0.0);
                    send_accData.y.add(0.0);
                    send_accData.z.add(0.0);
                }
                for (int i = send_grpo.size(); i < 400; i++) {
                    send_grpo.grpo_x.add(0.0);
                    send_grpo.grpo_y.add(0.0);
                    send_grpo.grpo_z.add(0.0);
                }
            } else {
                send_accData = acc.copyOf(segment_start, segment_start + 400);
            }
//          将数据发送出去
            sendRequestWithOkHttp(send_accData,send_grpo);
        }
    }

    private void sendRequestWithOkHttp(AccData send_acc,GrpoData send_grpo) {
        //将数据转化为JSON字符串
        String json = JSON.toJSONString(send_acc);
        String json2=JSON.toJSONString(send_grpo);
        //合并加速度和陀螺仪数据
        String sendJson="{\"deviceId\":\""+id+"\","+json.substring(1,json.length()-1)+","+json2.substring(1,json2.length()-1)+"}";
        MediaType type = MediaType.parse("application/json;charset=utf-8");
        RequestBody RequestBody2 = RequestBody.create(type,sendJson);
        //发送数据
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    // 指定访问的服务器地址
                    .addHeader("Connection","close")
                    .url("http://3714w993m5.qicp.vip/sendData")
                    .post(RequestBody2)
                    .build();
            Response response = client.newCall(request).execute();
            System.out.println(response);
            Headers headers = response.headers();
            System.out.println(headers);
            String responseData = response.body().string();
            System.out.println(responseData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONObject object = JSONObject.parseObject(jsonData);
            String name = object.getString("the activity data is");
            //日志
            Log.d("name", "结果是：" + name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String findId() throws IOException {
        //获取id文件存储路径
        String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String idFilename = basePath + "/deviceId.txt";
        File f=new File(idFilename);
        //如果文件存在则读取id，否则创建id文件
        if (!f.exists()){
            BufferedWriter wId = new BufferedWriter(new FileWriter(idFilename, true));
            UUID uuid = UUID.randomUUID();
            wId.write(String.valueOf(uuid));
            wId.close();
            return String.valueOf(uuid);
        }else {
            FileReader fileReader = new FileReader(idFilename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String id=bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();
            return id;

        }

    }

}
