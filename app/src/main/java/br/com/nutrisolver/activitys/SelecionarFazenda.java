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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.tools.AdapterFazenda;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.UserUtil;

public class SelecionarFazenda extends AppCompatActivity {
    //private FirebaseFirestore db;
    private SharedPreferences sharedpreferences;
    private ListView listView_Fazendas;
    private AdapterFazenda adapterFazenda;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_fazenda);

        progressBar = findViewById(R.id.progress_bar);
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        configura_listView();

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

        //db.collection("fazendas").whereEqualTo("dono_uid", UserUtil.getCurrentUser().getUid()).get()
        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("fazendas", "dono_uid", UserUtil.getCurrentUser().getUid())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            adapterFazenda.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                adapterFazenda.addItem(document.toObject(Fazenda.class));
                            }
                            progressBar.setVisibility(View.GONE);
                            Log.i("MY_FIRESTORE", "Sucesso atualiza lista de fazendas");
                        } else {
                            Log.i("MY_FIRESTORE", "Error getting documents: " + task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
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

    private void configura_listView() {
        listView_Fazendas = (ListView) findViewById(R.id.lista_fazendas);
        adapterFazenda = new AdapterFazenda(this);
        listView_Fazendas.setAdapter(adapterFazenda);
        listView_Fazendas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("fazenda_corrente_id", adapterFazenda.getItemIdString(position));
                editor.apply();

                Intent it = new Intent(view.getContext(), TelaPrincipal.class);
                startActivity(it);
                finish();
            }
        });
    }
}
