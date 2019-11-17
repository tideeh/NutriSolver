package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.tools.AdapterFazenda;
import br.com.nutrisolver.tools.UserUtil;

public class SelecionarFazenda extends AppCompatActivity {
    private FirebaseFirestore db;
    private SharedPreferences sharedpreferences;
    private List<Fazenda> fazendas;
    private ListView listView_Fazendas;
    private AdapterFazenda adapterFazenda;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_fazenda);

        listView_Fazendas = (ListView) findViewById(R.id.lista_fazendas);
        progressBar = findViewById(R.id.progress_bar);

        db = FirebaseFirestore.getInstance();
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        atualiza_lista_de_fazendas();
        configura_toolbar();
    }

    private void configura_toolbar() {
        // adiciona a barra de tarefas na tela
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
    }

    private void atualiza_lista_de_fazendas() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("fazendas").whereEqualTo("dono_uid", UserUtil.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    fazendas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        fazendas.add(document.toObject(Fazenda.class));
                    }
                    adapterFazenda = new AdapterFazenda(fazendas, SelecionarFazenda.this);
                    listView_Fazendas.setAdapter(adapterFazenda);
                    progressBar.setVisibility(View.GONE);
                } else {
                    Log.d("MY_FIRESTORE", "Error getting documents: ", task.getException());
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        listView_Fazendas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("fazenda_corrente_id", adapterFazenda.getItemIdString(i));
                editor.apply();

                Intent it = new Intent(view.getContext(), TelaPrincipal.class);
                startActivity(it);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

    }

    public void cadastrar_fazenda(View view) {
        startActivity(new Intent(this, CadastrarFazenda.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_itens, menu);

        MenuItem mSearch = menu.findItem(R.id.mi_search);
        mSearch.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.miDeslogar:
                logout();
                return true;

            case R.id.mi_refresh:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        UserUtil.logOut();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove("fazenda_corrente_id");
        editor.apply();

        startActivity(new Intent(this, Login.class));
        finish();
    }
}
