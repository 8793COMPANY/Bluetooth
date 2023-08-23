package com.corporation8793.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestActivity extends AppCompatActivity {

    private final static int PERMISSION_ACCESS_FINE_LOCATION = 10;
    private final static int REQUEST_ENABLE_BT = 1; // 블루투스 활성화 상태

    //UUID생성
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    TextView main_text;
    Button paired_button, search_button, send_button;
    ListView device_list, device_list2;

    ImageView bluetooth_img;
    ToggleButton lock_img, uv_light_img;
    Slider time_slider;

    BluetoothManager bluetoothManager; // 블루투스 매니저
    BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    Set<BluetoothDevice> bluetoothDevices;
    ArrayAdapter<String> arrayAdapter, arrayAdapter2;
    ArrayList<String> arrayList, arrayList2;

    BluetoothSocket bluetoothSocket = null;
    ConnectedThread connectedThread;

    BluetoothDevice bluetoothDevice;
    private OutputStream outputStream = null; //블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림

    boolean bluetoothCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        main_text = findViewById(R.id.main_text);
//        paired_button = findViewById(R.id.paired_button);
//        search_button = findViewById(R.id.search_button);
//        send_button = findViewById(R.id.send_button);
//        device_list = findViewById(R.id.device_list);
//        device_list2 = findViewById(R.id.device_list2);
        bluetooth_img = findViewById(R.id.bluetooth_img);
        lock_img = findViewById(R.id.lock_img);
        uv_light_img = findViewById(R.id.uv_light_img);
        time_slider = findViewById(R.id.time_slider);

        main_text.setTextColor(Color.parseColor("#64abe4"));
        Shader shader = new LinearGradient(0, 0, main_text.getPaint().measureText(main_text.getText().toString()), main_text.getTextSize(),
                new int[]{Color.parseColor("#64abe4"), Color.parseColor("#7b61cb")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        main_text.getPaint().setShader(shader);

        // 위치 권환 허용 코드
        String[] permission_list = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        ActivityCompat.requestPermissions(TestActivity.this, permission_list, 1);

        //권한 체크
        bluetoothCheck();

        // 블루투스 활성화 코드
        // 블루투스 어댑터를 디폴트 어댑터로 설정
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            Log.e("testtest", "Bluetooth 미지원 기기");
        } else { // 디바이스가 블루투스를 지원 할 때
            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있을 때)
                //selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    requestPermissions(
//                            new String[]{
//                                    Manifest.permission.BLUETOOTH,
//                                    Manifest.permission.BLUETOOTH_SCAN,
//                                    Manifest.permission.BLUETOOTH_ADVERTISE,
//                                    Manifest.permission.BLUETOOTH_CONNECT
//                            }
//                            , 1);
//
//                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//
//                        // 선택한 값이 onActivityResult 함수에서 콜백
//                        startActivityForResult(intent, REQUEST_ENABLE_BT);
//                    }
//                } else {
//                    requestPermissions(
//                            new String[]{
//                                    Manifest.permission.BLUETOOTH
//                            }
//                            , 1);
//                }
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }
                // 선택한 값이 onActivityResult 함수에서 콜백
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver2, filter);

//                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
//                        filter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
//                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
//                        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//                        registerReceiver(receiver, filter);

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

        uv_light_img.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // on
                    if (bluetoothCheck) {
                        if (connectedThread != null) {
                            connectedThread.write("a");
                        }
                    } else {

                    }
                } else { // off
                    if (connectedThread != null) {
                        connectedThread.write("b");
                    }
                }
            }
        });

//        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        arrayList = new ArrayList<>();
//        device_list.setAdapter(arrayAdapter);

        bluetooth_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(TestActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }
                //이미 페어링 되어있는 블루투스 기기를 탐색
                bluetoothDevices = bluetoothAdapter.getBondedDevices();
                //디바이스를 선택하기 위한 대화상자 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
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

