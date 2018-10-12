package me.samlss.rainyview_demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import me.samlss.view.RainyView;

public class MoreActivity extends AppCompatActivity {
    private RainyView rainyView1;
    private RainyView rainyView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        rainyView1 = findViewById(R.id.fv1);
        rainyView2 = findViewById(R.id.fv2);

        rainyView1.setLeftCloudColor(Color.parseColor("#B7AC8D"));
        rainyView1.setRightCloudColor(Color.parseColor("#9b8f84"));
        rainyView1.setRainDropColor(Color.parseColor("#9aa9bb"));
        rainyView1.setRainDropMaxNumber(50);
        rainyView1.setRainDropMaxLength(50);
        rainyView1.setRainDropMinLength(20);
        rainyView1.setRainDropMaxSpeed(3);
        rainyView1.setRainDropMinSpeed(1);
        rainyView1.setRainDropSlope(-4);
        rainyView1.setRainDropCreationInterval(10);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rainyView1.start();
        rainyView2.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            rainyView1.release();
            rainyView2.release();
        }else{
            rainyView1.stop();
            rainyView2.stop();
        }
    }

    public void onStart(View view) {
        rainyView1.start();
        rainyView2.start();
    }

    public void onStop(View view) {
        rainyView1.stop();
        rainyView2.stop();
    }
}
