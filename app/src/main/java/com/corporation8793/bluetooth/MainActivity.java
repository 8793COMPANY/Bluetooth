package com.corporation8793.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.slider.Slider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSION_ACCESS_FINE_LOCATION = 10;
    private final static int REQUEST_ENABLE_BT = 1; // 블루투스 활성화 상태

    //UUID생성
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    TextView main_text;
    ImageView bluetooth_img;
    ToggleButton lock_img, uv_light_img;
    Slider time_slider;

    BluetoothManager bluetoothManager; // 블루투스 매니저
    BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    Set<BluetoothDevice> bluetoothDevices;

    BluetoothSocket bluetoothSocket = null;
    ConnectedThread connectedThread;
    boolean bluetoothCheck = false;

    private Timer timerCall;
    private int nCnt;
    TimerTask timerTask;

//    ProgressDialog progressDialog;
//    ConstraintLayout progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_text = findViewById(R.id.main_text);
        bluetooth_img = findViewById(R.id.bluetooth_img);
        lock_img = findViewById(R.id.lock_img);
        uv_light_img = findViewById(R.id.uv_light_img);
        time_slider = findViewById(R.id.time_slider);
//        progress = findViewById(R.id.progress);

        // 타이머
        nCnt = 0;

//        progressDialog = new ProgressDialog(this);
//        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 타이틀 그라데이션
        main_text.setTextColor(Color.parseColor("#64abe4"));
        Shader shader = new LinearGradient(0, 0, main_text.getPaint().measureText(main_text.getText().toString()), main_text.getTextSize(),
                new int[]{Color.parseColor("#64abe4"), Color.parseColor("#7b61cb")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        main_text.getPaint().setShader(shader);

        // 위치 권환 허용 코드
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // 권한 체크
        bluetoothCheck();

        // 블루투스 활성화 코드
        // 블루투스 어댑터를 디폴트 어댑터로 설정
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            Log.e("testtest", "Bluetooth 미지원 기기");
        } else { // 디바이스가 블루투스를 지원 할 때
            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있을 때)
            } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }
                // 선택한 값이 onActivityResult 함수에서 콜백
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }

        // 블루투스 연결 확인
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);

        // slider
        time_slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (connectedThread != null) {
                    if (timerCall != null) {
                        timerCall.cancel();
                    }
                    connectedThread.write("b");
                    uv_light_img.setChecked(false);
                }
            }
        });

        // 잠금버튼
        lock_img.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // on
                    uv_light_img.setEnabled(false);
                    time_slider.setEnabled(false);
                } else { // off
                    uv_light_img.setEnabled(true);
                    time_slider.setEnabled(true);
                }
            }
        });

        // 불빛 버튼
        uv_light_img.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // on
                    if (bluetoothCheck) {
                        float time = time_slider.getValue();
                        Log.e("testtest", time+"");

                        if ((int) time == 1) {
                            if (connectedThread != null) {
                                connectedThread.write("a");
                            }
                        } else {
                            switch ((int) time) {
                                case 4: { // 3(180)
                                    nCnt = 10;
                                    //nCnt = 180;
                                    break;
                                }
                                case 7: { // 5(300)
                                    nCnt = 300;
                                    break;
                                }
                                case 10: { // 10(600)
                                    nCnt = 600;
                                    break;
                                }
                                default:
                                    break;
                            }

                            if (timerTask != null) {
                                timerTask.cancel();
                            }

                            timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    if (connectedThread != null) {
                                        nCnt--;

                                        if (nCnt <= 0) {
                                            connectedThread.write("b");
                                            uv_light_img.setChecked(false);
                                            timerCall.cancel();
                                        } else {
                                            connectedThread.write("a");
                                            Log.e("testtest", nCnt+"");
                                        }
                                    }
                                }
                            };

                            timerCall = new Timer();
                            timerCall.schedule(timerTask, 0, 1000);
                        }
                    }
                } else { // off
                    if (connectedThread != null) {
                        connectedThread.write("b");
                    }
                }
            }
        });

        // 블루투스 버튼
        bluetooth_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }
                //이미 페어링 되어있는 블루투스 기기를 탐색
                bluetoothDevices = bluetoothAdapter.getBondedDevices();
                //디바이스를 선택하기 위한 대화상자 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("페어링 된 블루투스 디바이스 목록");
                //페어링 된 각각의 디바이스의 이름과 주소를 저장
                List<String> list = new ArrayList<>();
                List<String> list2 = new ArrayList<>();

                if (bluetoothDevices.size() > 0) {
                    //모든 디바이스의 이름을 리스트에 추가
                    for (BluetoothDevice devices : bluetoothDevices) {
                        list.add(devices.getName());
                        list2.add(devices.getAddress());
                    }
                    list.add("취소");
                } else {
                    list.add("없음");
                }

                //list1, 2를 Charsequence 배열로 변경
                final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
                list.toArray(new CharSequence[list.size()]);
                final CharSequence[] charSequences2 = list2.toArray(new CharSequence[list2.size()]);
                list2.toArray(new CharSequence[list2.size()]);

                //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
                builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //해당 디바이스와 연결하는 함수 호출
                        if (!charSequences[which].toString().equals("취소")) {
                            if (!charSequences[which].toString().equals("없음")) {
                                //connectDevice(charSequences[which].toString());
                                setConnectDevice(charSequences2[which].toString());
                            }
                        }
                    }
                });
                //뒤로가기 버튼 누를때 창이 안닫히도록 설정
                builder.setCancelable(false);
                //다이얼로그 생성
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    // 페어링된 블루투스 기기 연결
    public void setConnectDevice(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        Log.e("testtestt", device + "");

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
        } catch (Exception e) {
            Log.e("testtest", "connection failed! : " + e + "");
            e.printStackTrace();
        }
    }

    // 기기 연결 확인
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.e("testtest", "열결됨");
                bluetooth_img.setBackgroundResource(R.drawable.bluetooth_image_on);
                bluetoothCheck = true;
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.e("testtest", "열결 끊김");
                bluetooth_img.setBackgroundResource(R.drawable.bluetooth_image_off);
                bluetoothCheck = false;
            }
        }
    };

    // 블루투스 권한 확인
    private void bluetoothCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                builder.setMessage("어플리케이션이 비콘을 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                });
                builder.show();
            }

            if (MainActivity.this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                builder.setMessage("어플리케이션이 블루투스를 연결 할 수 있도록 위치 정보 액세스 권한을 부여하십시오1.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
                    }
                });
                builder.show();
            }

            if (MainActivity.this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                builder.setMessage("어플리케이션이 블루투스를 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오2.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                    }
                });
                builder.show();
            }
        }
    }

    // 블루투스 권한 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("디버깅", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("권한 제한");
                    builder.setMessage("위치 정보 및 액세스 권한이 허용되지 않았으므로 블루투스를 검색 및 연결할수 없습니다.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                break;
            }
            case 2: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("디버깅", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("권한 제한");
                    builder.setMessage("블루투스 스캔권한이 허용되지 않았습니다.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                break;
            }
            case 3: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("디버깅", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("권한 제한");
                    builder.setMessage("블루투스 연결 권한이 허용되지 않았습니다.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }
}