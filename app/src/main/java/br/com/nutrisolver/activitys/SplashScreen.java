package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import br.com.nutrisolver.BuildConfig;
import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.UserUtil;

public class SplashScreen extends AppCompatActivity {
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private Fazenda fazenda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        Log.i("MY_VERSION_CONTROL", "versionCode: " + versionCode);
        Log.i("MY_VERSION_CONTROL", "versionName: " + versionName);

        ((TextView) findViewById(R.id.splash_version_name)).setText(versionName);

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

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verificaLogin();
            }
        }, 500);
    }

    private void verificaLogin() {

        if (!UserUtil.isLogged()) {
            Intent it = new Intent(this, Login.class);
            startActivity(it);
            finish();
        }
        else {
            // ja esta logado, vai para tela inicial ou para tela de selecionar fazendas
            fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1"); // getting String

            DataBaseUtil.getInstance().getDocument("fazendas", fazenda_corrente_id)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Log.i("VERIFICA_FAZ_CORRENTE", "1");
                            if (task.isSuccessful()) {
                                if(task.getResult() != null) {
                                    fazenda = task.getResult().toObject(Fazenda.class);
                                    if (fazenda != null) {
                                        Log.i("VERIFICA_FAZ_CORRENTE", "2");
                                        if (fazenda.getDono_uid().equals(UserUtil.getCurrentUser().getUid())) { // ja possui fazenda corrente e eh dele
                                            Log.i("VERIFICA_FAZ_CORRENTE", "3");

                                            SharedPreferences.Editor editor = sharedpreferences.edit();
                                            editor.putString("fazenda_corrente_id", fazenda.getId());
                                            editor.putString("fazenda_corrente_nome", fazenda.getNome());
                                            editor.apply();

                                            startActivity(new Intent(getApplicationContext(), NovaMainActivity.class));
                                            finish();
                                            return;
                                        }
                                    }
                                }
                            }
                            Log.i("VERIFICA_FAZ_CORRENTE", "4");

                            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                            finish();
                        }
                    });
        }
    }
}
