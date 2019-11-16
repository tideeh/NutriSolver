package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private FirebaseFirestore db;
    private Fazenda fazenda;
    private FirebaseUser currentUser;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progress_bar);
/*
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {

        }
        catch (NoSuchAlgorithmException e) {

        }
        */


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verificaLogin();
            }
        }, 1000);
    }

    private void verificaLogin() {
        progressBar.setVisibility(View.VISIBLE);

        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            progressBar.setVisibility(View.GONE);
            // nao esta logado, vai para tela de login
            Intent it = new Intent(this, Login.class);
            startActivity(it);
            finish();
        }
        else{
            // ja esta logado, vai para tela inicial ou para tela de selecionar fazendas
            fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1"); // getting String

            DocumentReference docRef = db.collection("fazendas").document(fazenda_corrente_id);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.i("VERIFICA_FAZ_CORRENTE", "1");
                    fazenda = documentSnapshot.toObject(Fazenda.class);
                    if(fazenda != null){
                        Log.i("VERIFICA_FAZ_CORRENTE", "2");
                        if(fazenda.getDono_uid().equals(currentUser.getUid())){ // ja possui fazenda corrente e eh dele
                            progressBar.setVisibility(View.GONE);
                            Log.i("VERIFICA_FAZ_CORRENTE", "3");
                            startActivity(new Intent(getApplicationContext(), TelaPrincipal.class));
                            finish();
                            return;
                        }
                    }
                    Log.i("VERIFICA_FAZ_CORRENTE", "4");
                    progressBar.setVisibility(View.GONE);

                    //SharedPreferences.Editor editor = sharedpreferences.edit();
                    //editor.remove("fazenda_corrente_id");
                    //editor.apply();

                    startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Log.i("VERIFICA_FAZ_CORRENTE", "5");
                    startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                    finish();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    progressBar.setVisibility(View.GONE);
                    Log.i("VERIFICA_FAZ_CORRENTE", "6");
                    startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                    finish();
                }
            });
        }
    }


}
