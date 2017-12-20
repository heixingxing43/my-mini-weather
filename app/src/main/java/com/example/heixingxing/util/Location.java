package com.example.heixingxing.util;

import android.content.Context;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by heixingxing on 2017/12/16.
 */

public class Location {
    private LocationClient ltc = null;
    private LocationClientOption ltcoption = null;
    private GetLocateListener getlclistener;

    public void startLocation(Context context){
        initLocation(context);
        ltc.setLocOption(ltcoption);
        ltc.start();
    }

    //初始化定位信息
    public void initLocation(Context context){
        ltc = new LocationClient(context);
        ltcoption = new LocationClientOption();

        ltcoption.setIsNeedAddress(true);
        ltcoption.setOpenGps(true);
        ltcoption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        ltcoption.setCoorType("bd0911");//设置返回值坐标类型bd09ll表示百度经纬度坐标,gcj02表示经过国测局加密的坐标,wgs84表示gps获取的坐标
        ltcoption.setScanSpan(1000);//设置间隔性定位
        ltcoption.setTimeOut(30000);
        ltc.setLocOption(ltcoption);

        getlclistener = new GetLocateListener();
        ltc.registerLocationListener(getlclistener);
    }

    private void stopLocation(){
        ltc.stop();
    }
}
