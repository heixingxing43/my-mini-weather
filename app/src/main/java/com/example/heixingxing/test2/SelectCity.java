package com.example.heixingxing.test2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by heixingxing on 2017/10/18.
 */

public class SelectCity extends Activity implements View.OnClickListener{
    private ImageView mBackbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        mBackbtn = (ImageView)findViewById(R.id.title_back);
        mBackbtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.title_back:
                //传递数据
                Intent i = new Intent();
                i.putExtra("cityCode","101160101");
                setResult(RESULT_OK,i);

                finish();
                //Toast.makeText(SelectCity.this,"test",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
}
