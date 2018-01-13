package com.example.heixingxing.Listener;

import android.os.Message;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.heixingxing.app.MyApplication;
import com.example.heixingxing.bean.City;
import com.example.heixingxing.test2.MainActivity;

import java.util.List;


/**
 * Created by heixingxing on 2017/12/20.
 */

public class MyLocationListener extends BDAbstractLocationListener {
    private MainActivity context;
    private MyApplication myApplication;
    private List<City> mCityList;

    public MyLocationListener(MainActivity context){
        this.context = context;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        if(location == null){
            sendMassage("-1");
            return;
        }

        if(location.getAddrStr()==null){
            sendMassage("-1");
            return;
        }

        String province = getSubstring(location.getProvince());    //获取省份
        String city = getSubstring(location.getCity());    //获取城市
        String district = getSubstring(location.getDistrict());    //获取区县

        Log.d("location","country "+province);
        Log.d("location","locate city "+city);

        if(!ifCityExist(district).equals("-1")){
            sendMassage(ifCityExist(district));
        }else if(!ifCityExist(city).equals("-1")){
            sendMassage(ifCityExist(city));
        }else{
            sendMassage("-1");
        }
    }
    public String getSubstring(String str){
        return str.substring(0, str.length()-1);
    }

    //判断城市名是否在列表里
    private String ifCityExist(String cityName){
        myApplication = (MyApplication) context.getApplication();
        mCityList = myApplication.getCityList();
        for(City city:mCityList){
            if(city.getCity().equals(cityName)){
                return city.getNumber();    //若存在则返回城市代码
            }
        }
        return "-1";
    }

    //通过消息进行传递
    private void sendMassage(String citycode){
        Message message = new Message();
        message.what = MainActivity.LOCATION;
        message.obj = citycode;
        context.getmHandler().sendMessage(message);
    }
}
