package com.fate.xpInject;
import java.io.File;

public class Common {
    public static final String CLASS_PACKAGE_PARSER_PACKAGE = "android.content.pm.PackageParser.Package";
    public static final String[] MTP_APPS = new String[]{"com.android.MtpApplication", "com.samsung.android.MtpApplication"};
    public static final String PERM_ACCESS_ALL_EXTERNAL_STORAGE = "android.permission.ACCESS_ALL_EXTERNAL_STORAGE";
    public static final String PERM_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String PERM_WRITE_MEDIA_STORAGE = "android.permission.WRITE_MEDIA_STORAGE";

    private Common() {
        super();
    }

    public static String appendFileSeparator(String arg2) {
        if(!arg2.endsWith(File.separator)) {
            arg2 = arg2 + File.separator;
        }

        return arg2;
    }
}
