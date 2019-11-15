package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.tools.AdapterFazenda;
import br.com.nutrisolver.tools.ToastUtil;

public class SelecionarFazenda extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private Fazenda fazenda;
    private List<Fazenda> fazendas;
    private ListView listaFazendas;
    private AdapterFazenda adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_fazenda);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            // nao esta logado!!
            startActivity(new Intent(this, Login.class));
            finish();
        }

        listaFazendas = (ListView) findViewById(R.id.lista_fazendas);
        fazendas = new ArrayList<>();

        db.collection("fazendas").whereEqualTo("dono_uid", currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        fazendas.add(document.toObject(Fazenda.class));
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    adapter = new AdapterFazenda(fazendas, SelecionarFazenda.this);
                    listaFazendas.setAdapter(adapter);
                } else {
                    ToastUtil.show(getApplicationContext(), "Erro ao recuperar documentos Fazendas", Toast.LENGTH_SHORT);
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        listaFazendas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("fazenda_corrente_id", adapter.getItemIdString(i));
                editor.apply();

                Intent it = new Intent(view.getContext(), TelaPrincipal.class);
                startActivity(it);
                finish();
            }
        });

    }

    public void cadastrarFazenda(View view) {
        startActivity(new Intent(this, CadastrarFazenda.class));
        finish();
    }
}
