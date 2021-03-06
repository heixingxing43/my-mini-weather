package com.example.heixingxing.test2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.heixingxing.Listener.MyLocationListener;
import com.example.heixingxing.app.MyApplication;
import com.example.heixingxing.bean.TodayWeather;
import com.example.heixingxing.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener{
    private static final int UPDATE_TODAY_WEATHER = 1;
    public static final int LOCATION = 2;

    private ImageView mUpdateBtn;   //更新天气添加onclick事件
    private ImageView mCitySelect;  //选择城市添加onclick事件
    private ImageView mShareBtn;    //分享按钮
    private ImageView mLocateBtn;   //定位当前城市
    private ProgressBar mUpdateProg;

    //初始化界面控件
    private TextView cityTv, timeTv, humidityTv, temperature_detail_Tv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    //滑动效果监听器
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views = new ArrayList<View>();

    //设置滑动效果小圆点
    private ImageView[] dots;
    private int[] ids = {R.id.iv1, R.id.iv2};

    //更新未来几日天气
    private TextView firstDateTv,firstTptTv,firstClimateTv,firstFengliTv;
    private TextView secondDateTv,secondTptTv,secondClimateTv,secondFengliTv;
    private TextView thirdDateTv,thirdTptTv,thirdClimateTv,thirdFengliTv;
    private TextView forthDateTv,forthTptTv,forthClimateTv,forthFengliTv;
    private ImageView firstWeaImg, secondWeaImg, thirdWeaImg, forthWeaImg;

    //定位
    public LocationClient mLocationClient = null;
    public LocationClientOption option = null;
    private MyLocationListener myListener;
    //private String locate_city = null;

    //构建消息句柄
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case LOCATION:
                    String citycode = (String)msg.obj;
                    Log.d("location",citycode);
                    if(citycode.equals("-1")){
                        Toast.makeText(MainActivity.this,"Fail to get location",Toast.LENGTH_SHORT).show();
                    }else{
                        queryWeatherCode(citycode);
                    }
                    if(mLocationClient.isStarted()) mLocationClient.stop();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        //add in 10.11 about network
        //为刷新添加onclick事件
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        //更新图标旋转
        mUpdateProg = (ProgressBar)findViewById(R.id.title_update_prog);
        //为选择城市添加onclick事件
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        //定位城市添加onclick事件
        mLocateBtn = (ImageView) findViewById(R.id.title_location);
        mLocateBtn.setOnClickListener(this);
        //分享功能
        mShareBtn = (ImageView)findViewById(R.id.title_share);
        mShareBtn.setOnClickListener(this);

        initView(); //初始化控件

        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            queryWeatherCode(cityCode);
            Log.d("myWeather","Network is OK1.");
            Log.d("myWeather",cityCode);
            Toast.makeText(MainActivity.this,"Network is OK.", Toast.LENGTH_LONG).show();
        }else{
            Log.d("myWeather","Network is not OK.");
            Toast.makeText(MainActivity.this,"Network is not OK.", Toast.LENGTH_LONG).show();
        }

        initDots();
    }

    //对xml文件中的控件进行初始化，findViewById用来获取xml文件中的相应按钮
    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        temperature_detail_Tv = (TextView) findViewById(R.id.temperature_detail);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        LayoutInflater inflater = LayoutInflater.from(this);
        views.add(inflater.inflate(R.layout.weather1, null));
        views.add(inflater.inflate(R.layout.weather2, null));
        //实例化一个vp监听器
        vpAdapter = new ViewPagerAdapter(views,this);
        //通过findViewById方法找到viewpager对象
        vp = (ViewPager)findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        //小圆点
        vp.setOnPageChangeListener(this);

        firstDateTv = (TextView)views.get(0).findViewById(R.id.firstday_date);
        firstWeaImg = (ImageView)views.get(0).findViewById(R.id.firstday_weather);
        firstTptTv = (TextView)views.get(0).findViewById(R.id.firstday_temperature);
        firstClimateTv = (TextView)views.get(0).findViewById(R.id.firstday_climate);
        firstFengliTv = (TextView)views.get(0).findViewById(R.id.firstday_wind);

        secondDateTv = (TextView)views.get(0).findViewById(R.id.secondday_date);
        secondWeaImg = (ImageView)views.get(0).findViewById(R.id.secondday_weather);
        secondTptTv = (TextView)views.get(0).findViewById(R.id.secondday_temperature);
        secondClimateTv = (TextView)views.get(0).findViewById(R.id.secondday_climate);
        secondFengliTv = (TextView)views.get(0).findViewById(R.id.secondday_wind);

        thirdDateTv = (TextView)views.get(1).findViewById(R.id.thirdday_date);
        thirdWeaImg = (ImageView)views.get(1).findViewById(R.id.thirdday_weather);
        thirdTptTv = (TextView)views.get(1).findViewById(R.id.thirdday_temperature);
        thirdClimateTv = (TextView)views.get(1).findViewById(R.id.thirdday_climate);
        thirdFengliTv = (TextView)views.get(1).findViewById(R.id.thirdday_wind);

        forthDateTv = (TextView)views.get(1).findViewById(R.id.forthday_date);
        forthWeaImg = (ImageView)views.get(1).findViewById(R.id.forthday_weather);
        forthTptTv = (TextView)views.get(1).findViewById(R.id.forthday_temperature);
        forthClimateTv = (TextView)views.get(1).findViewById(R.id.forthday_climate);
        forthFengliTv = (TextView)views.get(1).findViewById(R.id.forthday_wind);

        firstWeaImg = (ImageView)views.get(0).findViewById(R.id.firstday_weather);
        secondWeaImg = (ImageView)views.get(0).findViewById(R.id.secondday_weather);
        thirdWeaImg = (ImageView)views.get(1).findViewById(R.id.thirdday_weather);
        forthWeaImg = (ImageView)views.get(1).findViewById(R.id.forthday_weather);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        temperature_detail_Tv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

    //add in 10.11
    @Override
    public void onClick(View v) {
        //关联城市图标 add in 10.18.2017
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code","101010100");
        String cityName = sharedPreferences.getString("cityName","上海");
        if(v.getId()==R.id.title_location){
            Log.d("location","getlocation start");
            getLocation();
        }
        if(v.getId() == R.id.title_share){
            Log.d("share","share");
            getScreenShot();
        }
        if(v.getId()==R.id.title_city_manager){//点击选择城市的图标
            Intent i = new Intent(this, SelectCity.class);
            i.putExtra("cityName", cityName);
            startActivityForResult(i,1);    //通过intent传递信息
        }
        //get cityID
        if(v.getId() == R.id.title_update_btn){
            //从SharedPreferences中读取城市ID。如果未定义则缺省值为101010100
//            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("location",cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather", "network is OK");
                queryWeatherCode(cityCode); //从网络中获取城市信息
            }else{
                Log.d("myWeather", "network is not OK");
                Toast.makeText(MainActivity.this,"network is not OK",Toast.LENGTH_LONG).show();
            }
        }
    }

    //设置PM图片
    void setPM25Image(int pm){
        if(pm>=0 && pm<=50){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }
        else if(pm>50 && pm<=100){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        }
        else if(pm>100 && pm<=150){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        }
        else if(pm>150 && pm<=200){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
        }
        else if(pm>200 && pm<=300){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        }
        else if(pm>=300){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }
    }

    //设置天气图片
    void setClimateImage(String type, ImageView img){
        switch (type){
            case "暴雪":
                img.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "暴雨":
                img.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                img.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "大雪":
                img.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "大雨":
                img.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "多云":
                img.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "雷阵雨":
                img.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                img.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "晴":
                img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
            case "沙尘暴":
                img.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "小雪":
                img.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "特大暴雨":
                img.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "雾":
                img.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "阴":
                img.setImageResource(R.drawable.biz_plugin_weather_yin);
                break;
            case "小雨":
                img.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "雨夹雪":
                img.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "阵雪":
                img.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                break;
            case "阵雨":
                img.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                break;
            case "中雪":
                img.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "中雨":
                img.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            default:
                img.setImageResource(R.drawable.biz_plugin_weather_qing);
                break;
        }
    }

    //利用TodayWeather对象更新UI中的控件
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");

        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("cityName", todayWeather.getCity());
        editor.commit();

        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        temperature_detail_Tv.setText("温度："+todayWeather.getWendu()+"℃");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());

        firstDateTv.setText(todayWeather.getDate1());
        firstTptTv.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
        firstClimateTv.setText(todayWeather.getType1());
        firstFengliTv.setText("风力:"+todayWeather.getFengli1());

        secondDateTv.setText(todayWeather.getDate2());
        secondTptTv.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
        secondClimateTv.setText(todayWeather.getType2());
        secondFengliTv.setText("风力:"+todayWeather.getFengli2());

        thirdDateTv.setText(todayWeather.getDate3());
        thirdTptTv.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
        thirdClimateTv.setText(todayWeather.getType3());
        thirdFengliTv.setText("风力:"+todayWeather.getFengli3());

        forthDateTv.setText(todayWeather.getDate4());
        forthTptTv.setText(todayWeather.getHigh4()+"~"+todayWeather.getLow4());
        forthClimateTv.setText(todayWeather.getType4());
        forthFengliTv.setText("风力:"+todayWeather.getFengli4());

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();

        if(todayWeather.getPm25()==null){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            pmDataTv.setText("无pm值");
            pmQualityTv.setText("无法估计空气质量");
        }else{
            int pm = Integer.parseInt(todayWeather.getPm25());
            setPM25Image(pm);
        }

        //根据天气类型更新图标
        setClimateImage(todayWeather.getType(), weatherImg);
        setClimateImage(todayWeather.getType1(), firstWeaImg);
        setClimateImage(todayWeather.getType2(), secondWeaImg);
        setClimateImage(todayWeather.getType3(), thirdWeaImg);
        setClimateImage(todayWeather.getType4(), forthWeaImg);

        mUpdateBtn.setVisibility(View.VISIBLE);
        mUpdateProg.setVisibility(View.INVISIBLE);
    }

    //编写解析函数，解析出城市名称以及更新时间信息
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            //Toast.makeText(MainActivity.this,"parseXML", Toast.LENGTH_LONG).show();
            while(eventType != XmlPullParser.END_DOCUMENT){ //xml文档没有结束
                switch (eventType){
                    //判断当前时间是否为文档开始时间
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前时间是否为标签元素开始时间
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType = xmlPullParser.next();   //按顺序读取每一个信息
                                todayWeather.setCity(xmlPullParser.getText());
                                //Log.d("myWeather","城市:  "+xmlPullParser.getText());
                                //Toast.makeText(MainActivity.this,"city: "+xmlPullParser.getText(), Toast.LENGTH_LONG).show();
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                //Log.d("myWeather","更新时间:  "+xmlPullParser.getText());
                                //Toast.makeText(MainActivity.this,"updatetime:  "+xmlPullParser.getText(), Toast.LENGTH_LONG).show();
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                //Log.d("myWeather","湿度： "+xmlPullParser.getText());
                            }else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                //Log.d("myWeather", "温度:    "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                //Log.d("myWeather", "pm2.5:    "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                //Log.d("myWeather", "质量:    "+xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                //Log.d("myWeather", "风向:    "+xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang1(xmlPullParser.getText());
                                //Log.d("myWeather", "风向:    "+xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang2(xmlPullParser.getText());
                                //Log.d("myWeather", "风向:    "+xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang3(xmlPullParser.getText());
                                //Log.d("myWeather", "风向:    "+xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang4(xmlPullParser.getText());
                                //Log.d("myWeather", "风向:    "+xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                //Log.d("myWeather", "风力:    "+xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                //Log.d("myWeather", "风力:    "+xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                //Log.d("myWeather", "风力:    "+xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                //Log.d("myWeather", "风力:    "+xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli4(xmlPullParser.getText());
                                //Log.d("myWeather", "风力:    "+xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                //Log.d("myWeather", "日期:    "+xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate1(xmlPullParser.getText());
                                //Log.d("myWeather", "日期:    "+xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate2(xmlPullParser.getText());
                                //Log.d("myWeather", "日期:    "+xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate3(xmlPullParser.getText());
                                //Log.d("myWeather", "日期:    "+xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate4(xmlPullParser.getText());
                                //Log.d("myWeather", "日期:    "+xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最高温:    "+xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最高温:    "+xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最高温:    "+xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最高温:    "+xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh4(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最高温:    "+xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最低温:    "+xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最低温:    "+xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最低温:    "+xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最低温:    "+xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow4(xmlPullParser.getText().substring(2).trim());
                                //Log.d("myWeather", "最低温:    "+xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                //Log.d("myWeather", "类型:    "+xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                //Log.d("myWeather", "类型:    "+xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                //Log.d("myWeather", "类型:    "+xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                //Log.d("myWeather", "类型:    "+xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType4(xmlPullParser.getText());
                                //Log.d("myWeather", "类型:    "+xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        }catch(XmlPullParserException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    //获取网络数据，参数是cityCode
    private void queryWeatherCode(String cityCode){
        mUpdateBtn.setVisibility(View.INVISIBLE);
        mUpdateProg.setVisibility(View.VISIBLE);

        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);

        String cityCode2 = cityCode.substring(0, 5) + "0100";
        final String address2 = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode2;
        Log.d("myWeather", address2);

        String cityCode3 = cityCode.substring(0, 5) + "0101";
        final String address3 = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode3;
        Log.d("myWeather", address3);
        //Toast.makeText(MainActivity.this, address, Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                HttpURLConnection con2 = null;
                HttpURLConnection con3 = null;
                TodayWeather todayWeather = null;
                TodayWeather todayWeather2 = null;
                TodayWeather todayWeather3 = null;
                try{
                    //Toast.makeText(MainActivity.this, "str", Toast.LENGTH_LONG).show();
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);   //将获得的信息一条放在一起
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather","1 "+responseStr);
                    todayWeather = parseXML(responseStr);   //解析网络数据，今日天气的具体信息

                    if(address.equals(address2)){
                        todayWeather2 = null;
                    }else{
                        URL url2 = new URL(address2);
                        con2 = (HttpURLConnection)url2.openConnection();
                        con2.setRequestMethod("GET");
                        con2.setConnectTimeout(8000);
                        con2.setReadTimeout(8000);
                        InputStream in2 = con2.getInputStream();
                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(in2));
                        StringBuilder response2 = new StringBuilder();
                        String str2;
                        while((str2=reader2.readLine()) != null){
                            response2.append(str2);   //将获得的信息一条放在一起
                        }
                        String responseStr2 = response2.toString();
                        Log.d("myWeather","2 "+responseStr2);
                        todayWeather2 = parseXML(responseStr2);   //解析网络数据，今日天气的具体信息
                    }

                    if(address.equals(address3)){
                        todayWeather3 = null;
                    }else{
                        URL url3 = new URL(address3);
                        con3 = (HttpURLConnection)url3.openConnection();
                        con3.setRequestMethod("GET");
                        con3.setConnectTimeout(8000);
                        con3.setReadTimeout(8000);
                        InputStream in3 = con3.getInputStream();
                        BufferedReader reader3 = new BufferedReader(new InputStreamReader(in3));
                        StringBuilder response3 = new StringBuilder();
                        String str3;
                        while((str3=reader3.readLine()) != null){
                            response3.append(str3);   //将获得的信息一条放在一起
                        }
                        String responseStr3 = response3.toString();
                        Log.d("myWeather","3 "+responseStr3);
                        todayWeather3 = parseXML(responseStr3);   //解析网络数据，今日天气的具体信息
                    }

                    if(todayWeather != null && (todayWeather2 != null || todayWeather3 != null)){
                        //Log.d("myWeather",todayWeather.getCity()+"1");
                        //Log.d("myWeather",todayWeather2.getCity()+"2");
                        //Log.d("myWeather",todayWeather3.getCity()+"3");
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        if(todayWeather.getCity() != null){
                            Log.d("myWeather","test1");
                            msg.obj=todayWeather;
                            Log.d("myWeather","todayweather");
                        }else if(todayWeather.getCity() == null && todayWeather2 != null && todayWeather2.getCity()!=null){
                            Log.d("myWeather","test2");
                            msg.obj=todayWeather2;
                            //Toast.makeText(MainActivity.this, "无此城市信息，已为您选择临近城市.", Toast.LENGTH_SHORT).show();
                            Log.d("myWeather","todayweather2");
                        }else if(todayWeather.getCity() == null && todayWeather3 != null && todayWeather3.getCity()!=null){
                            Log.d("myWeather","test3");
                            msg.obj=todayWeather3;
                            //Toast.makeText(MainActivity.this, "无此城市信息，已为您选择临近城市.", Toast.LENGTH_SHORT).show();
                            Log.d("myWeather","todayweather3");
                        }
                        mHandler.sendMessage(msg);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    //返回主界面时传递城市的代码
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather","选择的城市代码："+newCityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather", "network is OK");
                //Toast.makeText(MainActivity.this,"network is OK",Toast.LENGTH_LONG).show();
                if(!newCityCode.equals("-1")){
                    queryWeatherCode(newCityCode);  //从网络中获取数据，参数是newCityCode
                    SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("main_city_code", newCityCode);
                    editor.commit();
                }else{
                    Toast.makeText(MainActivity.this, "Failed to match a city.", Toast.LENGTH_LONG).show();
                }
            }else{
                Log.d("myWeather", "network is not OK");
                Toast.makeText(MainActivity.this,"network is not OK",Toast.LENGTH_LONG).show();
            }
        }
    }

    //将滑动小圆点控件定义于数组中
    private void initDots(){
        dots = new ImageView[views.size()];
        for(int i=0; i<views.size(); i++){
            dots[i] = (ImageView)findViewById(ids[i]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i=0; i<ids.length; i++){
            if(i == position){
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            }else{
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void initLocationInfo(){
        Log.d("location","in init");
        option = new LocationClientOption();
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，设置定位模式，默认高精度；
        option.setCoorType("bd09ll");//可选，设置返回经纬度坐标类型，默认gcj02
        option.setScanSpan(1000);//可选，设置发起定位请求的间隔，int类型，单位ms
        option.setOpenGps(true);//可选，设置是否使用gps，默认false
        option.setLocationNotify(true);//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setIgnoreKillProcess(false);//可选，定位SDK内部是一个service，并放到了独立进程。
        option.SetIgnoreCacheException(false);//可选，设置是否收集Crash信息，默认收集，即参数为false
        option.setWifiCacheTimeOut(5*60*1000);//如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
        option.setEnableSimulateGps(false);//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        mLocationClient.setLocOption(option);
    }

    public Handler getmHandler(){
        return mHandler;
    }

    public void getLocation(){
        Log.d("location","in getlocation");
        mLocationClient = new LocationClient(getApplicationContext());
        myListener = new MyLocationListener(this);
        mLocationClient.registerLocationListener(myListener);
        initLocationInfo();
        mLocationClient.start();
    }

    //屏幕截图
    public void getScreenShot(){
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();
        if (bmp != null){
            try {
                Log.d("share","test");
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                String filePath = sdCardPath + File.separator + "screenshot.png";

                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                Toast.makeText(this, "截图已保存", Toast.LENGTH_SHORT).show();
                os.flush();
                os.close();
            } catch (Exception e) {

            }
        }
    }
}