//        arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        arrayList2 = new ArrayList<>();
//        device_list2.setAdapter(arrayAdapter2);
//
//        paired_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                arrayAdapter.clear();
//
//                if (arrayList != null || !arrayList.isEmpty()) {
//                    arrayList.clear();
//                }
//
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                }
//
//                //이미 페어링 되어있는 블루투스 기기를 탐색
//                bluetoothDevices = bluetoothAdapter.getBondedDevices();
//                //페어링 되어있는 장치가 있는 경우
//                if (bluetoothDevices.size() > 0) {
//                    //arrayList.add("페어링 된 디바이스 목록");
//                    Log.e("testtest", bluetoothDevices.size() + "");
//
//                    for (BluetoothDevice device : bluetoothDevices) {
//                        String deviceName = device.getName();
//                        String deviceHardwareAddress = device.getAddress();
//
//                        arrayAdapter.add(deviceName);
//                        arrayList.add(deviceHardwareAddress);
//                    }
//                    //arrayList.add("끝");
//                } else { //페어링 된 장치가 없는 경우
//                    Log.e("testtest", "no device");
//                }
//
//                // case 2
//                //디바이스를 선택하기 위한 대화상자 생성
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setTitle("페어링 된 블루투스 디바이스 목록");
//                //페어링 된 각각의 디바이스의 이름과 주소를 저장
//                List<String> list = new ArrayList<>();
//                //모든 디바이스의 이름을 리스트에 추가
//                for (BluetoothDevice devices : bluetoothDevices) {
//                    list.add(devices.getName());
//                }
//                list.add("취소");
//
//                //list를 Charsequence 배열로 변경
//                final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
//                list.toArray(new CharSequence[list.size()]);
//
//                //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
//                builder.setItems(charSequences, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //해당 디바이스와 연결하는 함수 호출
//                        if (!charSequences[which].toString().equals("취소")) {
//                            connectDevice(charSequences[which].toString());
//                        }
//                    }
//                });
//                //뒤로가기 버튼 누를때 창이 안닫히도록 설정
//                builder.setCancelable(false);
//                //다이얼로그 생성
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            }
//        });
//
//        search_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                }
//
//                if (bluetoothAdapter.isDiscovering()) {
//                    bluetoothAdapter.cancelDiscovery();
//                } else {
//                    if (bluetoothAdapter.isEnabled()) {
//                        bluetoothAdapter.startDiscovery();
//                        arrayAdapter2.clear();
//                        arrayList2.clear();
////                        if (arrayList != null || !arrayList.isEmpty()) {
////                            arrayList.clear();
////                        }
//                        IntentFilter filter = new IntentFilter();
//                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
//                        filter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
//                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
//                        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//                        registerReceiver(receiver, filter);
//                    } else {
//                        Log.e("testtest", "bluetooth not on");
//                    }
//                }
//            }
//        });
//
//        send_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (connectedThread != null) {
//                    connectedThread.write("a");
//                }
//            }
//        });
//
//        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.e("testtest", "ononon");
//
//                final String name = arrayAdapter.getItem(position);
//                final String address = arrayList.get(position);
//                boolean flag = true;
//
//                Log.e("testtest", name);
//                Log.e("testtest", address);
//
//                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//
//                //connectDevice(name);
//                try {
//                    bluetoothSocket = createBluetoothSocket(device);
//                    Log.e("testtest", bluetoothSocket+"");
//                } catch (IOException e) {
//                    flag = false;
//                    main_text.setText("socket creation failed!");
//                    Log.e("testtest", "1" + e);
//                    e.printStackTrace();
//                }
//
//                try {
//                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    }
//                    bluetoothSocket.connect();
//                    main_text.setText("connected to "+name);
//                } catch (IOException e) {
//                    main_text.setText("socket connect failed! " + e+"");
//                    Log.e("testtest", "2" + e);
////                    try {
////                        bluetoothSocket.close();
////                    } catch (IOException e2) {
////                        Log.e("testtest", "unable to close() socket during connection failure", e2);
////                        test_text.setText("socket close " + e+"");
////                    }
//                }
//
//                connectedThread = new ConnectedThread(bluetoothSocket);
//                //test_text.setText("connected to "+name);
//                connectedThread.start();
//            }
//        });
//
//        device_list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.e("testtest", "ononon2");
//
//                final String name = arrayAdapter2.getItem(position);
//                final String address = arrayList2.get(position);
//                //boolean flag = true;
//
//                Log.e("testtest", name);
//                Log.e("testtest", address);
//
//                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//                Log.e("testtestt", device+"");
//
//                try {
//                    Method method = device.getClass().getMethod("createBond", (Class[]) null);
//                    method.invoke(device, (Object[]) null);
////                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
////                    }
////                    //bluetoothSocket = createBluetoothSocket(device);
////                    bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
////                    bluetoothSocket.connect();
//
////                    connectedThread = new ConnectedThread(bluetoothSocket);
////                    connectedThread.start();
//                } catch (Exception e) {
//                    //flag = false;
//                    Log.e("testtest", e + "");
//                    main_text.setText("connection failed!");
//                    e.printStackTrace();
//                }
////                if (flag) {
////                    test_text.setText("connected to " + name);
////                    connectedThread = new ConnectedThread(bluetoothSocket);
////                    connectedThread.start();
////                }
//            }
//        });
    }

    public void setConnectDevice(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        Log.e("testtestt", device + "");

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            if (ActivityCompat.checkSelfPermission(TestActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }
            //bluetoothSocket = createBluetoothSocket(device);
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
        } catch (Exception e) {
            Log.e("testtest", "connection failed! : " + e + "");
            e.printStackTrace();
        }
    }

    public void connectDevice(String deviceName) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : bluetoothDevices) {
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                Log.e("testtest", "cccc:" + bluetoothDevice + "");
                break;
            }
        }

        //UUID생성
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //boolean connect_status = true;
        //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (Exception e) {
            }
            bluetoothSocket = null;
        }

        try {
            //bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            //bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            bluetoothSocket = createBluetoothSocket(bluetoothDevice);
            bluetoothSocket.connect();

            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
            //outputStream = bluetoothSocket.getOutputStream();
            //inputStream = bluetoothSocket.getInputStream();
            //receiveData();
            //Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " 연결 완료!", Toast.LENGTH_SHORT).show();

            //상단에 연결된 디바이스 이름을 출력
            //test_text.setText(bluetoothDevice.getName());
            bluetooth_img.setBackgroundResource(R.drawable.bluetooth_image_on);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("testtest", e + "");
            //Toast.makeText(getApplicationContext(), e+"", Toast.LENGTH_SHORT).show();

            //상단에 연결된 디바이스 이름을 출력
            main_text.setText(bluetoothDevice.getName() + " failed " + e + "");
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, uuid);
        } catch (Exception e) {
            Log.e("testtest", "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        return device.createRfcommSocketToServiceRecord(uuid);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                //블루투스 디바이스 검색 시작
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    main_text.setText("블루투스 검색 시작");
                    break;
                //블루투스 디바이스 찾음
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(TestActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    }

                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();

                    //Log.e("testtest", "address : " + deviceHardwareAddress);
                    if (deviceName != null) {
                        Log.e("testtest", deviceName);
                        Log.e("testtest", deviceHardwareAddress);

                        boolean check = true;

                        if (arrayList2 != null) {
                            for (String address : arrayList2) {
                                if (address.equals(deviceHardwareAddress)) {
                                    check = false;
                                }
                            }
                        }

                        if (check) {
                            arrayAdapter2.add(deviceName);
                            arrayList2.add(deviceHardwareAddress);
                            arrayAdapter2.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("testtest", "null");
                    }
                    break;
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    main_text.setText("블루투스 검색 종료");
                    break;
                //블루투스 디바이스 페어링 상태 변화
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice paired = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (paired.getBondState() == BluetoothDevice.BOND_BONDED) {
                        String pairedName = paired.getName();
                        String pairedHardwareAddress = paired.getAddress();

                        arrayAdapter.add(pairedName);
                        arrayList.add(pairedHardwareAddress);
                        arrayAdapter.notifyDataSetChanged();

                        arrayAdapter2.remove(pairedName);
                        arrayList2.remove(pairedHardwareAddress);
                        arrayAdapter2.notifyDataSetChanged();
                    }
            }
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                //Log.e("testtest", "okkk");
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                }
//
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress();
//
//                //Log.e("testtest", "address : " + deviceHardwareAddress);
//                if (deviceName != null) {
//                    Log.e("testtest", deviceName);
//                    Log.e("testtest", deviceHardwareAddress);
//
//                    arrayAdapter2.add(deviceName);
//                    arrayList2.add(deviceHardwareAddress);
//                    arrayAdapter2.notifyDataSetChanged();
//                } else {
//                    Log.e("testtest", "null");
//                }
//
////                for (int i = 0; i < arrayAdapter.getCount(); i++) {
////                    if (arrayAdapter.getItem(i) != null) {
////                        Log.e("testtest", arrayAdapter.getItem(i));
////                    } else {
////                        Log.e("testtest", "null");
////                    }
////
////                    if (arrayList.get(i).equals(deviceHardwareAddress)) {
////                        //Log.e("testtest", "on");
////                    }
////                }
//
////                if (arrayAdapter.getCount() == 0) {
////                    arrayAdapter.add(deviceName);
////                    arrayList.add(deviceHardwareAddress);
////                    arrayAdapter.notifyDataSetChanged();
////                }
//            }
        }
    };

    private final BroadcastReceiver receiver2 = new BroadcastReceiver() {
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

    private void bluetoothCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (TestActivity.this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);

                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                builder.setMessage("어플리케이션이 비콘을 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                });
                builder.show();
            }

            if (TestActivity.this.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);

                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
                builder.setMessage("어플리케이션이 블루투스를 연결 할 수 있도록 위치 정보 액세스 권한을 부여하십시오1.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 3);
                    }
                });
                builder.show();
            }

            if (TestActivity.this.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);

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

        //unregisterReceiver(receiver);
        unregisterReceiver(receiver2);
    }
}