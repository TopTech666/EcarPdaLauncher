package ecar.com.pdalauncher.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ecar.com.pdalauncher.R;

/*************************************
 功能：
 创建者： kim_tony
 创建日期：2017/6/11
 版权所有：深圳市亿车科技有限公司
 *************************************/

public class CheckUtil {
    public static final String LAUNCHER_NAME = "com.ecar.launcher";  //亿车桌面包名

    public static boolean checkLauncher(Activity context) {
        if (!isInstalled(context)) {
            showDialog(context);
            return false;
        } else {
            if (TextUtils.isEmpty(getLauncherPackageName(context))) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
                intent.addCategory(Intent.CATEGORY_HOME);
                context.startActivity(intent);
            } else if (!LAUNCHER_NAME.equals(getLauncherPackageName(context))) {
                Toast.makeText(context, "请点击设置为默认按钮，并清除默认值", Toast.LENGTH_SHORT).show();
                getAppDetailSettingIntent(context);
                return false;
            }
        }
        return true;
    }

    //显示是否安装的提示
    private static void showDialog(final Activity activity) {
        AlertDialog.Builder builer = new AlertDialog.Builder(activity);
        builer.setCancelable(false);
        builer.setMessage("当前未安装亿车桌面，请点击确认安装");
        builer.setTitle("温馨提示");
        builer.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toSetUpLauncher(activity);
            }
        });
        builer.show();
    }

    //安装桌面APP
    private static void toSetUpLauncher(final Activity context) {
        if (copyApkFromAssets(context, "ecar_launcher.apk", getSdPatch(context) + "/ecar_launcher.apk")) {
            AlertDialog.Builder m = new AlertDialog.Builder(context)
                    .setMessage("是否安装？")
                    .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/ecar_launcher.apk"),
                                    "application/vnd.android.package-archive");
                            context.startActivity(intent);
                        }
                    });
            m.show();
        }
    }

    private static boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }

    //获取当前桌面包名
    public static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            return "";
        }
        //如果是不同桌面主题，可能会出现某些问题，这部分暂未处理
        if (res.activityInfo.packageName.equals("android")) {
            return "";
        } else {
            return res.activityInfo.packageName;
        }
    }

    //是否已安装亿车桌面
    public static boolean isInstalled(Context context) {

        return isPkgInstalled(context, LAUNCHER_NAME);

    }

    //是否安装了某应用   pkgName  包名
    public static boolean isPkgInstalled(Context context, String pkgName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }

    }

    public static String getSdPatch(Activity activity) {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        } else {
            String patch = null;
            return TextUtils.isEmpty(patch = getCanUsePatch(activity)) ? activity.getCacheDir().getAbsolutePath() : patch;
        }
    }

    private static String getCanUsePatch(Activity activity) {
        StorageManager mStorageManager = (StorageManager) activity.getSystemService("storage");
        Method method = null;

        try {
            method = mStorageManager.getClass().getMethod("getVolumePaths", new Class[0]);
        } catch (NoSuchMethodException var8) {
            var8.printStackTrace();
        }

        String[] paths = null;

        try {
            paths = (String[]) ((String[]) method.invoke(mStorageManager, new Object[0]));
        } catch (IllegalArgumentException var5) {
            var5.printStackTrace();
        } catch (IllegalAccessException var6) {
            var6.printStackTrace();
        } catch (InvocationTargetException var7) {
            var7.printStackTrace();
        }

        if (paths != null && paths.length != 0) {
            for (int i = 0; i < paths.length; ++i) {
                if ((new File(paths[i])).canRead()) {
                    return paths[i];
                }
            }

            return "";
        } else {
            return "";
        }
    }

    /**
     * 跳转到权限设置界面
     */
    private static void getAppDetailSettingIntent(Activity context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getLauncherPackageName(context), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", getLauncherPackageName(context));
        }
        context.startActivity(intent);

    }
}
