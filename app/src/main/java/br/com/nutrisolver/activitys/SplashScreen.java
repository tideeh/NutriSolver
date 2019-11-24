package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import br.com.nutrisolver.BuildConfig;
import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.MyApplication;
import br.com.nutrisolver.tools.UserUtil;

public class SplashScreen extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    //private FirebaseFirestore db;
    //private Fazenda fazenda = null;
    private MyApplication myApplication;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        myApplication = ((MyApplication) getApplication());

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        Log.i("MY_VERSION_CONTROL", "versionCode: " + versionCode);
        Log.i("MY_VERSION_CONTROL", "versionName: " + versionName);

        ((TextView) findViewById(R.id.splash_version_name)).setText(versionName);

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


        //db = FirebaseFirestore.getInstance();
        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!UserUtil.isLogged()) {
                    //progressBar.setVisibility(View.GONE);
                    Intent it = new Intent(SplashScreen.this, Login.class);
                    startActivity(it);
                    finish();
                } else {
                    atualiza_fazenda_corrente();
                }

                //verificaLogin();
            }
        }, 500);
    }

    private void atualiza_fazenda_corrente() {
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1"); // getting String

        //DocumentReference docRef = db.collection("fazendas").document(fazenda_corrente_id);
        DataBaseUtil.getInstance().getDocument("fazendas", fazenda_corrente_id)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                myApplication.setFazenda_corrente(task.getResult().toObject(Fazenda.class));
                            }
                        }
                        verifica_fazenda_usuario();
                    }
                });
    }

    private void verifica_fazenda_usuario() {
        Fazenda fazenda = myApplication.getFazenda_corrente();
        if(fazenda == null){
            startActivity(new Intent(this, SelecionarFazenda.class));
            finish();
        }
        else if (!fazenda.getDono_uid().equals(UserUtil.getCurrentUser().getUid())) {
            startActivity(new Intent(this, SelecionarFazenda.class));
            finish();
        } else {
            atualiza_lotes();
        }
    }

    private void atualiza_lotes() {

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("lotes", "fazenda_id", fazenda_corrente_id)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            myApplication.clearLotes();
                            if(task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    myApplication.addLote(document.toObject(Lote.class));
                                }
                            }
                        }
                        atualiza_dietas();
                    }
                });
    }

    private void atualiza_dietas() {

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("dietas", new String[]{"fazenda_id", "ativo"}, new Object[]{fazenda_corrente_id, true})
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            myApplication.clearDietas();
                            if(task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    myApplication.addDieta(document.toObject(Dieta.class));
                                }
                            }
                        }
                        startActivity(new Intent(getApplicationContext(), NovaMainActivity.class));
                        finish();
                    }
                });
    }
/*
    private void verificaLogin() {
        //progressBar.setVisibility(View.VISIBLE);


        if (!UserUtil.isLogged()) {
            //progressBar.setVisibility(View.GONE);
            Intent it = new Intent(this, Login.class);
            startActivity(it);
            finish();
        } else {
            // ja esta logado, vai para tela inicial ou para tela de selecionar fazendas
            fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1"); // getting String

            //DocumentReference docRef = db.collection("fazendas").document(fazenda_corrente_id);
            DataBaseUtil.getInstance().getDocument("fazendas", fazenda_corrente_id)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Log.i("VERIFICA_FAZ_CORRENTE", "1");
                            if (task.isSuccessful()) {
                                fazenda = task.getResult().toObject(Fazenda.class);
                                if (fazenda != null) {
                                    Log.i("VERIFICA_FAZ_CORRENTE", "2");
                                    if (fazenda.getDono_uid().equals(UserUtil.getCurrentUser().getUid())) { // ja possui fazenda corrente e eh dele
                                        //progressBar.setVisibility(View.GONE);
                                        Log.i("VERIFICA_FAZ_CORRENTE", "3");
                                        startActivity(new Intent(getApplicationContext(), NovaMainActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                            }
                            Log.i("VERIFICA_FAZ_CORRENTE", "4");
                            //progressBar.setVisibility(View.GONE);

                            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                            finish();
                        }
                    });
        }
    }

 */


}
