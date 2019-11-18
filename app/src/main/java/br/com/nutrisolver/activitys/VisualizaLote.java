package br.com.nutrisolver.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.UserUtil;

public class VisualizaLote extends AppCompatActivity {
    private static final int EDITAR_DIETA_REQUEST = 1002;

    private String lote_id;
    private String lote_nome;
    private ProgressBar progressBar;
    private ListView listView_ingredientes;
    private String dieta_ativa_id;
    private List<String> ingredientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualiza_lote);
        progressBar = findViewById(R.id.progress_bar);
        listView_ingredientes = (ListView) findViewById(R.id.listView_ingredientes);

        Intent it = getIntent();
        lote_id = it.getStringExtra("lote_id");
        lote_nome = it.getStringExtra("lote_nome");

        if (lote_id == null || lote_nome == null) {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        }

        atualiza_lista_dieta();
        configura_toolbar();

    }

    private void atualiza_lista_dieta() {
        progressBar.setVisibility(View.VISIBLE);

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("dietas", new String[]{"lote_id", "ativo"}, new Object[]{lote_id, true})
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ingredientes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.i("MY_FIRESTORE", "atualiza_lista_dieta: entrou 1");
                                ingredientes = document.toObject(Dieta.class).getIngredientes();
                                dieta_ativa_id = document.toObject(Dieta.class).getId();
                                break;
                            }

                            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(VisualizaLote.this, android.R.layout.simple_list_item_1, ingredientes);
                            listView_ingredientes.setAdapter(itemsAdapter);
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

        if (requestCode == EDITAR_DIETA_REQUEST && resultCode == 1) { // foi editado uma nova dieta, atualiza a lista com os ingredientes
            Dieta d = (Dieta) data.getSerializableExtra("dieta_editada");
            ingredientes = d.getIngredientes();

            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ingredientes);
            listView_ingredientes.setAdapter(itemsAdapter);

            dieta_ativa_id = d.getId();
        }
    }
}
