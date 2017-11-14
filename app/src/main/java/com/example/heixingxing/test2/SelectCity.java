package com.example.heixingxing.test2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.heixingxing.app.MyApplication;
import com.example.heixingxing.bean.City;
import com.example.heixingxing.db.CityDB;

import java.util.List;

/**
 * Created by heixingxing on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackbtn;
    private ListView mList;
    private EditText eSearch;
    //private ClearEditText mClearEditText;
    private List<City> cityList;
    private String[] tmp = new String[4000];//"num1","num2","num3","num4","num5"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();

        //mBackbtn = (ImageView)findViewById(R.id.title_back);
        //mBackbtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.title_back:
                //传递数据
                Intent i = new Intent();
                i.putExtra("cityCode","101100101");//101160101
                setResult(RESULT_OK,i);

                finish();
                //Toast.makeText(SelectCity.this,"test",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    private void initViews(){
        mBackbtn = (ImageView)findViewById(R.id.title_back);
        mBackbtn.setOnClickListener(this);
        mList = (ListView)findViewById(R.id.title_list);
        eSearch = (EditText)findViewById(R.id.search_city);

        MyApplication myApplication = (MyApplication)getApplication();

        cityList = myApplication.getCityList();

        int i=0;
        for(City city:cityList){
            //filterDateList.add(city);
            tmp[i++] = city.getCity();//+" "+city.getNumber()
            //Log.i("MyWeather",tmp[i-1]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                SelectCity.this, android.R.layout.simple_list_item_1, tmp);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapterView, View view, int i, long l){
                City city = cityList.get(i);
                Intent intent = new Intent();
                intent.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,intent);
                finish();
                //Log.i("MyWeather",city.getNumber());
            }
        });
    }
}
