package br.com.nutrisolver.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.ToastUtil;
import br.com.nutrisolver.tools.UserUtil;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN_GOOGLE = 9001;

    private SignInButton bt_login_com_google;
    private Button bt_login_com_senha;
    private LoginButton bt_login_com_facebook;
    private TextView bt_registrar;

    private EditText input_email;
    private EditText input_senha;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.login_progress_bar);

        // Configura Google Login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Configura Facebook Login
        mCallbackManager = CallbackManager.Factory.create();
        bt_login_com_facebook = findViewById(R.id.btn_login_com_facebook);
        bt_login_com_facebook.setPermissions("email", "public_profile");
        bt_login_com_facebook.setOnClickListener(this);
        bt_login_com_facebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // sucesso, agora autentica com o firebase
                //Toast.makeText(getApplicationContext(), "Facebook onSuccess", Toast.LENGTH_SHORT).show();
                UserUtil.loginWithFacebook(Login.this, loginResult.getAccessToken(), progressBar);
            }

            @Override
            public void onCancel() {
                //Toast.makeText(getApplicationContext(), "Facebook onCancel", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(FacebookException error) {
                ToastUtil.show(getApplicationContext(), "Facebook onError: " + error.toString(), Toast.LENGTH_SHORT);
                progressBar.setVisibility(View.GONE);
            }
        });

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        input_email = findViewById(R.id.login_input_email);
        input_senha = findViewById(R.id.login_input_senha);

        bt_login_com_google = findViewById(R.id.btn_login_com_google);
        setGooglePlusButtonText(bt_login_com_google, getString(R.string.fazer_login_com_google));
        bt_login_com_google.setOnClickListener(this);

        bt_login_com_senha = findViewById(R.id.btn_login_com_senha);
        bt_login_com_senha.setOnClickListener(this);

        bt_registrar = findViewById(R.id.btn_registrar);
        bt_registrar.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (UserUtil.isLogged()) {
            // ja esta logado, vai para a tela inicial ou selecionar fazenda
            startActivity(new Intent(this, SelecionarFazenda.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_login_com_senha:
                // login com email e senha
                String email = input_email.getText().toString();
                String senha = input_senha.getText().toString();

                UserUtil.loginWithEmailAndPassword(this, email, senha, progressBar);
                break;

            case R.id.btn_login_com_google:
                // login com Google
                progressBar.setVisibility(View.VISIBLE);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
                break;

            case R.id.btn_login_com_facebook:
                progressBar.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_registrar:
                startActivity(new Intent(getApplicationContext(), Register.class));
                break;

            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) { // resposta do login com Google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                UserUtil.loginWithGoogle(this, account, progressBar);
            } catch (ApiException e) {
                // Google Sign In failed
                ToastUtil.show(getApplicationContext(), "Autenticação falhou: " + e.toString(), Toast.LENGTH_SHORT);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }
}
