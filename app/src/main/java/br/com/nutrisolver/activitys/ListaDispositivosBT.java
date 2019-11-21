package br.com.nutrisolver.activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.ToastUtil;

public class ListaDispositivosBT extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private ListView listView_bt_devices;
    private ArrayAdapter<String> devicesArrayAdapter;
    private List<String> mac_address_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_dispositivos_bt);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        configura_listView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        check_bt_state();

        devicesArrayAdapter.clear();
        mac_address_list = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                devicesArrayAdapter.add(device.getName());
                mac_address_list.add(device.getAddress());
                //String deviceName = device.getName();
                //String deviceHardwareAddress = device.getAddress(); // MAC address
            }
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
    }

    private void configura_listView(){
        listView_bt_devices = (ListView) findViewById(R.id.listView_bt_devices);
        devicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView_bt_devices.setAdapter(devicesArrayAdapter);
        listView_bt_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mac_address_selected = mac_address_list.get(i);

                Intent it = new Intent();
                it.putExtra("device_mac_address", mac_address_selected);
                setResult(RESULT_OK, it);
                finish();
            }
        });
    }
}
