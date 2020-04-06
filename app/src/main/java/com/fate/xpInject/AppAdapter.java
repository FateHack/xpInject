package com.fate.xpInject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jy on 2018/12/17.
 */

public class AppAdapter extends ArrayAdapter<AppInfo> {

    private static final String INJECT="InjectedApp";

    private int resourceId;
    public AppAdapter(@NonNull Context context, int resource, @NonNull List<AppInfo> objects) {
        super(context, resource, objects);
        resourceId=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AppInfo appInfo=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        ImageView appImage=(ImageView)view.findViewById(R.id.appIcon);
        appImage.setImageDrawable(appInfo.getAppIcon());
        TextView appName=(TextView)view.findViewById(R.id.appName);
        appName.setText(appInfo.getAppName());
       // appName.setTextColor(Color.RED);
        SharedPreferences preference =GlobalApplication.getInstance().getSharedPreferences(INJECT, Context.MODE_PRIVATE);
        String app = preference.getString("App", null);
      //  Log.e("app",app);
        if(appInfo.getPackageName().equals(app)){
            view.setBackgroundColor(Color.argb(60,0,0,255));
        }
        TextView versionName=(TextView)view.findViewById(R.id.versionName);
        versionName.setText("v:"+appInfo.getVersionName());
        return view;
    }
}
