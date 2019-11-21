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
import br.com.nutrisolver.tools.AdapterDietaAtual;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.UserUtil;

public class VisualizaLote extends AppCompatActivity {
    private static final int EDITAR_DIETA_REQUEST = 1002;

    private String lote_id;
    private String lote_nome;
    private ProgressBar progressBar;
    private ListView listView_dieta_atual;
    private String dieta_ativa_id;
    private List<String> ingredientes_nomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualiza_lote);
        progressBar = findViewById(R.id.progress_bar);

        Intent it = getIntent();
        lote_id = it.getStringExtra("lote_id");
        lote_nome = it.getStringExtra("lote_nome");

        if (lote_id == null || lote_nome == null) {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        }

        configura_listView();

        atualiza_lista_dieta();
        configura_toolbar();

    }

    private void atualiza_lista_dieta() {
        progressBar.setVisibility(View.VISIBLE);

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("dietas", new String[]{"lote_id", "ativo"}, new Object[]{lote_id, true}, 1)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ingredientes_nomes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.i("MY_FIRESTORE", "atualiza_lista_dieta: entrou 1");
                                ingredientes_nomes = document.toObject(Dieta.class).getIngredientes_nomes();
                                dieta_ativa_id = document.toObject(Dieta.class).getId();
                            }

                            AdapterDietaAtual itemsAdapter = new AdapterDietaAtual(VisualizaLote.this, ingredientes_nomes);
                            //ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(VisualizaLote.this, android.R.layout.simple_list_item_1, ingredientes);
                            listView_dieta_atual.setAdapter(itemsAdapter);
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

    public void editar_dieta(View view) {
        Intent it = new Intent(this, EditarDieta.class);
        it.putExtra("lote_id", lote_id);
        it.putExtra("lote_nome", lote_nome);
        if (dieta_ativa_id != null)
            it.putExtra("dieta_ativa_id", dieta_ativa_id);

        startActivityForResult(it, EDITAR_DIETA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDITAR_DIETA_REQUEST && resultCode == 1) { // foi editado uma nova dieta, atualiza a lista com os ingredientes_nomes
            Dieta d = (Dieta) data.getSerializableExtra("dieta_editada");
            ingredientes_nomes = d.getIngredientes_nomes();

            AdapterDietaAtual itemsAdapter = new AdapterDietaAtual(this, ingredientes_nomes);
            //ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ingredientes);
            listView_dieta_atual.setAdapter(itemsAdapter);

            dieta_ativa_id = d.getId();
        }
    }

    private void configura_listView(){
        listView_dieta_atual = (ListView) findViewById(R.id.listView_dieta_atual);
    }

    public void executar_teste(View view) {
    }
}
