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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.Serializable;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.ToastUtil;
import br.com.nutrisolver.tools.UserUtil;

public class CadastrarLote extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    //private FirebaseFirestore db;
    private EditText input_nome_lote;
    private ProgressBar progressBar;
    private Lote lote;
    private String fazenda_corrente_id;
    private String fazenda_corrente_nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_lote);

        //db = FirebaseFirestore.getInstance();
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");
        fazenda_corrente_nome = sharedpreferences.getString("fazenda_corrente_nome", "-1");

        input_nome_lote = findViewById(R.id.cadastrar_nome_do_lote);
        progressBar = findViewById(R.id.progress_bar);

        configura_toolbar();
    }

    private void configura_toolbar() {
        // adiciona a barra de tarefas na tela
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
        // adiciona a seta de voltar na barra de tarefas
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Fazenda: " + fazenda_corrente_nome);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }
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
        if (!validaDados()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String nome_lote = input_nome_lote.getText().toString();

        lote = new Lote();
        lote.setNome(nome_lote);
        lote.setFazenda_id(fazenda_corrente_id);
        lote.setDono_uid(UserUtil.getCurrentUser().getUid());

        DataBaseUtil.getInstance().insertDocument("lotes", lote.getId(), lote);

        // envia o lote para o fragment
        NovaMainActivity.sendData("LotesFragment", "adiciona_lote", lote);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finaliza_cadastro();
            }
        }, 800);


    }

    private void finaliza_cadastro() {
        ToastUtil.show(getApplicationContext(), "Lote cadastrado com sucesso!", Toast.LENGTH_SHORT);
        progressBar.setVisibility(View.GONE);

        Intent it = new Intent();
        it.putExtra("lote_cadastrado", lote);
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
