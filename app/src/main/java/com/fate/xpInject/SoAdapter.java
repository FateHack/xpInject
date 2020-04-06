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
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

/**
 * Created by jy on 2018/12/17.
 */

public class SoAdapter extends ArrayAdapter<String> {
    private static final String INJECT = "InjectedApp";
    private int resourceId;

    public SoAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        SharedPreferences preference = GlobalApplication.getInstance().getSharedPreferences(INJECT, Context.MODE_PRIVATE);
        String selectedSo = preference.getString("soName", null);
        String selectedApp = preference.getString("App", null);
        String so = getItem(position);
        TextView soName = (TextView) view.findViewById(R.id.soName);
        soName.setText(so);
        try {
            if (selectedSo != null && isSelectedApp(selectedApp)) {
                if (selectedSo.equals(so)) {
                    view.setBackgroundColor(Color.argb(60, 0, 0, 255));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;

    }

    public boolean isSelectedApp(String selectedApp) throws IOException {
        String currentApp = FileUtils.getStr("/sdcard/currentApp");
        return selectedApp.equals(currentApp);
    }


}
