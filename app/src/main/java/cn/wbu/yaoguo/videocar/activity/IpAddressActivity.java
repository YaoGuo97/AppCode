package cn.wbu.yaoguo.videocar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.wbu.yaoguo.videocar.R;

public class IpAddressActivity extends AppCompatActivity {

    private static final String TAG = IpAddressActivity.class.getSimpleName();

    private EditText ipEt;
    private Button saveBtn;
    private Button resetBtn;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipaddress);

        initView();
        setListener();
    }

    private void setListener() {
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = ipEt.getText().toString();

                if (!checkIpAddress(ipAddress)) {
                    Toast.makeText(getApplicationContext(), "请输入正确的 IP 地址", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d(TAG, "IP 地址是[" + ipAddress + "]");

                Intent intent = new Intent();
                intent.putExtra("ipAddress", ipAddress);

                setResult(MainActivity.IP_ADDRESS_RESULT_CODE, intent);

                finish();
            }

        }); // saveBtn.setOnClickListener

        // 返回按钮
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 重置按钮
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("ipAddress", "192.168.1.1");
                intent.putExtra("port", 8080);

                setResult(MainActivity.IP_ADDRESS_RESULT_CODE, intent);
                Toast.makeText(getApplicationContext(), "重置设置成功", Toast.LENGTH_LONG).show();
                finish();
            }
        }); // resetBtn.setOnClickListener

    } // void setListener()

    /**
     * 判断 IP 是否合法
     */
    public static boolean checkIpAddress(String ipAddress) {
        if (ipAddress != null && !ipAddress.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            return ipAddress.matches(regex);
        }
        return false;
    } // boolean checkIpAddress(String ipAddress)

    /**
     * 初始化视图
     */
    private void initView() {
        ipEt = findViewById(R.id.ip_et);
        backBtn = findViewById(R.id.btn_back);
        saveBtn = findViewById(R.id.btn_save);
        resetBtn = findViewById(R.id.btn_reset);
    } // void initView()

} // class IpAddressActivity
