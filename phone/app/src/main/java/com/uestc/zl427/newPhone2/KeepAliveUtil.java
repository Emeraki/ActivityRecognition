package com.uestc.zl427.newPhone2;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * 保活工具类
 */
public class KeepAliveUtil {
    /**
     * 判断我们的应用是否在白名单中
     *
     * @param activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isIgnoringBatteryOptimizations(Activity activity) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        }
        return isIgnoring;
    }

    /**
     * 申请加入白名单
     *
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestIgnoreBatteryOptimizations(Activity activity,int requestCode) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到电池策略文字
     */
    public static String getBatteryStrategyStr(Context context) {
        if (isHuawei()) {
            return context.getString(R.string.str_battery_strategy_huawei);
        } else if (isXiaomi()) {
            return context.getString(R.string.str_battery_strategy_miui);
        } else if (isOPPO()) {
            return context.getString(R.string.str_battery_strategy_oppo);
        } else if (isVIVO()) {
            return context.getString(R.string.str_battery_strategy_vivo);
        } else {
            return context.getString(R.string.str_battery_strategy_miui);
        }
    }

    /**
     * 得到电池策略提示文字
     */
    public static String getBatteryStrategyTipStr(Context context) {
        if (isHuawei()) {
            return context.getString(R.string.str_battery_strategy_tip_huawei);
        } else if (isXiaomi()) {
            return context.getString(R.string.str_battery_strategy_tip_miui);
        } else if (isOPPO()) {
            return context.getString(R.string.str_battery_strategy_tip_oppo);
        } else if (isVIVO()) {
            return context.getString(R.string.str_battery_strategy_tip_vivo);
        } else {
            return context.getString(R.string.str_battery_strategy_tip_miui);
        }

    }

    /**
     * 跳转不同机型的电池策略界面
     */
    public static void startBatteryStrategyActivity(Context context) {
        Intent intent = new Intent();
        if (isHuawei()) {
            try {
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$HighPowerApplicationsActivity"));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.str_battery_strategy_error), Toast.LENGTH_SHORT).show();
            }
        } else if (isXiaomi()) {
            intent.putExtra("package_name", context.getPackageName());
            intent.putExtra("package_label", context.getResources().getString(R.string.app_name));
            try {
                intent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.str_battery_strategy_error), Toast.LENGTH_SHORT).show();

            }
        } else if (isOPPO()) {
            try {
                intent.setComponent(new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.str_battery_strategy_error), Toast.LENGTH_SHORT).show();
            }
        } else if (isVIVO()) {
            try {
                intent.setComponent(new ComponentName("com.iqoo.powersaving", "com.iqoo.powersaving.PowerSavingManagerActivity"));
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.str_battery_strategy_error), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.str_battery_strategy_error2), Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * 跳转不同机型的自启动管理界面
     */
    public static void startAppStartManagerActivity(Context context) {
        if (isHuawei()){
            goHuaweiSetting(context);
        }else if (isXiaomi()){
            goXiaomiSetting(context);
        }else if (isOPPO()){
            goOPPOSetting(context);
        }else if (isVIVO()){
            goVIVOSetting(context);

        }else {
            Toast.makeText(context, context.getResources().getString(R.string.str_battery_strategy_error2), Toast.LENGTH_SHORT).show();
        }
    }

    private static void goHuaweiSetting(Context context) {
        try {
            showActivity(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        } catch (Exception e) {
            showActivity(context, "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
        }
    }

    private static void goXiaomiSetting(Context context) {
        showActivity(context, "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity");
    }
    private static void goOPPOSetting(Context context) {
        try {
            showActivity(context, "com.coloros.phonemanager");
        } catch (Exception e1) {
            try {
                showActivity(context, "com.oppo.safe");
            } catch (Exception e2) {
                try {
                    showActivity(context, "com.coloros.oppoguardelf");
                } catch (Exception e3) {
                    showActivity(context, "com.coloros.safecenter");
                }
            }
        }

    }
    private static void goVIVOSetting(Context context) {
        showActivity(context, "com.iqoo.secure");

    }

    /**
     * 跳转到指定应用的指定页面
     */
    private static void showActivity(Context context, @NonNull String packageName, @NonNull String activityDir) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityDir));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
     * 跳转到指定应用的首页
     */
    private static void showActivity(Context context, @NonNull String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
    }

    /**
     * 华为厂商判断
     *
     * @return
     */
    public static boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.toLowerCase().equals("huawei") || Build.BRAND.toLowerCase().equals("honor");
        }
    }

    /**
     * 小米厂商判断
     * minSdkVersion在19以上，红米系列得到的是redmi的厂商名字，所以在这里适配下红米厂商
     *
     * @return
     */
    public static boolean isXiaomi() {
        return Build.BRAND != null && (Build.BRAND.toLowerCase().equals("xiaomi") || Build.BRAND.toLowerCase().equals("redmi"));
    }

    /**
     * OPPO厂商判断
     *
     * @return
     */
    public static boolean isOPPO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("oppo");
    }

    /**
     * VIVO厂商判断
     *
     * @return
     */
    public static boolean isVIVO() {
        return Build.BRAND != null && Build.BRAND.toLowerCase().equals("vivo");
    }
}
