package com.corporation8793.bluetooth;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeviceListDialog extends Dialog {
    public RecyclerView file_list;
    public LinearLayout close_btn;
    public TextView main_title, empty_text;

    FileLoadAdapter fileLoadAdapter;
    ArrayList<String> devices;

    Context context;
    String address;
    BluetoothAdapter bluetoothAdapter;
    UUID uuid;

    Set<BluetoothDevice> bluetoothDevices;
    BluetoothSocket bluetoothSocket = null;
    ConnectedThread connectedThread;

    ProgressDialog progressDialog;

    public DeviceListDialog(@NonNull Context context, String address, BluetoothAdapter bluetoothAdapter, UUID uuid) {
        super(context);
        this.context = context;
        this.address = address;
        this.bluetoothAdapter = bluetoothAdapter;
        this.uuid = uuid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_list);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        file_list = findViewById(R.id.file_list);
        close_btn = findViewById(R.id.bottom_section);
        main_title = findViewById(R.id.main_title);
        empty_text = findViewById(R.id.empty_text);

        progressDialog = new ProgressDialog(context);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        //이미 페어링 되어있는 블루투스 기기를 탐색
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }

        bluetoothDevices = bluetoothAdapter.getBondedDevices();
        //디바이스를 선택하기 위한 대화상자 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                        //dialog.dismiss();
                        //progressDialog.show();
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

        fileLoadAdapter = new FileLoadAdapter(context, list2);

        file_list.setAdapter(fileLoadAdapter);
        file_list.setLayoutManager(new LinearLayoutManager(getContext()));

        fileLoadAdapter.setOnItemClickListener(new FileLoadAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });

        close_btn.setOnClickListener(v->{
            dismiss();
        });
    }

    // 페어링된 블루투스 기기 연결
    public void setConnectDevice(String address) {
        //progressDialog.show();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        Log.e("testtestt", device + "");

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();
        } catch (Exception e) {
            Log.e("testtest", "connection failed! : " + e + "");
            e.printStackTrace();

        }
        //progressDialog.dismiss();
    }
}
