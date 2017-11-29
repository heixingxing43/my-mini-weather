package com.example.heixingxing.test2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.heixingxing.app.MyApplication;
import com.example.heixingxing.bean.City;

/**
 * Created by heixingxing on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackbtn;
    private EditText eSearch;
    private ListView mListView;
    private MyApplication myApplication;

    private List<City> cityList;
    private ArrayList<City> selectedList;
    private ArrayList<String> mArrayList;
    private TextView mText;
    private ArrayAdapter<String> adapter;
    private String newCityCode="-1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackbtn = (ImageView)findViewById(R.id.title_back);
        mBackbtn.setOnClickListener(this);
        eSearch = (EditText) findViewById(R.id.search_city);
        eSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterData(s.toString());
            }
        });

        mListView = (ListView) findViewById(R.id.city_list);
        myApplication = (MyApplication)getApplication();
        cityList = myApplication.getCityList();

        selectedList = new ArrayList<City>();
        for(City city : cityList){
            selectedList.add(city);
        }

        //string arraylist和selected一致 用于绑定监听器
        mArrayList = new ArrayList<String>();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                //Toast.makeText(SelectCity.this, "点击了城市" + mArrayList.get(i), Toast.LENGTH_SHORT).show();
                newCityCode = selectedList.get(i).getNumber();
                Intent intent = new Intent();
                intent.putExtra("cityCode", newCityCode);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


        for(int j = 0; j < selectedList.size(); j++) {
            mArrayList.add(selectedList.get(j).getCity() + "-" + selectedList.get(j).getProvince());
        }

        adapter = new ArrayAdapter<String>(
                SelectCity.this, android.R.layout.simple_list_item_1, mArrayList);
        mListView.setAdapter(adapter);

        /*Intent intent = this.getIntent();
        String cityname = intent.getStringExtra("city");

        mText = (TextView)findViewById(R.id.title_name);
        mText.setText("当前城市：" + cityname);*/

        //initViews();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.title_back:
                //传递数据
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String citycode = sharedPreferences.getString("main_city_code","101100101");
                Intent i = new Intent();
                i.putExtra("cityCode",citycode);//101160101
                setResult(RESULT_OK,i);
                finish();
                //Toast.makeText(SelectCity.this,"test",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    public void filterData(String filterStr){
        if(TextUtils.isEmpty(filterStr)){
            for(City city : cityList){
                selectedList.add(city);
            }
        }else{
            //这句必须加，否则下一次查找还会留有这一次的搜索结果
            selectedList.clear();
            for(City city : cityList){
                if (city.getCity().contains(filterStr) || city.getProvince().contains(filterStr)) {
                    selectedList.add(city);
                }
            }
        }

        mArrayList.clear();
        for (City city : selectedList) {
            mArrayList.add(city.getCity() + " （" + city.getProvince() +"）");
        }

        adapter.notifyDataSetChanged();
    }
}
