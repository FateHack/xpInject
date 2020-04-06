package com.fate.xpInject;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by jy on 2018/12/17.
 */

public class HookMain implements IXposedHookLoadPackage {
    private static final String INJECT = "InjectedApp";
    private static final String TAG = "Fuck";
    private static final String LIB = "fate";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.fate.xpInject")) {
            XposedHelpers.findAndHookMethod("com.fate.xpInject.MainActivity", lpparam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }
        String packageName = FileUtils.getAppName();
        String soName = FileUtils.getSoName();
        if (packageName != null && soName != null) {
            //Log.i(TAG, "packageName and soName are not null!");
            int i = soName.indexOf(".");
            final String finalSoName = soName.substring(3, i);
            if (packageName != null && soName != null) {
                if (lpparam.packageName.equals(packageName)) {
                    Log.i(TAG, "Ok!");
                    XposedHelpers.findAndHookMethod("dalvik.system.BaseDexClassLoader", ClassLoader.getSystemClassLoader(), "findLibrary", String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.args[0].toString().contains(finalSoName)) {
                                Log.i(TAG, "inject");
                                param.args[0] = LIB;
                            }
                        }
                    });
                }
            }
        }
    }
}

