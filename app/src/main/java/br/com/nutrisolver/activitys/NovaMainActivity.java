package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.FazendaUtil;
import br.com.nutrisolver.tools.MyApplication;
import br.com.nutrisolver.tools.TabsAdapter;
import br.com.nutrisolver.tools.UserUtil;

public class NovaMainActivity extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private String fazenda_corrente_nome;
    private TabsAdapter tabsAdapter;
    private TabLayout tabs;
    private ViewPager viewPager;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_main);

        myApplication = ((MyApplication)getApplication());

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        configura_toolbar();

        verifica_fazenda_corrente();
        inicia_tab_fragments();
    }

    private void verifica_fazenda_corrente() {
        Fazenda fazenda = myApplication.getFazenda_corrente();
        if(fazenda == null){
            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
            finish();
        }
        else if (!fazenda.getDono_uid().equals(UserUtil.getCurrentUser().getUid())) {
            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
            finish();
        }
        /*
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");

        DataBaseUtil.getInstance().getDocument("fazendas", fazenda_corrente_id)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult() != null){
                                Fazenda f = task.getResult().toObject(Fazenda.class);
                                if (f != null) {
                                    if (f.getDono_uid().equals(UserUtil.getCurrentUser().getUid())) { // ja possui fazenda corrente e eh dele
                                        fazenda_corrente_nome = f.getNome();
                                        getSupportActionBar().setTitle("Fazenda: " + fazenda_corrente_nome);
                                        atualiza_lista_de_lotes();
                                        return;
                                    }
                                }
                            }
                        }
                        startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                        finish();
                    }
                });

         */
    }

    private void atualiza_lista_de_lotes() {



        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("lotes", "fazenda_id", fazenda_corrente_id)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                myApplication.addLote(document.toObject(Lote.class));
                                Log.i("MY_FIRESTORE", "lotes do db: " + document.toObject(Lote.class).getNome());
                            }
                        } else {
                            Log.i("MY_FIRESTORE", "Error getting documents: " + task.getException());
                        }
                        inicia_tab_fragments();
                    }
                });
    }

    private void inicia_tab_fragments(){
        tabsAdapter = new TabsAdapter(getSupportFragmentManager(), 3);
        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);

        tabsAdapter.add(new LotesFragment(), "Lotes");
        tabsAdapter.add(new DietasFragment(), "Dietas");
        tabsAdapter.add(new TestesFragment(), "Testes");
        viewPager.setAdapter(tabsAdapter);
        tabs.setupWithViewPager(viewPager);
    }

    private void configura_toolbar() {
        // adiciona a barra de tarefas na tela
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
        // adiciona a seta de voltar na barra de tarefas
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
