package br.com.nutrisolver.activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.ToastUtil;

public class ListaDispositivosBT extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private ListView listView_bt_devices;
    private ListView listView_new_devices;
    private ArrayAdapter<String> pairedDevicesArrayAdapter ;
    private List<String> mac_address_paired_list;
    private List<String> mac_address_new_list;
    private ArrayAdapter<String> NewDevicesArrayAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_dispositivos_bt);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        progressBar = findViewById(R.id.progress_bar);

        configura_listView();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter2);

        configura_toolbar();

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
    }

    private void doDiscovery() {

        // Indicate scanning in the title
        //setProgressBarIndeterminateVisibility(true);
        //setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        progressBar.setVisibility(View.VISIBLE);
        bluetoothAdapter.startDiscovery();
    }


    @Override
    protected void onStart() {
        super.onStart();

        check_bt_state();

        pairedDevicesArrayAdapter.clear();
        mac_address_paired_list = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName());
                mac_address_paired_list.add(device.getAddress());
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
        NewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView_new_devices = (ListView) findViewById(R.id.listView_bt_new_devices);
        listView_new_devices.setAdapter(NewDevicesArrayAdapter);
        listView_new_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String mac_address_selected = mac_address_new_list.get(position);

                Intent it = new Intent();
                it.putExtra("device_mac_address", mac_address_selected);
                setResult(RESULT_OK, it);
                finish();
            }
        });

        listView_bt_devices = (ListView) findViewById(R.id.listView_bt_devices_paireds);
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView_bt_devices.setAdapter(pairedDevicesArrayAdapter);
        listView_bt_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String mac_address_selected = mac_address_paired_list.get(i);

                Intent it = new Intent();
                it.putExtra("device_mac_address", mac_address_selected);
                setResult(RESULT_OK, it);
                finish();
            }
        });
    }

    private void configura_toolbar() {
        // adiciona a barra de tarefas na tela
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
        // adiciona a seta de voltar na barra de tarefas
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i("MY_BLUETOOTH", "receiver entrou");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    NewDevicesArrayAdapter.add(device.getName());
                    Log.i("MY_BLUETOOTH", "receiver encontrou"+ device.getName());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
                progressBar.setVisibility(View.GONE);
                setTitle("Fim da procura");
                if (NewDevicesArrayAdapter.getCount() == 0) {
                    //String noDevices = "Nenhum dispositivo encontrado"
                    //NewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }
}
