package br.com.nutrisolver.activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.ToastUtil;

public class CadastrarLote extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private EditText input_nome_lote;
    private ProgressBar progressBar;
    private Lote lote;
    private String fazenda_corrente_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_lote);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        input_nome_lote = findViewById(R.id.cadastrar_nome_do_lote);
        progressBar = findViewById(R.id.progress_bar);

        Toolbar toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
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

        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1"); // getting String
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

    public void cadastrar_lote(View view) {
        if(!validaDados()){
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String nome_lote = input_nome_lote.getText().toString();

        lote = new Lote(nome_lote, fazenda_corrente_id);

        db.collection("lotes").document(lote.getId()).set(lote);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finaliza_cadastro();
            }
        }, 800);



    }

    private void finaliza_cadastro(){
        ToastUtil.show(getApplicationContext(), "Lote cadastrado com sucesso 11!", Toast.LENGTH_SHORT);
        progressBar.setVisibility(View.GONE);

        //Intent it = new Intent(this, TelaPrincipal.class);
        //it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //it.putExtra("atualiza_lotes", true);
        //startActivity(it);

        Intent it = new Intent();
        setResult(1, it);

        finish();
    }

    private boolean validaDados() {
        boolean valido = true;

        String nome_lote = input_nome_lote.getText().toString();
        if (TextUtils.isEmpty(nome_lote)) {
            input_nome_lote.setError("Campo necessário.");
            valido = false;
        } else {
            input_nome_lote.setError(null);
        }

        return valido;
    }
}
