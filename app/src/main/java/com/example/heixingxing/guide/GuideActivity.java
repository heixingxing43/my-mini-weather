package com.example.heixingxing.guide;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.heixingxing.test2.MainActivity;
import com.example.heixingxing.test2.R;
import com.example.heixingxing.test2.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heixingxing on 2017/12/15.
 */

public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener{
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    //设置滑动效果小圆点
    private ImageView[] guidedots;
    private int[] guide_ids = {R.id.guidev1, R.id.guidev2, R.id.guidev3};

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //判断是否为第一次打开APP
        if(!isFirstTime()){
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            SharedPreferences.Editor editor = getSharedPreferences("config",MODE_PRIVATE).edit();
            editor.putBoolean("isfirst",false);
            editor.commit();
        }
        setContentView(R.layout.guide);
        initviews();
        initGuideDots();
        btn = (Button)views.get(2).findViewById(R.id.guidebutton);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    public void initviews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.guidepage1,null));
        views.add(inflater.inflate(R.layout.guidepage2,null));
        views.add(inflater.inflate(R.layout.guidepage3,null));
        //实例化一个vp监听器
        vpAdapter = new ViewPagerAdapter(views,this);
        //通过findViewById方法找到viewpager对象
        vp = (ViewPager)findViewById(R.id.guideviewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
    }

    private void initGuideDots(){
        guidedots = new ImageView[views.size()];
        for(int i=0; i<views.size(); i++){
            guidedots[i] = (ImageView)findViewById(guide_ids[i]);
        }
    }

    private boolean isFirstTime(){
        boolean first = getSharedPreferences("config", MODE_PRIVATE).getBoolean("isfirst",true);
        return first;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i=0; i<guide_ids.length; i++){
            if(i == position){
                guidedots[i].setImageResource(R.drawable.page_indicator_focused);
            }else{
                guidedots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
