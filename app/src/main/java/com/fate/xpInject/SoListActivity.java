package com.fate.xpInject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoListActivity extends AppCompatActivity {

    private List<String> soNames = new ArrayList<>();
    private ListView soList;
    private static final String INJECT = "InjectedApp";
    SharedPreferences selectedApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("List<SO>");
        }
        selectedApp = GlobalApplication.getInstance().getSharedPreferences(INJECT, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = selectedApp.edit();
        selectedApp.getString(INJECT, null);
        setContentView(R.layout.activity_so_list);
        soList = (ListView) findViewById(R.id.soList);
        getAllSo();
        SoAdapter soAdapter = new SoAdapter(this, R.layout.so_checked_item, soNames);
        soList.setAdapter(soAdapter);
//        if (selectedApp.getString("App", null) != null) {
//            if (!selectedApp.getString("App", null).equals(getIntent().getStringExtra("packageName"))) {
//                soList.setBackgroundColor(Color.WHITE);
//            }
//        }

        soList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                for (int i = 0; i < parent.getCount(); i++) {
//                    View v = parent.getChildAt(i);
//                    if (v != null) {
//                        if (position == i) {
//                            v.setBackgroundColor(Color.argb(60, 0, 0, 255));
//                        } else {
//                            v.setBackgroundColor(Color.argb(224, 237, 232, 232));
//                        }
//                    }
//                }
                String selectedSo = soNames.get(position);
                Toast.makeText(getBaseContext(), "Intercepted " + selectedSo + " success! ", Toast.LENGTH_SHORT).show();
                editor.putString("App", getIntent().getStringExtra("packageName"));
                editor.putString("soName", selectedSo);
                editor.commit();
                try {
                    FileUtils.writeStr("/sdcard/App", getIntent().getStringExtra("packageName"));
                    FileUtils.writeStr("/sdcard/soName", selectedSo);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                String path = getApplicationInfo().nativeLibraryDir + "/libfate.so";
                String path2 = getIntent().getStringExtra("nativeLibraryDir") + "/libunity.so";
                moveFileToSystem(getIntent().getStringExtra("nativeLibraryDir"), path, getIntent().getStringExtra("nativeLibraryDir"));
                exusecmd("chmod 777 " + getIntent().getStringExtra("nativeLibraryDir") + "/libfate.so");
                File sdFile = new File("/sdcard/path");
                if (!sdFile.exists()) {
                    try {
                        sdFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    FileOutputStream fos = new FileOutputStream(sdFile);
                    fos.write(path2.getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getBaseContext(), "You will enter  " + getIntent().getStringExtra("appName"), Toast.LENGTH_SHORT).show();
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                killPackage(getIntent().getStringExtra("packageName"));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PackageManager packageManager = getPackageManager();
                        Intent intent = new Intent();
                        intent = packageManager.getLaunchIntentForPackage(getIntent().getStringExtra("packageName"));
                        if (intent == null) {
                        } else {
                            startActivity(intent);
                        }
                    }
                }, 2000);
            }
        });

    }

    private ActivityManager activityManager;

    private void killPackage(String pkg) {
        try {
            activityManager.killBackgroundProcesses(pkg);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
    }


    private void getAllSo() {
        String path = getIntent().getStringExtra("nativeLibraryDir");
        File files = new File(path);
        File[] listFiles = files.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(".so") && !file.getName().contains("fate")) {
                soNames.add(file.getName());
            }
        }
    }


    public static boolean exusecmd(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
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
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //移动文件
    public static void moveFileToSystem(String appPath, String filePath, String targetPath) {
        exusecmd("mount -o rw,remount " + appPath);
        exusecmd("chmod 777 " + appPath);
        exusecmd("cp  " + filePath + " " + targetPath);
    }

}
