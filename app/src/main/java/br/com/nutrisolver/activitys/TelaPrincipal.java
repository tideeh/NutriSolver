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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.UserUtil;
import io.fabric.sdk.android.Fabric;

public class TelaPrincipal extends AppCompatActivity {
    private static final int CADASTRAR_LOTE_REQUEST = 1001;
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private String fazenda_corrente_nome;
    private ListView listView_lotes;
    private AdapterLote adapterLote;
    // nomes e IDs na mesma ordem para usar no spinner
    private List<String> fazendas_nomes;
    private List<String> fazendas_ids;
    private Spinner spinner;
    private ProgressBar progressBar;

    //private boolean atualiza_lotes;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fabric.with(this, new Crashlytics());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        progressBar = findViewById(R.id.progress_bar);
        spinner = findViewById(R.id.spn_fazendas);

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        configura_listView();

        verifica_fazenda_corrente();

        //atualiza_lista_de_lotes();

        configura_toolbar_com_nav_drawer();
        progressBar.setVisibility(View.VISIBLE);
        configura_spinner_fazendas();
    }

    private void atualiza_lista_de_lotes() {
        progressBar.setVisibility(View.VISIBLE);

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("lotes", "fazenda_id", fazenda_corrente_id)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            adapterLote.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                adapterLote.addItem(document.toObject(Lote.class));
                                Log.i("MY_FIRESTORE", "lotes do db: " + document.toObject(Lote.class).getNome());
                            }
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.i("MY_FIRESTORE", "Error getting documents: " + task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

        //db.collection("lotes").whereEqualTo("fazenda_id", fazenda_corrente_id).get()
    }

    private void verifica_fazenda_corrente() {
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");

        //DocumentReference docRef = db.collection("fazendas").document(fazenda_corrente_id);
        DataBaseUtil.getInstance().getDocument("fazendas", fazenda_corrente_id)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.toObject(Fazenda.class) != null) {
                                if (document.toObject(Fazenda.class).getDono_uid().equals(UserUtil.getCurrentUser().getUid())) { // ja possui fazenda corrente e eh dele
                                    fazenda_corrente_nome = document.toObject(Fazenda.class).getNome();
                                    getSupportActionBar().setTitle("Fazenda: " + fazenda_corrente_nome);
                                    return;
                                }
                            }
                        }
                        startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                        finish();
                    }
                });
    }

    private void configura_toolbar_com_nav_drawer() {
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, my_toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_itens, menu);

        MenuItem mSearch = menu.findItem(R.id.mi_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //mAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i("MY_ACTIVITY_RESULT", "2");

        if (!UserUtil.isLogged()) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        // fecha o navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
    }

    private void configura_spinner_fazendas() {

        //db.collection("fazendas").whereEqualTo("dono_uid", UserUtil.getCurrentUser().getUid()).get()
        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("fazendas", "dono_uid", UserUtil.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    fazendas_nomes = new ArrayList<>();
                    fazendas_ids = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        fazendas_nomes.add(document.toObject(Fazenda.class).getNome());
                        fazendas_ids.add(document.toObject(Fazenda.class).getId());
                    }
                    String[] opcoes = fazendas_nomes.toArray(new String[0]);
                    ArrayAdapter<String> spn_adapter = new ArrayAdapter<String>(TelaPrincipal.this, R.layout.spinner_layout, opcoes);
                    spinner.setAdapter(spn_adapter);

                    spinner.setSelection(fazendas_ids.indexOf(fazenda_corrente_id));

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("fazenda_corrente_id", fazendas_ids.get(position));
                            editor.apply();
                            //Log.i("MY_SPINNER", "pos "+position+" nome "+fazendas_nomes.get(position));

                            // fecha o navigation drawer
                            mDrawerLayout = findViewById(R.id.drawer_layout);
                            mDrawerLayout.closeDrawers();

                            fazenda_corrente_nome = fazendas_nomes.get(position);
                            fazenda_corrente_id = fazendas_ids.get(position);

                            atualiza_lista_de_lotes();

                            getSupportActionBar().setTitle("Fazenda: " + fazenda_corrente_nome);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                } else {
                    Log.i("MY_FIRESTORE", "Erro ao recuperar documentos Fazendas: " + task.getException());
                }
            }
        });
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

    public void cadastrar_lote(View view) {
        Intent it = new Intent(this, CadastrarLote.class);
        if (fazenda_corrente_nome != null)
            it.putExtra("faz_corrente_nome", fazenda_corrente_nome);

        startActivityForResult(it, CADASTRAR_LOTE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_LOTE_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Lote l = (Lote) data.getParcelableExtra("lote_cadastrado");
            Log.i("MY_ACTIVITY_RESULT", "lote nome: " + l.getNome());

            adapterLote.addItem(l);
        }
    }

    private void configura_listView() {
        listView_lotes = (ListView) findViewById(R.id.lista_lotes);
        adapterLote = new AdapterLote(this);
        listView_lotes.setAdapter(adapterLote);
        listView_lotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent it = new Intent(view.getContext(), VisualizaLote.class);
                it.putExtra("lote_id", adapterLote.getItemIdString(position));
                it.putExtra("lote_nome", adapterLote.getItemName(position));
                startActivity(it);
            }
        });
    }

    public void sidebar_testar_amostra(View v) {
       startActivity(new Intent(this, ExecutarTeste1.class));
    }
}
