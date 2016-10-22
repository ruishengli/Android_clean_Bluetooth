
package com.inlook.bluetooth_sdk.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.inlook.bluetooth_sdk.ble.BleRequest.RequestType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressLint("NewApi")
public class AndroidBle implements IBle, IBleRequestHandler {

    private static final String TAG = "AndroidBle";

    private BleService mService;
    private BluetoothAdapter mBtAdapter;
    private Map<String, BluetoothGatt> mBluetoothGatts;
    private volatile boolean mOadBusy = false; // Write/read pending response

    /*private ScanCallback mtLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            mService.bleDeviceFound(result.getDevice(), result.getRssi(),
                    result.getScanRecord().getBytes(), BleService.DEVICE_SOURCE_SCAN);
        }
    };*/

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

   private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            mService.bleDeviceFound(device, rssi, scanRecord, BleService.DEVICE_SOURCE_SCAN);
        }
    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            final String address = gatt.getDevice().getAddress();
            Log.e(TAG, "onConnectionStateChange " + address + " status " + status + " newState "
                    + newState);

            //34，129
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnect(address);
                mService.bleGattDisConnected(address);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mService.bleGattConnected(gatt.getDevice());
                /*
                模拟等待discoverService时候连接断开
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disconnect(address);
                        mService.bleGattDisConnected(address);
                    }
                },150);*/
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                mService.addBleRequest(new BleRequest(RequestType.DISCOVER_SERVICE, address));

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                disconnect(address);
                mService.bleGattDisConnected(address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String address = gatt.getDevice().getAddress();
            String name = gatt.getDevice().getName();
            setOadNoBusy();

            Log.e(TAG, "onServicesDiscovered " + address + " name:" + name + " status " + status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                mService.requestProcessed(address, RequestType.DISCOVER_SERVICE, "", false,null);
                return;
            }

            mService.bleServiceDiscovered(gatt.getDevice().getAddress());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {

            super.onCharacteristicRead(gatt, characteristic, status);

            setOadNoBusy();
            String address = gatt.getDevice().getAddress();
            Log.d(TAG, "onCharacteristicRead " + address + " uuid " + characteristic.getUuid()
                    + " status " + status);


            if (status != BluetoothGatt.GATT_SUCCESS) {

               /* if(status == 133) {
                    disconnect(address);
                    mService.bleGattDisConnected(address);
                    return;
                }*/
                mService.requestProcessed(address, RequestType.READ_CHARACTERISTIC,
                        characteristic.getUuid().toString(), false,null);

                return;
            }
            // Log.d(TAG, "data " + characteristic.getStringValue(0));
            mService.bleCharacteristicRead(gatt.getDevice().getAddress(),
                    characteristic.getUuid().toString(), status, characteristic.getValue());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            super.onCharacteristicChanged(gatt, characteristic);
            setOadNoBusy();

            String address = gatt.getDevice().getAddress();
            if(characteristic.getValue() != null) {
                Log.d(TAG, "onCharacteristicChanged "
                        + address + " val:" + Arrays.toString(characteristic.getValue())
                        );
            } else {
                Log.d(TAG, "onCharacteristicChanged " + address );
            }
            mService.bleCharacteristicChanged(address, characteristic.getUuid().toString(),
                    characteristic.getValue());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

            super.onCharacteristicWrite(gatt, characteristic, status);

            setOadNoBusy();

            waitIdle();

            String address = gatt.getDevice().getAddress();
            Log.d(TAG, "onCharacteristicWrite " + address + " uuid " + characteristic.getUuid()
                    + " status " + status );
            //+ " value " + Arrays.toString(characteristic.getValue())

            if (BleService.OAD_IMAGE_IDENTIFY.equalsIgnoreCase(characteristic.getUuid().toString()) || BleService.OAD_BLOCK_REQUEST.equalsIgnoreCase(characteristic.getUuid().toString())) {
                return;
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                mService.requestProcessed(address, RequestType.WRITE_CHARACTERISTIC,
                        characteristic.getUuid().toString(), false,characteristic.getValue());
                return;
            }
            mService.bleCharacteristicWrite(gatt.getDevice().getAddress(),
                    characteristic.getUuid().toString(), status,characteristic.getValue());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {

            super.onDescriptorWrite(gatt, descriptor, status);

            setOadNoBusy();

            String address = gatt.getDevice().getAddress();
            Log.e(TAG, "onDescriptorWrite " + address + " status " + status);

            BleRequest request = mService.getCurrentRequest();
            if (request == null || request.type == null) {
                return;
            }
            if (request.type == RequestType.CHARACTERISTIC_NOTIFICATION
                    || request.type == RequestType.CHARACTERISTIC_INDICATION
                    || request.type == RequestType.CHARACTERISTIC_STOP_NOTIFICATION) {

                if (status != BluetoothGatt.GATT_SUCCESS) {
                    mService.requestProcessed(address, request.type, "",
                            false,null);
                    return;
                }
                if (request.type == RequestType.CHARACTERISTIC_NOTIFICATION) {
                    mService.bleCharacteristicNotification(address,
                            descriptor.getCharacteristic().getUuid().toString(), true, status);
                } else if (request.type == RequestType.CHARACTERISTIC_INDICATION) {
                    mService.bleCharacteristicIndication(address,
                            descriptor.getCharacteristic().getUuid().toString(), status);
                } else {
                    mService.bleCharacteristicNotification(address,
                            descriptor.getCharacteristic().getUuid().toString(), false, status);
                }
                return;
            }
        }
    };


    private void waitIdle() {
        try {
            //Log.d(TAG,"sleep in...");
            Thread.sleep(1);
            //Log.d(TAG,"sleep out...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AndroidBle(BleService service) {
        mService = service;
        if (!mService.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mService.bleNotSupported();
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) mService
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();

        if (mBtAdapter == null) {
            mService.bleNoBtAdapter();
        }
        mBluetoothGatts = new HashMap<>();
    }

    @Override
    public void startScan() {
       // mBtAdapter.getBluetoothLeScanner().startScan(mtLeScanCallback);
        mBtAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    public void startScan(UUID[] uuids) {
         mBtAdapter.startLeScan(uuids, mLeScanCallback);
    }

    @Override
    public void stopScan() {
        //mBtAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        mBtAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    public boolean adapterEnabled() {
        if (mBtAdapter != null) {
            return mBtAdapter.isEnabled();
        }
        return false;
    }

    @Override
    public boolean connect(String address) {
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        BluetoothGatt gatt = device.connectGatt(mService, false, mGattCallback);

        if (gatt == null) {
            mBluetoothGatts.remove(address);
            return false;
        } else {
            // TODO: if state is 141, it can be connected again after about 15 seconds
            Log.d(TAG, "connect and put gatt in map");
            mBluetoothGatts.put(address, gatt);
            return true;
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "...onDestroy...");
        if (mBluetoothGatts != null && !mBluetoothGatts.isEmpty()) {
            Set<Map.Entry<String, BluetoothGatt>> set = mBluetoothGatts.entrySet();
            try {
                Iterator<Map.Entry<String, BluetoothGatt>> iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, BluetoothGatt> entry = iterator.next();
                    disconnect(entry.getKey());
                }
            } catch (Exception e) {

            }
        }
    }


    @Override
    public void disconnect(String address) {
        setOadNoBusy();
        Log.d(TAG, "disconnect...");
        if (mBluetoothGatts.containsKey(address)) {
            BluetoothGatt gatt = mBluetoothGatts.remove(address);
            if (gatt != null) {
                gatt.disconnect();
                Log.d(TAG, "GATT Close...");
                gatt.close();
            }
        }

    }



    @Override
    public ArrayList<BleGattService> getServices(String address) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null) {
            return null;
        }

        ArrayList<BleGattService> list = new ArrayList<BleGattService>();
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService s : services) {
            BleGattService service = new BleGattService(s);
            list.add(service);
        }
        return list;
    }

    @Override
    public boolean requestReadCharacteristic(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        mService.addBleRequest(new BleRequest(RequestType.READ_CHARACTERISTIC,
                gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    public boolean readCharacteristic(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null) {
            return false;
        }
        return gatt.readCharacteristic(characteristic.getGattCharacteristicA());
    }

    @Override
    public boolean discoverServices(String address) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null) {
            return false;
        }

        boolean ret = gatt.discoverServices();
        if (!ret) {
            disconnect(address);
        }
        return ret;
    }

    @Override
    public BleGattService getService(String address, UUID uuid) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null) {
            Log.e(TAG, "on getService gatt is null");
            // 设备已经断开，需要发出通知
            if (mService != null) {
                mService.bleGattDisConnected(address);
            }
            return null;
        }
        BluetoothGattService service = gatt.getService(uuid);
        if (service == null) {
            Log.d(TAG, "on getService is null ,uuid:" + uuid);
            return null;
        } else {
            return new BleGattService(service);
        }
    }

    @Override
    public boolean requestCharacteristicNotification(String address,
                                                     BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        mService.addBleRequest(new BleRequest(RequestType.CHARACTERISTIC_NOTIFICATION,
                gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    @Override
    public boolean characteristicNotification(String address,
                                              BleGattCharacteristic characteristic) {
        BleRequest request = mService.getCurrentRequest();
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        boolean enable = true;
        if (request.type == RequestType.CHARACTERISTIC_STOP_NOTIFICATION) {
            enable = false;
        }

        BluetoothGattCharacteristic c = characteristic.getGattCharacteristicA();
        if (!gatt.setCharacteristicNotification(c, enable)) {
            return false;
        }

        BluetoothGattDescriptor descriptor = c.getDescriptor(BleService.DESC_CCC);
        if (descriptor == null) {
            return false;
        }

        byte[] val_set ;
       if (request.type == RequestType.CHARACTERISTIC_NOTIFICATION) {

            val_set = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

        } else if (request.type == RequestType.CHARACTERISTIC_INDICATION) {

            val_set = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else {

            val_set = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
        if (!descriptor.setValue(val_set)) {
            return false;
        }

        return gatt.writeDescriptor(descriptor);
    }

    @Override
    public boolean requestWriteCharacteristic(String address, BleGattCharacteristic characteristic,
                                              String remark) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        mService.addBleRequest(new BleRequest(RequestType.WRITE_CHARACTERISTIC,
                gatt.getDevice().getAddress(), characteristic, remark));
        return true;
    }

    @Override
    public boolean writeCharacteristic(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }
        Log.e(TAG, "on write characteristic " + characteristic.getUuid().toString() + " value:"
                + Arrays.toString(characteristic.getValue()));

        characteristic.getGattCharacteristicA().setValue(characteristic.getValue());
        return gatt.writeCharacteristic(characteristic.getGattCharacteristicA());
    }

    @Override
    public boolean requestConnect(String address) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt != null && gatt.getServices().size() == 0) {
            return false;
        }
        mService.init();
        mService.addBleRequest(new BleRequest(RequestType.CONNECT_GATT, address));
        return true;
    }

    @Override
    public String getBTAdapterMacAddr() {
        if (mBtAdapter != null) {
            return mBtAdapter.getAddress();
        }
        return null;
    }

    @Override
    public boolean requestIndication(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        mService.addBleRequest(new BleRequest(RequestType.CHARACTERISTIC_INDICATION,
                gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    @Override
    public boolean requestStopNotification(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        mService.addBleRequest(new BleRequest(RequestType.CHARACTERISTIC_NOTIFICATION,
                gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    // OAD单独使用的方法

    /**
     * get current firmware image info
     *
     * @param address
     * @param characteristic
     * @return
     */
    @Override
    public boolean setTargetImageEnableNotification(String address,
                                                    BleGattCharacteristic characteristic) {
        Log.e(TAG, "setTargetImageEnableNotification ");
        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        if (mOadBusy) {
            Log.e(TAG, "setTargetImageEnableNotification busy:" + mOadBusy);
            return false;
        }

        boolean enable = true;
        BluetoothGattCharacteristic c = characteristic.getGattCharacteristicA();
        if (!gatt.setCharacteristicNotification(c, enable)) {
            return false;
        }

        BluetoothGattDescriptor descriptor = c.getDescriptor(BleService.DESC_CCC);
        if (descriptor == null) {
            return false;
        }

        byte[] val_set = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;

        if (!descriptor.setValue(val_set)) {
            return false;
        }
        mOadBusy = true;

        return gatt.writeDescriptor(descriptor);
    }

    @Override
    public boolean writeImageBlock(String address, BleGattCharacteristic characteristic) {
        if (mOadBusy) {
            Log.e(TAG, "writeImageBlock busy:" + mOadBusy);
            return false;
        }

        BluetoothGatt gatt = mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }

        mOadBusy = true;
        boolean ret = gatt.writeCharacteristic(characteristic.getGattCharacteristic());
        if (!ret) {
            Log.e(TAG, "writeImageBlock false");
        }
        return ret;
    }


    @Override
    public boolean getOadBusy() {
        return mOadBusy;
    }

    @Override
    public void setOadNoBusy() {
        mOadBusy = false;
    }

}
