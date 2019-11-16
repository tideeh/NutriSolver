package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.ListView;
import android.widget.ProgressBar;
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
    private Fazenda fazenda;
    private ListView listaLotes;
    private List<Lote> lotes;
    private AdapterLote adapter;

    private ProgressBar progressBar;

    private static final int CADASTRAR_LOTE_REQUEST = 1001;

    //private boolean atualiza_lotes;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_itens, menu);
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
                    fazenda = documentSnapshot.toObject(Fazenda.class);
                    if(fazenda != null){
                        if(fazenda.getDono_uid().equals(currentUser.getUid())){ // ja possui fazenda corrente e eh dele
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

        getSupportActionBar().setTitle("Fazenda: "+fazenda.getNome());
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
        startActivityForResult(new Intent(this, CadastrarLote.class), CADASTRAR_LOTE_REQUEST);
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
