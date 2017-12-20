package com.example.heixingxing.util;

import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * Created by heixingxing on 2017/12/16.
 */

public class GetLocateListener implements BDLocationListener{

    @Override
    public void onReceiveLocation(BDLocation location) {
        if(location != null){
            StringBuffer sb = new StringBuffer();
            sb.append("时间：     " + location.getTime() + "\n");
            sb.append("定位类型： " + location.getLocType() + "\n");
            sb.append("纬度：     " + location.getLatitude() + "\n");
            sb.append("经度：     " + location.getAltitude() + "\n");
            sb.append("半径范围： " + location.getRadius() + "\n");
            sb.append("速度：     " + location.getSpeed() + "\n");
            sb.append("卫星数量： " + location.getSatelliteNumber() + "\n");
            sb.append("当前地址： " + location.getAddress() + "\n");
            sb.append("方位：     " + location.getDirection() + "\n");
            sb.append("国家：     " + location.getCountry() + "\n");
            sb.append("省：       " + location.getProvince() + "\n");
            sb.append("市：       " + location.getCity() + "\n");
            sb.append("街区：     " + location.getDistrict() + "\n");
            sb.append("定位时间： " + location.getTime() + "\n");
        }
    }
}
