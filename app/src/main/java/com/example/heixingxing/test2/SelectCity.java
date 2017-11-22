package com.example.heixingxing.test2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.heixingxing.edit.ClearEditTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heixingxing on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackbtn;
    private ListView mList;
    private EditText eSearch;
    private ClearEditTest mClearEditText;
    private List<City> cityList;
    private List<City> filterDataList;
    private ArrayAdapter<String> adapter;
    private String[] tmp = new String[4000];//"num1","num2","num3","num4","num5"
    private String[] tmp1 = new String[4000];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initViews();
        search_City();

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
        //eSearch = (EditText)findViewById(R.id.search_city);

        MyApplication myApplication = (MyApplication)getApplication();

        cityList = myApplication.getCityList();

        int i=0;
        for(City city:cityList){
            //filterDateList.add(city);
            tmp[i++] = city.getCity();//+" "+city.getNumber()
            //Log.i("MyWeather",tmp[i-1]);
        }

        adapter = new ArrayAdapter<String>(
                SelectCity.this, android.R.layout.simple_list_item_1, tmp); //ArrayAdapter<String>
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapterView, View view, int i, long l){
                City city = cityList.get(i);
                Log.d("filter","!null");
                Intent intent = new Intent();
                intent.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,intent);
                finish();
                //Log.i("MyWeather",city.getNumber());
            }
        });
    }

    public void search_City(){
        eSearch = (EditText) findViewById(R.id.search_city);
        eSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
                adapter = new ArrayAdapter<String>(
                        SelectCity.this, android.R.layout.simple_list_item_1, tmp1); //ArrayAdapter<String>
                mList.setAdapter(adapter);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Intent intent = new Intent();
                for(int j=0; j<tmp.length; j++){
                    if(tmp[j] == tmp1[i]){
                        i=j;
                        break;
                    }
                }
                City city = cityList.get(i);
                Log.d("filter",city.getNumber());
                intent.putExtra("cityCode", city.getNumber());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void filterData(String filterStr){
        Log.d("filter",filterStr+"asd");
        boolean flag = false;
        int i = 0;
        if(TextUtils.isEmpty(filterStr)){
            Log.d("filter","list empty");
            for(City city : cityList){
                tmp1[i++] = city.getCity();
            }
        }else{
            i=0;
            for(City city : cityList){
                if(city.getCity().indexOf(filterStr.toString()) != -1){
                    tmp1[i] = city.getCity();
                    Log.d("filter",tmp1[i]+"qwe");
                    i++;
                    flag=true;
                }
            }
        }
        if(flag == false){
            Log.d("filter","flag=false");
            tmp1 = tmp;
            Toast.makeText(SelectCity.this, "未找到城市", Toast.LENGTH_LONG).show();
        }else{
            for(int j=i; j<tmp1.length; j++){
                tmp1[j]=" ";
            }
        }

        //mList.setAdapter(adapter);
    }
}
