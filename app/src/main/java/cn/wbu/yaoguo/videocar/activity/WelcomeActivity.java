package cn.wbu.yaoguo.videocar.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import cn.wbu.yaoguo.videocar.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 前往主界面
        toMainPage();
    }

    /**
     * 前往主界面
     */
    private void toMainPage() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            // 结束欢迎页
            finish();
        }, 2500); // TODO 欢迎页展示时间
    }
}
