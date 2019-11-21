package br.com.nutrisolver.activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.ToastUtil;

public class ExecutarTeste1 extends AppCompatActivity {
    private final int CONECTAR_DISPOSITIVO_REQUEST = 1001;
    private final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    private String device_mac_address;
    private BluetoothSocket btSocket;
    //private ExecutarTeste1.ConnectThread thread_para_conectar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executar_teste1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();

        check_bt_state(); // verifica se o bluetooth esta ativado

        //device_mac_address = getIntent().getStringExtra("device_mac_address");

        if(device_mac_address == null){ // inicia a activity para conectar em um dispositovo
            startActivityForResult(new Intent(this, ListaDispositivosBT.class), CONECTAR_DISPOSITIVO_REQUEST);
        }
        else{
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(device_mac_address);
            //thread_para_conectar = new ExecutarTeste1.ConnectThread(device);

        }
    }

    private void check_bt_state(){
        if(bluetoothAdapter==null) {
            ToastUtil.show(this, "Device does not support bluetooth", Toast.LENGTH_LONG);
            finish();
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode != RESULT_OK){ // bluetooth nao foi ativado
                ToastUtil.show(this, "Falha ao ativar o bluetooth", Toast.LENGTH_LONG);
                finish();
            }
        }

        if(requestCode == CONECTAR_DISPOSITIVO_REQUEST){
            if(resultCode == RESULT_OK){
                device_mac_address = data.getStringExtra("device_mac_address");
            }
            else{
                ToastUtil.show(this, "Falha ao selecionar um dispositivo", Toast.LENGTH_LONG);
                finish();
            }

        }
    }
/*
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

 */
}
