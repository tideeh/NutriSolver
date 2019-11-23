package br.com.nutrisolver.activitys;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.ToastUtil;
import br.com.nutrisolver.tools.UserUtil;

public class ExecutarTeste1 extends AppCompatActivity {
    public static final String TOAST = "toast";
    private static final int MESSAGE_READ = 0;
    private static final int MESSAGE_WRITE = 1;
    private static final int MESSAGE_TOAST = 2;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String DESLIGAR_LED = "0";
    private static String LIGAR_LED = "1";
    private static String LIGAR_BUZZER = "2";
    private static String DESLIGAR_BUZZER = "3";
    private final int CONECTAR_DISPOSITIVO_REQUEST = 1001;
    private final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter;
    TextView textView_bt_read;
    TextView textView_bt_send;
    long startTime = 0;
    Handler timerHandler = new Handler();
    private String device_mac_address;
    private BluetoothSocket bt_socket;
    private BluetoothDevice bt_device;
    private ExecutarTeste1.ConnectThread thread_para_conectar;
    private ExecutarTeste1.ConnectedThread thread_conectada;
    private Handler bluetooth_io;
    private ProgressBar progressBar;
    private String estado = "0";
    private EditText tempo_para_execucao;
    private int TEMPO_DO_TESTE = 10;
    private SharedPreferences sharedpreferences;
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            ((TextView) findViewById(R.id.teste_timer)).setText(Integer.toString(TEMPO_DO_TESTE - seconds - 1));

