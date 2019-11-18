package br.com.nutrisolver.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.ToastUtil;
import br.com.nutrisolver.tools.UserUtil;

public class EditarDieta extends AppCompatActivity {
    public static String[] possiveis_ingredientes = new String[]{"Milho", "Farelo de Soja", "Feno"};
    Dieta dieta;
    private String lote_id;
    private String lote_nome;
    private String dieta_ativa_id;
    private ProgressBar progressBar;
    private ListView listView_editar_ingredientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dieta);

        listView_editar_ingredientes = (ListView) findViewById(R.id.listView_editar_ingredientes);
        progressBar = findViewById(R.id.progress_bar);

        Intent it = getIntent();
        lote_id = it.getStringExtra("lote_id");
        lote_nome = it.getStringExtra("lote_nome");
        dieta_ativa_id = it.getStringExtra("dieta_ativa_id");

        if (lote_id == null || lote_nome == null) {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        }

        atualiza_lista_ingredientes();
        configura_toolbar();
    }

    private void atualiza_lista_ingredientes() {
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, possiveis_ingredientes);
        listView_editar_ingredientes.setAdapter(itemsAdapter);
        listView_editar_ingredientes.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void salvar_dieta(View view) {
        if (listView_editar_ingredientes.getCheckedItemCount() == 0) {
            ToastUtil.show(this, "Selecione pelo menos um ingrediente", Toast.LENGTH_SHORT);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        dieta = new Dieta(lote_id);

        int len = listView_editar_ingredientes.getCount();
        SparseBooleanArray checked = listView_editar_ingredientes.getCheckedItemPositions();

        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                String item = possiveis_ingredientes[i];
                dieta.addIngrediente(item);
            }
        }

        // desativa a dieta anterior
        if (dieta_ativa_id != null) {
            Log.i("MY_FIRESTORE", " " + dieta_ativa_id);
            DataBaseUtil.getInstance().updateDocument("dietas", dieta_ativa_id, "ativo", false);
        }

        // salva a nova dieta
        DataBaseUtil.getInstance().insertDocument("dietas", dieta.getId(), dieta);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finaliza_cadastro();
            }
        }, 800);
    }

    private void finaliza_cadastro() {
        ToastUtil.show(this, "Dieta editada com sucesso!", Toast.LENGTH_SHORT);
        progressBar.setVisibility(View.GONE);

        Intent it = new Intent();
        it.putExtra("dieta_editada", dieta);
        setResult(1, it);

        finish();
    }

    public void cancelar(View view) {
        finish();
    }
}