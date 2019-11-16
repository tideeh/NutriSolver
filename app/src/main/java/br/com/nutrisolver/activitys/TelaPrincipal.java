package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

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
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.AdapterFazenda;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.ToastUtil;

public class TelaPrincipal extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private String fazenda_corrente_nome;
    //private Fazenda fazenda;
    private ListView listaLotes;
    private List<Lote> lotes;
    private AdapterLote adapter;


    // nomes e IDs na mesma ordem para usar no spinner
    private List<String> fazendas_nomes;
    private List<String> fazendas_ids;
    private Spinner spinner;

    private ProgressBar progressBar;

    private static final int CADASTRAR_LOTE_REQUEST = 1001;

    //private boolean atualiza_lotes;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // faz isso aqui tambem (alem do onStart), pois eh usado no spinner
        currentUser = mAuth.getCurrentUser();

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        // adiciona a barra de tarefas na tela
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

        spinner = findViewById(R.id.spn_fazendas);
        atualiza_spinner_fazendas();
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

        currentUser = mAuth.getCurrentUser();

        //progressBar.setVisibility(View.VISIBLE);

        verifica_login_fazenda();


        // fecha o navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
    }

    private void verifica_login_fazenda(){
        if(currentUser == null){
            // nao esta logado!!
            startActivity(new Intent(this, Login.class));
            finish();
        }
        else{
            fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1"); // getting String

            DocumentReference docRef = db.collection("fazendas").document(fazenda_corrente_id);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.toObject(Fazenda.class) != null){
                        if(documentSnapshot.toObject(Fazenda.class).getDono_uid().equals(currentUser.getUid())){ // ja possui fazenda corrente e eh dele
                            fazenda_corrente_nome = documentSnapshot.toObject(Fazenda.class).getNome();
                            atualiza_interface();
                            return;
                        }
                    }
                    startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                    finish();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                    finish();
                }
            });
        }
    }

    private void atualiza_interface(){

        listaLotes = (ListView) findViewById(R.id.lista_lotes);

        db.collection("lotes").whereEqualTo("fazenda_id", fazenda_corrente_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    lotes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        lotes.add(document.toObject(Lote.class));
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    adapter = new AdapterLote(lotes, TelaPrincipal.this);
                    listaLotes.setAdapter(adapter);

                    progressBar.setVisibility(View.GONE);
                } else {
                    ToastUtil.show(getApplicationContext(), "Erro ao recuperar documentos Fazendas", Toast.LENGTH_SHORT);
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        listaLotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Intent it = new Intent(view.getContext(), VisualizaLote.class);
                //it.putExtra("lote_id", adapter.getItemIdString(position));
                //startActivity(it);
            }
        });

        getSupportActionBar().setTitle("Fazenda: "+fazenda_corrente_nome);
    }

    private void atualiza_spinner_fazendas(){
        // spinner com as fazendas

        db.collection("fazendas").whereEqualTo("dono_uid", currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    fazendas_nomes = new ArrayList<>();
                    fazendas_ids = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.i("MY_SPINNER", document.toObject(Fazenda.class).getNome());
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
                            Log.i("MY_SPINNER", "pos "+position+" nome "+fazendas_nomes.get(position));

                            // fecha o navigation drawer
                            mDrawerLayout = findViewById(R.id.drawer_layout);
                            mDrawerLayout.closeDrawers();

                            progressBar.setVisibility(View.VISIBLE);
                            fazenda_corrente_nome = fazendas_nomes.get(position);
                            fazenda_corrente_id = fazendas_ids.get(position);
                            atualiza_interface();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                } else {
                    Log.i("MY_FIREBASE", "Erro ao recuperar documentos Fazendas: "+task.getException());
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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout(){
        mAuth.signOut();
        LoginManager.getInstance().logOut();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove("fazenda_corrente_id");
        editor.apply();

        startActivity(new Intent(this, Login.class));
        finish();
    }

    public void cadastrar_lote(View view) {
        Intent it = new Intent(this, CadastrarLote.class);
        if(fazenda_corrente_nome != null)
            it.putExtra("faz_corrente_nome", fazenda_corrente_nome);

        startActivityForResult(it, CADASTRAR_LOTE_REQUEST);
        //startActivity(new Intent(this, CadastrarLote.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CADASTRAR_LOTE_REQUEST && resultCode == 1){ // foi cadastrado um lote novo
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}