            timerHandler.postDelayed(this, 500);
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executar_teste1);

        progressBar = findViewById(R.id.progress_bar);
        tempo_para_execucao = findViewById(R.id.tempo_para_execucao);
        textView_bt_read = findViewById(R.id.leitura_bluetooth_debug);
        textView_bt_send = findViewById(R.id.envio_bluetooth_debug);
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetooth_io = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        String writeMessage = new String(writeBuf, 0, writeBuf.length);
                        Log.i("MY_BLUETOOTH", "write: " + writeMessage + "; nBytes: " + writeBuf.length);
                        String aux1 = "Enviado: " + writeMessage;
                        textView_bt_send.setText(aux1);
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        Log.i("MY_BLUETOOTH", "read: " + readMessage + "; nBytes: " + msg.arg1);
                        String aux2 = "Recebido: " + readMessage;
                        textView_bt_read.setText(aux2);
                        break;
                    case MESSAGE_TOAST:
                        ToastUtil.show(ExecutarTeste1.this, msg.getData().getString(TOAST), Toast.LENGTH_SHORT);
                        break;
                }

            }
        };

        configura_toolbar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        check_bt_state(); // verifica se o bluetooth existe, caso exista e esteja desligado, pede para ligar
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (thread_conectada != null)
            thread_conectada.cancel();


    }

    private void check_bt_state() {
        if (bluetoothAdapter == null) {
            ToastUtil.show(this, "Device does not support bluetooth", Toast.LENGTH_LONG);
            finish();
        } else if (bluetoothAdapter.isEnabled()) { // se esta ativado, entao verifica a conexao
            check_bt_connection();
        } else { // pede permissao para ligar o bt
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void check_bt_connection() {
        device_mac_address = sharedpreferences.getString("device_mac_address", null);
        Log.i("MY_BLUETOOTH", "check_bt: " + device_mac_address);
        if (device_mac_address == null) { // inicia a activity para conectar em um dispositovo
            startActivityForResult(new Intent(this, ListaDispositivosBT.class), CONECTAR_DISPOSITIVO_REQUEST);
        } else {

            if (bt_socket != null) {
                if (bt_socket.isConnected()) { // ja esta conectado
                    return;
                }
            }

            bt_device = bluetoothAdapter.getRemoteDevice(device_mac_address);
            thread_para_conectar = new ExecutarTeste1.ConnectThread(bt_device);
            progressBar.setVisibility(View.VISIBLE);
            thread_para_conectar.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) { // bluetooth ativado, agora verifica a conexao
                check_bt_connection();
            } else { // bluetooth nao foi ativado
                ToastUtil.show(this, "Falha ao ativar o bluetooth", Toast.LENGTH_LONG);
                finish();
            }
        }

        if (requestCode == CONECTAR_DISPOSITIVO_REQUEST) {
            if (resultCode == RESULT_OK) {
                device_mac_address = data.getStringExtra("device_mac_address");

                // salva para usar futuramente (evita ficar selecionando o dispositivo toda hora)
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("device_mac_address", device_mac_address);
                editor.apply();

                Log.i("MY_BLUETOOTH", "act result: " + device_mac_address);
            } else {
                ToastUtil.show(this, "Falha ao selecionar um dispositivo", Toast.LENGTH_LONG);
                finish();
            }

        }
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

    public void btn_ligar_teste(View view) {
        String tempo_aux = tempo_para_execucao.getText().toString();
        if (tempo_aux == null || tempo_aux.isEmpty()) {
            ToastUtil.show(this, "Digite a duração do teste!", Toast.LENGTH_SHORT);
            return;
        }

        if (bt_socket == null) {
            ToastUtil.show(this, "Dispositivo não conectado", Toast.LENGTH_SHORT);
            return;
        }
        if (!bt_socket.isConnected()) { // ja esta conectado
            ToastUtil.show(this, "Dispositivo não conectado", Toast.LENGTH_SHORT);
            return;
        }

        if(Integer.parseInt(tempo_aux) < 1){
            ToastUtil.show(this, "Digite um valor maior que 0", Toast.LENGTH_SHORT);
            return;
        }

        if (estado.equals("0")) { // ligar
            estado = alterarEstado(LIGAR_LED);
            TEMPO_DO_TESTE = Integer.parseInt(tempo_aux);
            ((Button) findViewById(R.id.btn_ligar)).setText("Teste em andamento..");

            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            ((TextView) findViewById(R.id.teste_status)).setText("Teste em andamento..");

            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finalizarTeste();
                }
            }, Integer.parseInt(tempo_aux) * 1000);

            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    desligaBuzzy();
                }
            }, (Integer.parseInt(tempo_aux) * 1000) + 500);

        }
    }

    private void desligaBuzzy() {
        estado = alterarEstado(DESLIGAR_BUZZER);
        ((Button) findViewById(R.id.btn_ligar)).setText("Executar");
        estado = alterarEstado(DESLIGAR_LED);

        ((TextView) findViewById(R.id.teste_status)).setText("Teste finalizado!");
    }

    private void finalizarTeste() {
        estado = alterarEstado(LIGAR_BUZZER);
        ((TextView) findViewById(R.id.teste_status)).setText("Finalizando..");

        timerHandler.removeCallbacks(timerRunnable);

    }

    private String alterarEstado(String novoEstado) {
        if (thread_conectada == null) {
            ToastUtil.show(this, "Dispositivo não conectado", Toast.LENGTH_SHORT);
        } else {
            thread_conectada.write(novoEstado.getBytes());
        }

        return novoEstado;
    }

    private class ConnectThread extends Thread {
        //private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            Log.i("MY_BLUETOOTH", "new thread connect");
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            } catch (IOException e) {
                Log.e("MY_BLUETOOTH", "Socket's create() method failed", e);
            }
            bt_socket = tmp;
        }

        public void run() {
            Log.i("MY_BLUETOOTH", "run thread connect");
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                ExecutarTeste1.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.status_bluetooth_debug)).setText("Status: conectando..");
                    }
                });

                bt_socket.connect();
            } catch (IOException connectException) {
                Log.e("MY_BLUETOOTH", "Unable to connect", connectException);

                // Unable to connect; close the socket and return.
                try {
                    bt_socket.close();
                } catch (IOException closeException) {
                    Log.e("MY_BLUETOOTH", "Could not close the client socket", closeException);
                }

                ExecutarTeste1.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        ToastUtil.show(ExecutarTeste1.this, "Falha ao conectar no dispositivo", Toast.LENGTH_LONG);
                        sharedpreferences.edit().remove("device_mac_address").apply();
                        startActivityForResult(new Intent(ExecutarTeste1.this, ListaDispositivosBT.class), CONECTAR_DISPOSITIVO_REQUEST);
                        ((TextView)findViewById(R.id.status_bluetooth_debug)).setText("Status: desconectado");
                        //finish();
                    }
                });

                return;
            }

            ExecutarTeste1.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.status_bluetooth_debug)).setText("Status: conectado");
                }
            });

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            thread_conectada = new ConnectedThread();
            thread_conectada.start();
            this.interrupt();
        }
    }

    private class ConnectedThread extends Thread {
        //private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread() {
            //mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = bt_socket.getInputStream();
            } catch (IOException e) {
                Log.e("MY_BLUETOOTH", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = bt_socket.getOutputStream();
            } catch (IOException e) {
                Log.e("MY_BLUETOOTH", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.i("MY_BLUETOOTH", "new thread connected");
        }

        public void run() {
            Log.i("MY_BLUETOOTH", "run thread connected");
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                //Log.i("MY_BLUETOOTH", "while thread connected");
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = bluetooth_io.obtainMessage(MESSAGE_READ, numBytes, -1, mmBuffer);
                    //Message readMsg = bluetooth_io.obtainMessage(MESSAGE_READ, 1, -1, numBytes);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.i("MY_BLUETOOTH", "Input stream was disconnected " + e);
                    try {
                        bt_socket.close();
                    } catch (IOException e2) {
                        Log.e("MY_BLUETOOTH", "Could not close the connect socket", e2);
                    }

                    ExecutarTeste1.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)findViewById(R.id.status_bluetooth_debug)).setText("Status: desconectado");
                            ToastUtil.show(ExecutarTeste1.this, "Input stream desconectado", Toast.LENGTH_LONG);
                        }
                    });


                    break;
                }
            }

            this.interrupt();
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = bluetooth_io.obtainMessage(MESSAGE_WRITE, bytes);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e("MY_BLUETOOTH", "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        bluetooth_io.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                bluetooth_io.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            Log.i("MY_BLUETOOTH", "cancel thread connected");
            try {
                bt_socket.close();
            } catch (IOException e) {
                Log.e("MY_BLUETOOTH", "Could not close the connect socket", e);
            }

            this.interrupt();
        }
    }
}