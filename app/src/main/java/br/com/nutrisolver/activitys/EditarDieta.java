package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.objects.Ingrediente;
import br.com.nutrisolver.tools.AdapterIngredienteNome;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.ToastUtil;
import br.com.nutrisolver.tools.UserUtil;

public class EditarDieta extends AppCompatActivity {
    private final long TIMEOUT_DB = 30 * 60 * 1000; // ms (MIN * 60 * 100)
    public List<String> ingredientes_nomes = new ArrayList<>();// = new String[] { "Milho", "Farelo de Soja", "Feno" };
    Dieta dieta;
    private SharedPreferences sharedpreferences;
    private String lote_id;
    private String lote_nome;
    private String dieta_ativa_id;
    private ProgressBar progressBar;
    private ListView listView_editar_ingredientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dieta);


        progressBar = findViewById(R.id.progress_bar);
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        Intent it = getIntent();
        lote_id = it.getStringExtra("lote_id");
        lote_nome = it.getStringExtra("lote_nome");
        dieta_ativa_id = it.getStringExtra("dieta_ativa_id");

        if (lote_id == null || lote_nome == null) {
            startActivity(new Intent(this, TelaPrincipal.class));
            finish();
        }

        configura_listView();

        atualiza_lista_ingredientes();
        configura_toolbar();
    }

    private void atualiza_lista_ingredientes() {
        //usado para a criacao do primeiro documento dos possiveis ingredientes
        /*
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Milho", new Ingrediente("Milho"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Caroço de algodão", new Ingrediente("Caroço de algodão"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Farelo de Soja", new Ingrediente("Farelo de Soja"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Feno", new Ingrediente("Feno"));
        DataBaseUtil.getInstance().insertDocument("ingredientes", "Silagem", new Ingrediente("Silagem"));

         */


        progressBar.setVisibility(View.VISIBLE);

        // busca no sharedPreferences caso nao tenha passado TIMEOUT_DB desde a ultima consulta no db

        if (System.currentTimeMillis() - sharedpreferences.getLong("ingredientes_nomes_last_update", 0) < TIMEOUT_DB) {
            String ing_aux = sharedpreferences.getString("ingredientes_nomes", null);

            if (ing_aux != null) {
                ingredientes_nomes = new ArrayList<>(Arrays.asList(ing_aux.split(";;;")));
            }

            AdapterIngredienteNome itemsAdapter = new AdapterIngredienteNome(this, ingredientes_nomes);
            listView_editar_ingredientes.setAdapter(itemsAdapter);

            progressBar.setVisibility(View.GONE);
        } else {
            // recebe os possiveis ingredientes
            DataBaseUtil.getInstance().getDocumentsWhereEqualTo("ingredientes", "ativo", true)
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    ingredientes_nomes.add(document.toObject(Ingrediente.class).getNome());
                                }

                                // salva no sharedpreferences por um tempo para evitar muitos acessos no DB
                                String possiveis_ingredientes_string = TextUtils.join(";;;", ingredientes_nomes);

                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("ingredientes_nomes", possiveis_ingredientes_string);
                                editor.putLong("ingredientes_nomes_last_update", System.currentTimeMillis());
                                editor.apply();
                            }
                            AdapterIngredienteNome itemsAdapter = new AdapterIngredienteNome(EditarDieta.this, ingredientes_nomes);
                            listView_editar_ingredientes.setAdapter(itemsAdapter);

                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
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

        dieta = new Dieta();
        dieta.setLote_id(lote_id);

        int len = listView_editar_ingredientes.getCount();
        SparseBooleanArray checked = listView_editar_ingredientes.getCheckedItemPositions();

        for (int i = 0; i < len; i++) {
            if (checked.get(i)) {
                String item = ingredientes_nomes.get(i);
                dieta.addIngrediente_nome(item);
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

    private void configura_listView() {
        listView_editar_ingredientes = findViewById(R.id.listView_editar_ingredientes);
        listView_editar_ingredientes.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        listView_editar_ingredientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (listView_editar_ingredientes.isItemChecked(i)) {
                    view.findViewById(R.id.listView_poss_ing_add).setVisibility(View.GONE);
                    view.findViewById(R.id.listView_poss_ing_remove).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.listView_poss_ing_remove).setVisibility(View.GONE);
                    view.findViewById(R.id.listView_poss_ing_add).setVisibility(View.VISIBLE);
                }

            }
        });
    }
}
