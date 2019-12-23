package cn.wbu.yaoguo.videocar.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.EventListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.wbu.yaoguo.videocar.R;
import cn.wbu.yaoguo.videocar.constant.Instruction;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 服务端信息
     */
    // IP 地址返回码
    public static final int IP_ADDRESS_RESULT_CODE = 1;
    // 协议
    public static final String PROTOCOL = "http://";
    // 视频服务端端口
    public static final int VIDEO_PORT = 8080;
    // 控制服务端端口
    public static final int CONTROL_PORT = 8081;
    // 视频资源地址
    public static final String RESOURCE_PATH = "/javascript_simple.html";
    // 拍照
    public static final String IMAGE_SRC = "/?action=snapshot";

    // 轮胎最大速度
    public static final int WHEEL_MAX_SPEED = 600;
    // 轮胎最小速度
    public static final int WHEEL_MIN_SPEED = -600;
    // 轮胎加减速步长
    public static final int WHEEL_SPEED_STEP_VALUE = 5;

    /**
     * 消息
     */
    // 连接小车成功
    private static final int CONNECTED_SERVER_SUCCESS = 1;
    // 连接小车失败
    private static final int CONNECTED_SERVER_FAIL = 2;
    // 断开连接
    private static final int DISCONNECTED_TO_SERVER = 3;
    // 拍照成功
    private static final int SNAPSHOT_SUCCESS = 4;

    // 控制套接字
    private Socket socket;
    // 服务端输出流
    private OutputStream serverOut;
    // 服务端输入流
    private InputStream serverIn;

    // 视频地址
    private String videoUrl;
    // 拍照图片地址
    private String imgUrl;
    // 控制服务端 IP
    private String controlIpAddress;
    // 视频控件
    private WebView webView;

    // 所有控制按钮
    private ImageButton forwardBtn; // 小车前进
    private ImageButton backBtn; // 小车后退
    private ImageButton turnLeftBtn; // 小车左转
    private ImageButton turnRightBtn; // 小车右转
    private ImageButton cameraToUpBtn; // 舵机云台 上
    private ImageButton cameraToDownBtn; // 舵机云台 下
    private ImageButton cameraToLeftBtn; // 舵机云台 左
    private ImageButton cameraToRightBtn; // 舵机云台 右
    private ImageButton leftEmUpBtn;
    private ImageButton leftEmDownBtn;
    private ImageButton rightEmUpBtn;
    private ImageButton rightEmDownBtn;

    private ImageButton reconnectBtn; // 重新连接
    private ImageButton settingsBtn; // 设置
    private ImageButton snapshotBtn; // 拍照
    private Switch modelChangeBtn; // 模式切换

    // 信息
    private TextView recvMsgTv; // 回显信息
    private TextView debugInfoTv; // 调试信息
    private TextView modelTv; // 模式信息

    // 状态
    private String direction = "静止";
    private int cameraH = 0; // 舵机云台 水平
    private int cameraV = 0; // 舵机云台 垂直
    private int leftWheel = 0; // 电机 左
    private int rightWheel = 0; // 电机 右
    private int leftSpeed = 200;
    private int rightSpeed = 200;

    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("hello" + Environment.getExternalStorageDirectory().getAbsoluteFile().toString());

        initView(); // 初始化视图
        setListener(); // 设置监听
        initWebView(); // 初始化 WebView
        connectTCP(); // 建立 TCP 连接
        initOther(); // 初始化其他组件
    } // void onCreate()

    /**
     * 初始化视图
     */
    private void initView() {
        forwardBtn = findViewById(R.id.btn_forward);
        backBtn = findViewById(R.id.btn_back);
        turnLeftBtn = findViewById(R.id.btn_turn_left);
        turnRightBtn = findViewById(R.id.btn_turn_right);
        cameraToUpBtn = findViewById(R.id.camera_to_up);
        cameraToDownBtn = findViewById(R.id.camera_to_down);
        cameraToLeftBtn = findViewById(R.id.camera_to_left);
        cameraToRightBtn = findViewById(R.id.camera_to_right);

        leftEmUpBtn = findViewById(R.id.left_em_up);
        leftEmDownBtn = findViewById(R.id.left_em_down);
        rightEmUpBtn = findViewById(R.id.right_em_up);
        rightEmDownBtn = findViewById(R.id.right_em_down);

        reconnectBtn = findViewById(R.id.reconnect_btn);
        settingsBtn = findViewById(R.id.settings_btn);
        snapshotBtn = findViewById(R.id.snapshot_btn);
        modelChangeBtn = findViewById(R.id.model_change_btn);

        recvMsgTv = findViewById(R.id.recv_msg_tv);
        debugInfoTv = findViewById(R.id.debug_info_tv);
        modelTv = findViewById(R.id.model_tv);

        webView = findViewById(R.id.web_view);

        updateInfo();
    }

    /**
     * 设置监听
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        CarControlListener carControlListener = new CarControlListener();
        forwardBtn.setOnTouchListener(carControlListener);
        backBtn.setOnTouchListener(carControlListener);
        turnLeftBtn.setOnTouchListener(carControlListener);
        turnRightBtn.setOnTouchListener(carControlListener);

        CameraControlListener cameraControlListener = new CameraControlListener();
        cameraToUpBtn.setOnTouchListener(cameraControlListener);
        cameraToDownBtn.setOnTouchListener(cameraControlListener);
        cameraToLeftBtn.setOnTouchListener(cameraControlListener);
        cameraToRightBtn.setOnTouchListener(cameraControlListener);

        leftEmUpBtn.setOnClickListener(this);
        leftEmDownBtn.setOnClickListener(this);
        rightEmUpBtn.setOnClickListener(this);
        rightEmDownBtn.setOnClickListener(this);

        reconnectBtn.setOnClickListener((v) -> {
            // 断开 TCP
            disconnectTCP();
            // 连接 TCP
            connectTCP();
        });

        // 设置按钮 跳转页面 ipAddress
        settingsBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(MainActivity.this, IpAddressActivity.class);
            startActivityForResult(intent, IP_ADDRESS_RESULT_CODE);
        });

        // 拍照按钮
        snapshotBtn.setOnClickListener((v) -> new Thread(() -> {
            File rootFile = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File imgDir = new File(rootFile, "Pictures");
            File imgFile = new File(imgDir, "car-img-" + System.currentTimeMillis() + ".jpg");

            Log.d(TAG, "setListener: hello: " + imgDir);
            Log.d(TAG, "setListener: hello: " + imgFile);

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                URL url = new URL(imgUrl);
                inputStream = url.openStream();
                outputStream = new FileOutputStream(imgFile);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                mainHandler.sendEmptyMessage(SNAPSHOT_SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imgFile)));
        }).start());

        // 模式切换
        modelChangeBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sendControlMessage(Instruction.MODE_AUTO);
                modelTv.setText("避障模式");
            } else {
                sendControlMessage(Instruction.MODE_CONTROL);
                modelTv.setText("遥控模式");
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        SharedPreferences sp = getSharedPreferences("user_data", MODE_PRIVATE);
        videoUrl = sp.getString("videoUrl", null);
        imgUrl = sp.getString("imgUrl", null);

        Log.d(TAG, "视频 URL 是[" + videoUrl + "]");
        Log.d(TAG, "图片 URL 是[" + imgUrl + "]");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        if (videoUrl != null) {
            webView.loadUrl(videoUrl);
        }

        controlIpAddress = sp.getString("controlIpAddress", null);

        if (videoUrl == null && imgUrl == null) {

        }
    }

    private void initOther() {

        // boolean handleMessage()
        mainHandler = new Handler(msg -> {
            switch (msg.what) {
                case CONNECTED_SERVER_SUCCESS: // 连接成功
                    Toast.makeText(MainActivity.this, "小车连接成功", Toast.LENGTH_LONG).show();
                    break;
                case CONNECTED_SERVER_FAIL: // 连接失败
                    Exception e = (Exception) msg.obj;
                    Toast.makeText(MainActivity.this, "小车连接失败\n错误信息: " + (e == null ? null : e.getMessage()), Toast.LENGTH_LONG).show();
                    break;
                case SNAPSHOT_SUCCESS:
                    Toast.makeText(this, "已保存至图库", Toast.LENGTH_SHORT).show();
                    break;
                case DISCONNECTED_TO_SERVER:
                    Toast.makeText(this, "已断开连接", Toast.LENGTH_SHORT).show();
                    break;
            } // switch
            return false;
        }); // mainHandler

    } // void initOther()

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_em_up: // 左侧电机增加
                if (leftWheel < WHEEL_MAX_SPEED) {
                    leftWheel += WHEEL_SPEED_STEP_VALUE;
                    leftSpeed += WHEEL_SPEED_STEP_VALUE;
                    sendControlMessage(Instruction.EM_LEFT_UP);
                }
                break;
            case R.id.left_em_down: // 左侧电机减少
                if (leftWheel > WHEEL_MIN_SPEED) {
                    leftWheel -= WHEEL_SPEED_STEP_VALUE;
                    leftSpeed -= WHEEL_SPEED_STEP_VALUE;
                    sendControlMessage(Instruction.EM_LEFT_DOWN);
                }
                break;
            case R.id.right_em_up: // 右侧电机增加
                if (rightWheel < WHEEL_MAX_SPEED) {
                    rightWheel += WHEEL_SPEED_STEP_VALUE;
                    rightSpeed += WHEEL_SPEED_STEP_VALUE;
                    sendControlMessage(Instruction.EM_RIGHT_UP);
                }
                break;
            case R.id.right_em_down: // 右侧电机减少
                if (rightWheel > WHEEL_MIN_SPEED) {
                    rightWheel -= WHEEL_SPEED_STEP_VALUE;
                    rightSpeed -= WHEEL_SPEED_STEP_VALUE;
                    sendControlMessage(Instruction.EM_RIGHT_DOWN);
                }
                break;
        } // switch (v.getId())
        updateInfo();

    } // void onClick(View v)

    /**
     * 更新界面信息
     */
    private void updateInfo() {
        debugInfoTv.setText(
                new StringBuffer()
                        .append("左轮速度: ").append(rightSpeed)
                        .append("\n右轮速度: ").append(leftSpeed)
                        .append("\n调试信息:\n")
                        .append("控制方向: ").append(direction).append('\n')
                        .append("水平舵机: ").append(cameraH).append('\n')
                        .append("垂直舵机: ").append(cameraV).append('\n')
                        .append("左侧电机: ").append(leftWheel).append('\n')
                        .append("右侧电机: ").append(rightWheel)
        );
    }

    /**
     * 从设置 IP 界面返回时调用
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case IP_ADDRESS_RESULT_CODE:
                if (data != null) {
                    String ipAddress = data.getStringExtra("ipAddress");

                    videoUrl = PROTOCOL + ipAddress + ':' + VIDEO_PORT + RESOURCE_PATH;
                    Log.d(TAG, "视频 URL 是[" + videoUrl + ']');
                    webView.loadUrl(videoUrl);
                    Log.d(TAG, "视频画面加载完毕");

                    imgUrl = PROTOCOL + ipAddress + ':' + VIDEO_PORT + IMAGE_SRC;
                    Log.d(TAG, "图片 URL 是[" + imgUrl + ']');

                    controlIpAddress = ipAddress;
                    Log.d(TAG, "控制 IP 是[" + controlIpAddress + ']');

                    // 持久化 视频地址 和 控制地址
                    SharedPreferences sp = getSharedPreferences("user_data", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("videoUrl", videoUrl);
                    editor.putString("imgUrl", imgUrl);
                    editor.putString("controlIpAddress", controlIpAddress);
                    editor.apply();
                }
                break;
        }
    } // void onActivityResult

    /**
     * 建立 TCP 连接
     */
    private void connectTCP() {
        if (controlIpAddress != null && !controlIpAddress.isEmpty()) {
            new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(controlIpAddress, CONTROL_PORT), 5000);
                    serverOut = socket.getOutputStream();
                    serverIn = socket.getInputStream();
                } catch (IOException e) {
                    Message msg = new Message();
                    msg.what = CONNECTED_SERVER_FAIL;
                    msg.obj = e;
                    mainHandler.sendMessage(msg);
                }
                if (socket != null && socket.isConnected()) {
                    mainHandler.sendEmptyMessage(CONNECTED_SERVER_SUCCESS);
                }
            }).start();
        } else {
            Toast.makeText(this, "还未设置服务端 IP 地址\n请点击设置按钮前往设置", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 断开 TCP 连接
     */
    private void disconnectTCP() {
        if (socket != null) {
            sendControlMessage("disconnect");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (serverOut != null) {
                serverOut.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (serverIn != null) {
                serverIn.close();
            }
        } catch (IOException ignored) {
        }
    } // void disconnectTCP()

    /**
     * 发送消息至服务端
     *
     * @param message 消息
     */
    private void sendControlMessage(final String message) {
        if (serverOut != null) {
            new Thread(() -> {
                try {
                    serverOut.write(message.getBytes());
                    serverOut.flush();
                } catch (IOException e) {
                    mainHandler.sendEmptyMessage(DISCONNECTED_TO_SERVER);
                }
            }).start();
        }
        if (serverIn != null) {
            new Thread(() -> {
                int len;
                byte[] buffer = new byte[512];
                try {
                    while ((len = serverIn.read(buffer)) != -1) {
                        final String msg = new String(buffer, 0, len);
                        mainHandler.post(() -> recvMsgTv.setText(msg));
                    }
                } catch (IOException ignored) {
                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 断开 TCP 连接
        disconnectTCP();
    }

    //动态申请权限
    private void getPrimission() {
        PackageManager pm = getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.CAMERA", "com.zhengyuan.emcarsend"));
        if (permission) {
            //"有这个权限"
        } else {
            //"没有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 15);
            }
        }
        permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", "com.zhengyuan.emcarsend");
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 15);
            }
        }
        permission = PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", "com.zhengyuan.emcarsend");
        if (permission) {
            //"有这个权限"
            //Toast.makeText(Carout.this, "有权限", Toast.LENGTH_SHORT).show();
        } else {
            //"木有这个权限"
            //如果android版本大于等于6.0，权限需要动态申请
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 15);
            }
        }
    }

    /**
     * 车辆控制按钮的触摸监听
     */
    private class CarControlListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (v.getId()) {
                        case R.id.btn_forward:
                            sendControlMessage(Instruction.CAR_FORWARD);
                            direction = "前进";
                            break;
                        case R.id.btn_back:
                            sendControlMessage(Instruction.CAR_BACK);
                            direction = "后退";
                            break;
                        case R.id.btn_turn_left:
                            sendControlMessage(Instruction.CAR_TURN_LEFT);
                            direction = "左转";
                            break;
                        case R.id.btn_turn_right:
                            sendControlMessage(Instruction.CAR_TURN_RIGHT);
                            direction = "右转";
                            break;
                    }
                    updateInfo();
                    Log.d(TAG, "CarControlListener: onTouch: 按下");
                    break;
                case MotionEvent.ACTION_UP:
                    sendControlMessage(Instruction.KEY_UP);
                    direction = "静止";
                    updateInfo();
                    Log.d(TAG, "CarControlListener: onTouch: 松开");
                    break;
            } // switch (event.getAction())

            return false;

        } // boolean onTouch(View v, MotionEvent event)

    } // class CarControlListener

    /**
     * 摄像头云台控制的触摸监听
     */
    private class CameraControlListener implements View.OnTouchListener {

        // 摄像机云台旋转步长
        private static final int CAMERA_ROTATION_STEP_VALUE = 1;
        // 摄像机云台旋转最小值
        private static final int CAMERA_ROTATION_MIN_VALUE = -100;
        // 摄像机云台旋转最大值
        private static final int CAMERA_ROTATION_MAX_VALUE = 100;
        // 消息周期
        private static final long MESSAGE_SEND_DELAY_TIME = 200;

        // boolean handleMessage(@NonNull Message msg)
        private Handler handler = new Handler(msg -> {
            switch (msg.what) {
                case R.id.camera_to_up:
                    if (cameraV < CAMERA_ROTATION_MAX_VALUE) {
                        cameraV += CAMERA_ROTATION_STEP_VALUE;
                    }
                    sendControlMessage("camera_to_up");
                    break;
                case R.id.camera_to_right:
                    if (cameraH < CAMERA_ROTATION_MAX_VALUE) {
                        cameraH += CAMERA_ROTATION_STEP_VALUE;
                    }
                    sendControlMessage("camera_to_right");
                    break;
                case R.id.camera_to_down:
                    if (cameraV > CAMERA_ROTATION_MIN_VALUE) {
                        cameraV -= CAMERA_ROTATION_STEP_VALUE;
                    }
                    sendControlMessage("camera_to_down");
                    break;
                case R.id.camera_to_left:
                    if (cameraH > CAMERA_ROTATION_MIN_VALUE) {
                        cameraH -= CAMERA_ROTATION_STEP_VALUE;
                    }
                    sendControlMessage("camera_to_left");
                    break;
            } // switch (msg.what)
            updateInfo();
            return false;
        }); // private Handler handler

        private ScheduledExecutorService scheduledExecutor;

        /**
         * 按下按钮时执行
         *
         * @param viewId 按钮的 ID
         */
        private void startSendMsg(final int viewId) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutor.scheduleWithFixedDelay(() -> {
                Message msg = new Message();
                msg.what = viewId;
                handler.sendMessage(msg);
                Log.d(TAG, "CameraControlListener: startSendMsg: 发送消息");
            }, 0, MESSAGE_SEND_DELAY_TIME, TimeUnit.MILLISECONDS);
        }

        /**
         * 松开按钮时执行
         */
        private void stopSendMsg() {
            Log.d(TAG, "CameraControlListener: stopSendMsg: 停止发送消息");
            if (scheduledExecutor != null) {
                scheduledExecutor.shutdownNow();
                scheduledExecutor = null;
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // 按下
                    switch (v.getId()) {
                        case R.id.camera_to_up:
                            if (cameraV < CAMERA_ROTATION_MAX_VALUE) {
                                cameraV += CAMERA_ROTATION_STEP_VALUE;
                                sendControlMessage(Instruction.SE_UP);
                            }
                            break;
                        case R.id.camera_to_right:
                            if (cameraH < CAMERA_ROTATION_MAX_VALUE) {
                                cameraH += CAMERA_ROTATION_STEP_VALUE;
                                sendControlMessage(Instruction.SE_RIGHT);
                            }
                            break;
                        case R.id.camera_to_down:
                            if (cameraV > CAMERA_ROTATION_MIN_VALUE) {
                                cameraV -= CAMERA_ROTATION_STEP_VALUE;
                                sendControlMessage(Instruction.SE_DOWN);
                            }
                            break;
                        case R.id.camera_to_left:
                            if (cameraH > CAMERA_ROTATION_MIN_VALUE) {
                                cameraH -= CAMERA_ROTATION_STEP_VALUE;
                                sendControlMessage(Instruction.SE_LEFT);
                            }
                            break;
                    } // switch (msg.what)
                    updateInfo();
                    Log.d(TAG, "CameraControlListener: onTouch: 按下");
                    break;
                case MotionEvent.ACTION_UP: // 松开
                    sendControlMessage(Instruction.SE_KEY_UP);
                    Log.d(TAG, "CameraControlListener: onTouch: 松开");
                    break;
            } // switch (event.getAction())
            return false;

        } // boolean onTouch(View v, MotionEvent event)

    } // class CameraControlListener

} // class MainActivity
