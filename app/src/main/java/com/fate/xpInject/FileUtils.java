package com.fate.xpInject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jy on 2018/12/18.
 */

public class FileUtils {
    public static String getStr(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()){
            return null;
        }
        FileInputStream fis=new FileInputStream(file);
        byte[] buffer=new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        return new String(buffer,"UTF-8");
    }


    public static void writeStr(String path,String content) throws IOException {
        File file=new File(path);
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream fos=new FileOutputStream(file);
        fos.write(content.getBytes());
        fos.close();

    }

    public static String getAppName(){
        try {
            return FileUtils.getStr("/sdcard/App");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSoName(){
        try {
            return FileUtils.getStr("/sdcard/soName");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
