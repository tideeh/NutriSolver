package br.com.nutrisolver.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.tools.AdapterDieta;
import br.com.nutrisolver.tools.AdapterDietaAtual;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.UserUtil;

public class VisualizaLote extends AppCompatActivity {
    private static final int CADASTRAR_DIETA_REQUEST = 1001;

    private String lote_id;
    private String lote_nome;
    private ProgressBar progressBar;
    private ListView listView_dietas;
    private AdapterDieta adapterDieta = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualiza_lote);
        progressBar = findViewById(R.id.progress_bar);

        Intent it = getIntent();
        lote_id = it.getStringExtra("lote_id");
        lote_nome = it.getStringExtra("lote_nome");

        if (lote_id == null || lote_nome == null) {
            finish();
        }

        configura_listView();

        atualiza_lista_dieta();
        configura_toolbar();

        findViewById(R.id.fab_cadastrar_dieta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(VisualizaLote.this, CadastrarDieta.class);
                startActivityForResult(it, CADASTRAR_DIETA_REQUEST);
            }
        });
    }

    private void atualiza_lista_dieta() {
        progressBar.setVisibility(View.VISIBLE);

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("dietas", new String[]{"lote_id", "ativo"}, new Object[]{lote_id, true})
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            adapterDieta.clear();
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    adapterDieta.addItem(document.toObject(Dieta.class));
                                }
                            }
                        } else {
                            Log.i("MY_FIRESTORE", "atualiza_lista_dieta: " + task.getException());
                        }
                        progressBar.setVisibility(View.GONE);
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

        getSupportActionBar().setTitle("Lote: " + lote_nome);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_DIETA_REQUEST && resultCode == 1) { // foi cadastrada nova dieta, verifica se é desse lote, se for adiciona na lista
            Dieta d = (Dieta) data.getParcelableExtra("dieta_cadastrada");

            if(d.getLote_id().equals(lote_id)){
                adapterDieta.addItem(d);
            }
        }
    }

    private void configura_listView() {
        listView_dietas = (ListView) findViewById(R.id.listView_dietas);
        adapterDieta = new AdapterDieta(this);
        listView_dietas.setAdapter(adapterDieta);
    }

    public void executar_teste(View view) {
        startActivity(new Intent(this, ExecutarTeste1.class));
    }
}
