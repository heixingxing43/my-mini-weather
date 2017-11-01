package com.example.heixingxing.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.heixingxing.bean.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heixingxing on 2017/10/18.
 */

public class CityDB {
    public static final String CITY_DB_NAME = "city.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;

    public CityDB(Context context, String path){
        db = context.openOrCreateDatabase(path,Context.MODE_PRIVATE,null);
    }

    public List<City> getAllCity(){
        List<City> list = new ArrayList<City>();
        Cursor c = db.rawQuery("SELECT * from "+ CITY_TABLE_NAME, null);
        while(c.moveToNext()){
            String province = c.getString(c.getColumnIndex("province"));
            //Log.i("MyApp",province);
            String city = c.getString(c.getColumnIndex("city"));
            //Log.i("MyApp",city);
            String number = c.getString(c.getColumnIndex("number"));
            //Log.i("MyApp",number);
            String allPY = c.getString(c.getColumnIndex("allpy"));
            //Log.i("MyApp",allPY);//这里读不出来,这里要从数据库里获取,所以要和数据库的列名一致
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            //Log.i("MyApp",allFirstPY);
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            //Log.i("MyApp",firstPY);
            City item = new City(province,city,number,firstPY,allPY,allFirstPY);
            list.add(item);
        }
        return list;
    }
}
