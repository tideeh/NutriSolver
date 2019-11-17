package br.com.nutrisolver.tools;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import br.com.nutrisolver.activitys.SelecionarFazenda;

public class UserUtil {
    private static FirebaseAuth mAuth;

    // retorna o usuario logado atual (sera null caso nao esteja logado)
    public static FirebaseUser getCurrentUser(){
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth.getCurrentUser();
    }

    // retorna true caso esteja logado, e false caso nao esteja logado
    public static boolean isLogged(){
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }

        return mAuth.getCurrentUser() != null;
    }

    public static void loginWithGoogle(final AppCompatActivity ctx, GoogleSignInAccount acct, final ProgressBar progressBar){
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }

        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                            ctx.startActivity(new Intent(ctx, SelecionarFazenda.class));
                            ctx.finish();
                        }
                        else {
                            ToastUtil.show(ctx, "Autenticação falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public static void loginWithFacebook(final AppCompatActivity ctx, AccessToken token, final ProgressBar progressBar){
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }

        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // login realizado com sucesso, vai para tela inicial ou selecionar fazenda
                            ctx.startActivity(new Intent(ctx, SelecionarFazenda.class));
                            ctx.finish();
                        }
                        else {
                            ToastUtil.show(ctx, "Autenticação falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public static void loginWithEmailAndPassword(final AppCompatActivity ctx, String email, String senha, final ProgressBar progressBar){
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)){
            ToastUtil.show(ctx, "Os campos Email e Senha não podem ser vazios!", Toast.LENGTH_SHORT);
            return;
        }

        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // login realizado com sucesso, vai para a tela selecionar fazenda
                    ctx.startActivity(new Intent(ctx, SelecionarFazenda.class));
                    ctx.finish();
                }
                else {
                    ToastUtil.show(ctx, "Autenticação falhou: "+task.getException(), Toast.LENGTH_SHORT);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public static void createUserWithEmailAndPassword(final AppCompatActivity ctx, String email, String senha, final ProgressBar progressBar){

        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // registrado com sucesso, vai para a tela selecionar fazenda
                            ctx.startActivity(new Intent(ctx, SelecionarFazenda.class));
                            ctx.finish();
                        }
                        else{
                            // registro falhou
                            ToastUtil.show(ctx, "Registro falhou: "+task.getException(), Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public static void logOut(){
        if(mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }

        mAuth.signOut();
        LoginManager.getInstance().logOut();
    }
}
