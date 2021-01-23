package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;

import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AdvertiseActivity extends AppCompatActivity {
    private Button btn_Advertise;
    private TextView send_Data;
    private TextView connnectStatus;
    private EditText txt_wifiName;
    private EditText txt_wifiPassword;

    private String wifiName;
    private String wifiPassword;
    private BluetoothLeAdvertiser mBleAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothAdapter mBluetoothAdapter;
    private final  String TAG = "INFO--";
    String SERVICE_HEART_RATE = "0000180D-0000-1000-8000-00805F9B34FB";
    String CHAR_BODY_SENSOR_LOCATION_READ = "00002A38-0000-1000-8000-00805F9B34FB";
    String CHAR_GET_VALUE = "00002A33-0000-1000-8000-00805F9B34FB";
    String CHAR_SENSOR_LOCATION_READ = "00002A38-0000-1000-8000-00805F9B3400";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);

        //get components from layout
        btn_Advertise = (Button) findViewById(R.id.btn_Advertise);
        send_Data = (TextView) findViewById(R.id.send_result);
        connnectStatus = (TextView) findViewById(R.id.connectStatus);

        txt_wifiPassword = (EditText) findViewById(R.id.editTextWifiPassword);
        txt_wifiName = (EditText) findViewById(R.id.editTextWifiName);

        //check phone to support peripheral mode
        if(!isSupportPeripheral()){
            Toast.makeText(this, "Device dose't support Peripheral Mode.", Toast.LENGTH_SHORT).show();
            return;
        }

        //get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.setName("zen");

        //set btn on click action
        btn_Advertise.setOnClickListener(btn_Advertise_Listener);
    }

    /**
     * check the device support peripheral mode*/
    public boolean isSupportPeripheral(){
        if(!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            return  false;
        }
        return true;
    }

    /**
     * prepare to advertise*/
    Button.OnClickListener btn_Advertise_Listener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String strData;
            String wifiName = txt_wifiName.getText().toString();
            String wifipassword = txt_wifiPassword.getText().toString();

            System.out.println((wifiName!=null  && wifipassword!=null));
            Log.i(TAG, "onClick: 名字："+wifiName.length()+"密码："+wifipassword.length()+"");
            if (wifiName.length()>1  && wifipassword.length()>0){
                //start to advertise
                Log.i(TAG, "onClick: kkkk");
                strData = startAdvertise(wifiName,wifipassword);
                Log.i(TAG, "onClick: ---"+strData);

                send_Data.setText(strData);
                btn_Advertise.setEnabled(false);
                btn_Advertise.setBackgroundColor(3);
            }else {
                send_Data.setText("wifi 名字或密码为空！请检查重输入");
            }



        }
    };

    /**
     * start to advertise*/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String startAdvertise(String wifiName,String wifiPassword){
        String strData = "TestData";

        //get advertiser
        mBleAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        //set gatt service
        setService(wifiName,wifiPassword);

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
//                .setTimeout(100000)
                .build();

        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(SERVICE_HEART_RATE));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, strData.getBytes(Charset.forName("UTF-8")))
                .build();

        mBleAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

        return data.toString();
    }

    /**
     * stop to advertise*/
    public void stopAdvertise() {
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBleAdvertiser = null;
        }
    }

    /**
     *set service of the device*/
    public void setService(String n,String p){
        //get Gatt Server
        Log.i(TAG, "setService: 开始GattServerCallback");
        mBluetoothGattServer = ((BluetoothManager)getSystemService(this.BLUETOOTH_SERVICE))
                .openGattServer(this, mBluetoothGattServerCallback);

        //char's setting
//        BluetoothGattCharacteristic char_BodySensorLocation = new BluetoothGattCharacteristic(
//                UUID.fromString(CHAR_BODY_SENSOR_LOCATION_READ),
//                BluetoothGattCharacteristic.PROPERTY_READ,
//                BluetoothGattCharacteristic.PERMISSION_READ);
//
//        char_BodySensorLocation.setValue("HELLOHELLO");
//
//        BluetoothGattService  service_Heart_rate = new BluetoothGattService(
//                UUID.fromString(SERVICE_HEART_RATE),
//                BluetoothGattService.SERVICE_TYPE_PRIMARY);
//
//        service_Heart_rate.addCharacteristic(char_BodySensorLocation);
//        mBluetoothGattServer.addService(service_Heart_rate);
//        Toast.makeText(getApplicationContext(),"setService success", Toast.LENGTH_LONG).show();

/////////////////////////////////////////////////////////////////////////////////////////////////////
        BluetoothGattService  service_HeartRate = new BluetoothGattService(
                UUID.fromString(SERVICE_HEART_RATE),//"0000180D-0000-1000-8000-00805F9B34FB";
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic char_BodySensorLocation = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_BODY_SENSOR_LOCATION_READ),//"00002A38-0000-1000-8000-00805F9B34FB";
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic char_SensorLocation = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_SENSOR_LOCATION_READ),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic char_GetValue = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_GET_VALUE),// "00002A33-0000-1000-8000-00805F9B34FB";
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        Log.i(TAG, "setService: 设置蓝牙值;bytespace");
//        char_BodySensorLocation.setValue("Xiaomi_C5A8");
//        char_SensorLocation.setValue("jc123456");
        char_BodySensorLocation.setValue(n);
        char_SensorLocation.setValue(p);

        service_HeartRate.addCharacteristic(char_BodySensorLocation);
        service_HeartRate.addCharacteristic(char_SensorLocation);

        char_GetValue.setValue("99999");
        service_HeartRate.addCharacteristic(char_GetValue);
        mBluetoothGattServer.addService(service_HeartRate);
    }

    AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
          public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "AdvertiseCallback Success");
            Toast.makeText(getApplicationContext(), "Advertise success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            String strError = "";
            Log.i(TAG, "onStartFailure: AdvertiseCallback 错误码:"+errorCode);
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR){
                strError = "ADVERTISE_FAILED_INTERNAL_ERROR";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED){
                strError = "ADVERTISE_FAILED_ALREADY_STARTED";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE){
                strError = "ADVERTISE_FAILED_DATA_TOO_LARGE";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED){
                strError = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
            }
            if(errorCode == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS){
                strError = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
            }
            Log.d(TAG, "Advertising onStartFailure: " + errorCode + "-" + strError);
            Toast.makeText(getApplicationContext(), "Advertising onStartFailure: " + errorCode + "-" + strError, Toast.LENGTH_SHORT).show();
        }
    };

    BluetoothGattServerCallback mBluetoothGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG, "mBluetoothGattServerCallback onConnectionStateChange: 蓝牙连接状态改变");
            if(status == BluetoothGatt.GATT_SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    Log.d(TAG, "servercallback 已连接： "+ device.getAddress());
//                    Toast.makeText(getApplicationContext(), "已连接", Toast.LENGTH_SHORT).show();
                    connnectStatus.setText("已连接");

                }else{
                    Log.d(TAG, "servercallback 未来接: "+ newState);
                    connnectStatus.setText("未连接");

                }
            }else{
                Log.d(TAG, "servercallback BluetoothGatt status: "+ status + "newState: " + newState);
            }

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(TAG, "Our gatt server service was added.");
            Log.d(TAG, service.getUuid().toString());


        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Our gatt characteristic was read.");
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }
    };

}
