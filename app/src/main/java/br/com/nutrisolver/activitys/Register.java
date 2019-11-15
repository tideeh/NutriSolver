package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.ToastUtil;

public class Register extends AppCompatActivity {
    private EditText input_email;
    private EditText input_senha;
    private EditText input_senha_repetir;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        input_email = findViewById(R.id.register_input_email);
        input_senha = findViewById(R.id.register_input_senha);
        input_senha_repetir = findViewById(R.id.register_input_senha_repetir);

        progressBar = findViewById(R.id.register_progress_bar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            // ja esta logado, vai para a tela inicial ou selecionar fazenda
            startActivity(new Intent(this, SelecionarFazenda.class));
            finish();
        }
    }

    public void registrar(View view) {
        if(!validaDados()){
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        String email = input_email.getText().toString();
        String senha = input_senha.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // registrado com sucesso, vai para a tela inicial
                            Intent it = new Intent(getApplicationContext(), SelecionarFazenda.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(it);
                            finish();
                        }
                        else{
                            // registro falhou
                            ToastUtil.show(getApplicationContext(), "Registro falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                    }
                });
        // [END create_user_with_email]
    }

    private boolean validaDados() {
        boolean valido = true;

        String email = input_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            input_email.setError("Campo necessário.");
            valido = false;
        } else {
            input_email.setError(null);
        }

        String senha = input_senha.getText().toString();
        if (TextUtils.isEmpty(senha)) {
            input_senha.setError("Campo necessário.");
            valido = false;
        }
        else if(senha.length() < 6){
            input_senha.setError("Necessário pelo menos 6 caracteres.");
            valido = false;
        }
        else {
            input_senha.setError(null);
        }

        String senhaRepetir = input_senha_repetir.getText().toString();
        if(!senha.equals(senhaRepetir)){
            input_senha_repetir.setError("Senhas diferentes.");
            valido = false;
        } else {
            input_senha_repetir.setError(null);
        }

        return valido;
    }
}
