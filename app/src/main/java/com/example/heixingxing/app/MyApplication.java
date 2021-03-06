package com.example.heixingxing.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.example.heixingxing.bean.City;
import com.example.heixingxing.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by heixingxing on 2017/10/18.
 */

public class MyApplication extends Application{
    private static final String TAG = "MyApp";
    private static MyApplication mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication = this;
        mCityDB = openCityDB();
        initCityList();
    }

    //初始化城市列表
    public void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList(){
        mCityList = mCityDB.getAllCity();
        int i = 0;
        for(City city : mCityList){
            i++;
            String cityName = city.getCity();
            String province = city.getProvince();
            String cityCode = city.getNumber();
            //Log.d(TAG,cityCode+": "+cityName+" "+i);
        }
        Log.d(TAG,"i="+i);
        Log.d(TAG," "+mCityList);
        return true;
    }
    public List<City> getCityList(){
        return mCityList;
    }

    public static MyApplication getInstance(){
        return mApplication;
    }

    //创建打开数据库的方法
    private CityDB openCityDB(){
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        //数据库不存在时新建
        if(!db.exists()){
            String pathholder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathholder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try{
                InputStream is = getAssets().open("city.db");   //打开数据库文件
                FileOutputStream fos = new FileOutputStream(db);
                int len = 1;
                byte[] buffer = new byte[1024];
                while((len = is.read(buffer)) != -1){
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            }catch(IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }
}
