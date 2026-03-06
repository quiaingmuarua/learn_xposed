package com.demo.java.xposed;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.demo.java.xposed.base.BaseItemListActivity;
import com.demo.java.xposed.base.InfoItem;
import com.demo.java.xposed.base.Status;
import com.demo.java.xposed.device.PluginInit;
import com.demo.java.xposed.device.model.SimInfoModel;
import com.example.java.demo.R;

public class


MainActivity extends BaseItemListActivity {
    private static final int REQUEST_CODE_SMS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            TextView titleView = findViewById(R.id.text_view_title);
            titleView.setText(PluginInit.version);

            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            startSdk();
        } catch (Exception e) {
            Log.e("MainActivity", "onCreate: ", e);

        }

    }


    public void startSdk() {
        // 创建样例数据
        Log.d("MainActivity", "startSdk: ");
        infoItemList.add(InfoItem.withResult("plugin info", PluginInit.gsonInfo(), Status.TODOCheck, PluginInit::gsonInfo));
//        infoItemList.add(InfoItem.withResult("filePath config", FilePathConfig.toJSON(SimInfoModel.getInstance().getPhoneNumber(true,false)), Status.TODOCheck, () -> FilePathConfig.toJSON(SimInfoModel.getInstance().getPhoneNumber())));
        setupResultAdapter();

    }
}




