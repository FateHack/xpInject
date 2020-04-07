package com.fate.xpInject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    private ListView appList;
    private List<AppInfo> appInfos = new ArrayList<AppInfo>();
    private List<String> packageNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upgradeRootPermission(getPackageCodePath());
        requestPower();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (isModuleActive()) {
                actionBar.setTitle("List<APP>(Activated)");
            } else {
                actionBar.setTitle("List<APP>(Not Activated)");
                Toast.makeText(this, "Current Module is not activated!", Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(this, "Please choose app to inject so!", Toast.LENGTH_SHORT).show();
        bindViews();
        getPackages();
        AppAdapter appAdapter = new AppAdapter(this, R.layout.applist, appInfos);
        appList.setAdapter(appAdapter);
        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SoListActivity.class);
                if (!appInfos.get(position).getApplicationInfo().nativeLibraryDir.isEmpty()) {
                    intent.putExtra("nativeLibraryDir", appInfos.get(position).getApplicationInfo().nativeLibraryDir);
                    intent.putExtra("packageName", appInfos.get(position).getApplicationInfo().packageName);
                    intent.putExtra("appName", appInfos.get(position).getAppName());
                    try {
                        FileUtils.writeStr("/sdcard/currentApp", appInfos.get(position).getApplicationInfo().packageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(), "该APP下没有lib目录", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void bindViews() {
        appList = (ListView) findViewById(R.id.appList);

    }

    private static boolean isModuleActive() {

        return false;

    }


    private void getPackages() {
        // 获取已经安装的所有应用, PackageInfo　系统类，包含应用信息
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用
                // AppInfo 自定义类，包含应用信息
                AppInfo appInfo = new AppInfo();
                appInfo.setAppName(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());//获取应用名称
                appInfo.setPackageName(packageInfo.packageName); //获取应用包名，可用于卸载和启动应用
                appInfo.setVersionName(packageInfo.versionName);//获取应用版本名
                appInfo.setVersionCode(packageInfo.versionCode);//获取应用版本号
                appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(getPackageManager()));//获取应用图标
                appInfo.setApplicationInfo(packageInfo.applicationInfo);
                appInfos.add(appInfo);
                packageNames.add(appInfo.getAppName());
            } else { // 系统应用

            }
        }

    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void requestPower() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "需要读写权限，请打开设置开启对应的权限", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    /**
     * onRequestPermissionsResult方法重写，Toast显示用户是否授权
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String requestPermissionsResult = "";
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    requestPermissionsResult += permissions[i] + " 申请成功\n";
                } else {
                    requestPermissionsResult += permissions[i] + " 申请失败\n";
                }
            }
        }
        Toast.makeText(this, requestPermissionsResult, Toast.LENGTH_SHORT).show();
    }

    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }
}
