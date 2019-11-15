package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.*;
import br.com.nutrisolver.tools.ToastUtil;

public class CadastrarFazenda extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText input_nome_fazenda;
    private ProgressBar progressBar;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");
    private SharedPreferences sharedpreferences;
    private Fazenda fazenda;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_fazenda);

        mAuth = FirebaseAuth.getInstance();

        // adiciona a barra de tarefas na tela
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();

        input_nome_fazenda = findViewById(R.id.cadastrar_nome_da_fazenda);
        progressBar = findViewById(R.id.progress_bar);

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            // nao esta logado!!
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    public void cadastrar_fazenda(View view) {
        if(!validaDados()){
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String nome_fazenda = input_nome_fazenda.getText().toString();

        fazenda = new Fazenda(nome_fazenda, currentUser.getUid());

        db.collection("fazendas").document(fazenda.getId()).set(fazenda);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("fazenda_corrente_id", fazenda.getId());
        editor.apply();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finaliza_cadastro();
            }
        }, 800);



    }

    private void finaliza_cadastro(){
        ToastUtil.show(getApplicationContext(), "Fazenda cadastrada com sucesso 11!", Toast.LENGTH_SHORT);
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getApplicationContext(), TelaPrincipal.class));
        finish();
    }

    private boolean validaDados() {
        boolean valido = true;

        String nome_fazenda = input_nome_fazenda.getText().toString();
        if (TextUtils.isEmpty(nome_fazenda)) {
            input_nome_fazenda.setError("Campo necessário.");
            valido = false;
        } else {
            input_nome_fazenda.setError(null);
        }

        return valido;
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
}
