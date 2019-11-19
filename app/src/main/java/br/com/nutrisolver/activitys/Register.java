package br.com.nutrisolver.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.UserUtil;

public class Register extends AppCompatActivity {
    private EditText input_email;
    private EditText input_senha;
    private EditText input_senha_repetir;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        input_email = findViewById(R.id.register_input_email);
        input_senha = findViewById(R.id.register_input_senha);
        input_senha_repetir = findViewById(R.id.register_input_senha_repetir);

        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (UserUtil.isLogged()) {
            startActivity(new Intent(this, SelecionarFazenda.class));
            finish();
        }
    }

    public void registrar(View view) {
        if (!validaDados()) {
            return;
        }

        String email = input_email.getText().toString();
        String senha = input_senha.getText().toString();

        UserUtil.createUserWithEmailAndPassword(this, email, senha, progressBar);
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
        } else if (senha.length() < 6) {
            input_senha.setError("Necessário pelo menos 6 caracteres.");
            valido = false;
        } else {
            input_senha.setError(null);
        }

        String senhaRepetir = input_senha_repetir.getText().toString();
        if (!senha.equals(senhaRepetir)) {
            input_senha_repetir.setError("Senhas diferentes.");
            valido = false;
        } else {
            input_senha_repetir.setError(null);
        }

        return valido;
    }
}
