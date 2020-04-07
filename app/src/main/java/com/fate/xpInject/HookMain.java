package com.fate.xpInject;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by jy on 2018/12/17.
 */

public class HookMain implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "Fuck";
    boolean detectedSdPath;
    public XC_MethodHook externalSdCardAccessHook;
    public XC_MethodHook externalSdCardAccessHook2;
    public XC_MethodHook getExternalFilesDirHook;
    public XC_MethodHook getExternalFilesDirsHook;
    public XC_MethodHook getExternalStorageDirectoryHook;
    public XC_MethodHook getExternalStoragePublicDirectoryHook;
    public XC_MethodHook getObbDirHook;
    public XC_MethodHook getObbDirsHook;
    public String internalSd;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        initHook();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.fate.xpInject")) {
            findAndHookMethod("com.fate.xpInject.MainActivity", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }
        if ((lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用
            final String packageName = FileUtils.getAppName();
            String soName = FileUtils.getSoName();
            if (packageName == null || soName == null) { //无sd卡权限
                hookSdPermissionIfNeed(lpparam, lpparam.packageName);
            }
            if (packageName != null && soName != null) {
                int i = soName.indexOf(".");
                final String finalSoName = soName.substring(3, i);
                if (lpparam.packageName.equals(packageName)) {
                    Log.i(TAG, "Ok!");
                    findAndHookMethod("dalvik.system.BaseDexClassLoader", ClassLoader.getSystemClassLoader(), "findLibrary", String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.args[0].toString().contains(finalSoName)) {
                                Log.i(TAG, "inject");
                                String libPath = lpparam.appInfo.nativeLibraryDir + "/libfate.so";
                                XposedHelpers.callMethod(Runtime.getRuntime(), "doLoad", libPath, param.thisObject.getClass().getClassLoader());
                            }
                        }
                    });
                }
            }
        }
    }

    private void hookSdPermissionIfNeed(XC_LoadPackage.LoadPackageParam lpparam, String targetPackageName) {
        Object[] v3;
        Class v1;
        int v9 = 19;
        int v8 = 3;
        int v7 = 2;
        if (("android".equals(lpparam.packageName)) && ("android".equals(lpparam.processName))) {
            if (Build.VERSION.SDK_INT >= 23) {
                v1 = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
                v3 = new Object[4];
                v3[0] = "android.content.pm.PackageParser.Package";
                v3[1] = Boolean.TYPE;
                v3[v7] = String.class;
                v3[v8] = this.externalSdCardAccessHook2;
                XposedHelpers.findAndHookMethod(v1, "grantPermissionsLPw", v3);
            } else {
                if (Build.VERSION.SDK_INT != 21 && Build.VERSION.SDK_INT != 22) {
                    if (Build.VERSION.SDK_INT == v9) {
                        v1 = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
                        v3 = new Object[v8];
                        v3[0] = XmlPullParser.class;
                        v3[1] = String.class;
                        v3[v7] = this.externalSdCardAccessHook;
                        XposedHelpers.findAndHookMethod(v1, "readPermission", v3);
                    }

                } else {
                    v1 = XposedHelpers.findClass("com.android.server.SystemConfig", lpparam.classLoader);
                    v3 = new Object[v8];
                    v3[0] = XmlPullParser.class;
                    v3[1] = String.class;
                    v3[v7] = this.externalSdCardAccessHook;
                    XposedHelpers.findAndHookMethod(v1, "readPermission", v3);
                }
            }
        }

        if (!this.detectedSdPath) {
            try {
                this.internalSd = Environment.getExternalStorageDirectory().getPath();
                this.detectedSdPath = true;
            } catch (Exception v1_1) {
            }
        }

        // 判断是否目标app
        if (isEnabledApp(lpparam, targetPackageName)) {
            XposedHelpers.findAndHookMethod(Environment.class, "getExternalStorageDirectory", new Object[]{this.getExternalStorageDirectoryHook});
            v1 = XposedHelpers.findClass("android.app.ContextImpl", lpparam.classLoader);
            v3 = new Object[v7];
            v3[0] = String.class;
            v3[1] = this.getExternalFilesDirHook;
            XposedHelpers.findAndHookMethod(v1, "getExternalFilesDir", v3);
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass("android.app.ContextImpl", lpparam.classLoader), "getObbDir", new Object[]{this.getObbDirHook});
            v1 = Environment.class;
            v3 = new Object[v7];
            v3[0] = String.class;
            v3[1] = this.getExternalStoragePublicDirectoryHook;
            XposedHelpers.findAndHookMethod(v1, "getExternalStoragePublicDirectory", v3);
            if (Build.VERSION.SDK_INT >= v9) {
                v1 = XposedHelpers.findClass("android.app.ContextImpl", lpparam.classLoader);
                v3 = new Object[v7];
                v3[0] = String.class;
                v3[1] = this.getExternalFilesDirsHook;
                XposedHelpers.findAndHookMethod(v1, "getExternalFilesDirs", v3);
                XposedHelpers.findAndHookMethod(XposedHelpers.findClass("android.app.ContextImpl", lpparam.classLoader), "getObbDirs", new Object[]{this.getObbDirsHook});
            }
        }
    }

    public int[] appendInt(int[] cur, int val) {
        if (cur == null) {
            return new int[]{val};
        }
        final int N = cur.length;
        for (int i = 0; i < N; i++) {
            if (cur[i] == val) {
                return cur;
            }
        }
        int[] ret = new int[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public void changeDirPath(XC_MethodHook.MethodHookParam param) {
        File oldDirPath = (File) param.getResult();
        if (oldDirPath == null) {
            return;
        }
        String customInternalSd = getCustomInternalSd();
        if (customInternalSd.isEmpty()) {
            return;
        }
        String internalSd = getInternalSd();
        if (internalSd.isEmpty()) {
            return;
        }

        String dir = Common.appendFileSeparator(oldDirPath.getPath());
        String newDir = dir.replaceFirst(internalSd,
                customInternalSd);
        File newDirPath = new File(newDir);
        if (!newDirPath.exists()) {
            newDirPath.mkdirs();
        }
        param.setResult(newDirPath);
    }

    public void changeDirsPath(XC_MethodHook.MethodHookParam param) {
        File[] oldDirPaths = (File[]) param.getResult();
        ArrayList<File> newDirPaths = new ArrayList<File>();
        for (File oldDirPath : oldDirPaths) {
            if (oldDirPath != null) {
                newDirPaths.add(oldDirPath);
            }
        }

        String customInternalSd = getCustomInternalSd();
        if (customInternalSd.isEmpty()) {
            return;
        }

        String internalSd = getInternalSd();
        if (internalSd.isEmpty()) {
            return;
        }

        String dir = Common.appendFileSeparator(oldDirPaths[0].getPath());
        String newDir = dir.replaceFirst(internalSd, customInternalSd);
        File newDirPath = new File(newDir);

        if (!newDirPaths.contains(newDirPath)) {
            newDirPaths.add(newDirPath);
        }
        if (!newDirPath.exists()) {
            newDirPath.mkdirs();
        }

        File[] appendedDirPaths = newDirPaths.toArray(new File[newDirPaths
                .size()]);
        param.setResult(appendedDirPaths);
    }

    public String getCustomInternalSd() {
        String customInternalSd = getInternalSd();
        customInternalSd = Common.appendFileSeparator(customInternalSd);
        return customInternalSd;
    }

    public String getInternalSd() {
        internalSd = Common.appendFileSeparator(internalSd);
        return internalSd;
    }

    public boolean isEnabledApp(XC_LoadPackage.LoadPackageParam lpparam, String targePackageName) {
        String packageName = lpparam.packageName;
        return targePackageName.equals(packageName);
    }

    private void initHook() {
        getExternalStorageDirectoryHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                changeDirPath(param);
            }
        };

        getExternalFilesDirHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                changeDirPath(param);

            }
        };

        getObbDirHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                changeDirPath(param);
            }
        };

        getExternalStoragePublicDirectoryHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                changeDirPath(param);
            }
        };

        getExternalFilesDirsHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                changeDirsPath(param);
            }
        };

        getObbDirsHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                changeDirsPath(param);
            }
        };

        externalSdCardAccessHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                String permission = (String) param.args[1];
                boolean externalSdCardFullAccess = true;
                if (!externalSdCardFullAccess) {
                    return;
                }
                if (Common.PERM_WRITE_EXTERNAL_STORAGE
                        .equals(permission)
                        || Common.PERM_ACCESS_ALL_EXTERNAL_STORAGE
                        .equals(permission)) {
                    Class<?> process = XposedHelpers.findClass(
                            "android.os.Process", null);
                    int gid = (Integer) XposedHelpers.callStaticMethod(process,
                            "getGidForName", "media_rw");
                    Object permissions = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        permissions = XposedHelpers.getObjectField(
                                param.thisObject, "mPermissions");
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                        Object settings = XposedHelpers.getObjectField(
                                param.thisObject, "mSettings");
                        permissions = XposedHelpers.getObjectField(settings,
                                "mPermissions");
                    }
                    Object bp = XposedHelpers.callMethod(permissions, "get",
                            permission);
                    int[] bpGids = (int[]) XposedHelpers.getObjectField(bp,
                            "gids");
                    XposedHelpers.setObjectField(bp, "gids",
                            appendInt(bpGids, gid));
                }
            }
        };


        externalSdCardAccessHook2 = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                boolean externalSdCardFullAccess = true;
                if (!externalSdCardFullAccess) {
                    return;
                }
                Object extras = XposedHelpers.getObjectField(param.args[0], "mExtras");
                Object ps = XposedHelpers.callMethod(extras, "getPermissionsState");
                Object settings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                Object permissions = XposedHelpers.getObjectField(settings, "mPermissions");
                boolean hasPermission = (boolean) XposedHelpers.callMethod(ps, "hasInstallPermission", Common.PERM_WRITE_MEDIA_STORAGE);
                if (!hasPermission) {
                    Object permWriteMediaStorage = XposedHelpers.callMethod(permissions, "get",
                            Common.PERM_WRITE_MEDIA_STORAGE);
                    XposedHelpers.callMethod(ps, "grantInstallPermission", permWriteMediaStorage);
                }

            }
        };
    }
}

