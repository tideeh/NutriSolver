package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import br.com.nutrisolver.R;
import br.com.nutrisolver.tools.ToastUtil;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN_GOOGLE = 9001;

    private SignInButton bt_login_com_google;
    private Button bt_login_com_senha;
    private LoginButton bt_login_com_facebook;
    private TextView bt_registrar;

    private EditText input_email;
    private EditText input_senha;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

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
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                progressBar.setVisibility(View.GONE);
                //Toast.makeText(getApplicationContext(), "Facebook onCancel", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException error) {
                progressBar.setVisibility(View.GONE);
                ToastUtil.show(getApplicationContext(), "Facebook onError: "+error.toString(), Toast.LENGTH_SHORT);

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

        progressBar = findViewById(R.id.login_progress_bar);
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

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                            finish();
                        }
                        else {
                            ToastUtil.show(getApplicationContext(), "Autenticação falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_login_com_senha:
                // login com email e senha
                String email = input_email.getText().toString();
                String senha = input_senha.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)){
                    ToastUtil.show(this, "Os campos Email e Senha não podem ser vazios!", Toast.LENGTH_SHORT);
                    break;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                            finish();
                        }
                        else {
                            ToastUtil.show(getApplicationContext(), "Autenticação falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                    }
                });

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
            progressBar.setVisibility(View.GONE);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed
                ToastUtil.show(getApplicationContext(), "Autenticação falhou: "+e.toString(), Toast.LENGTH_SHORT);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                            startActivity(new Intent(getApplicationContext(), SelecionarFazenda.class));
                            finish();
                        }
                        else {
                            ToastUtil.show(getApplicationContext(), "Autenticação falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                    }
                });
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
