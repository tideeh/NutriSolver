package br.com.nutrisolver.objects;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Fazenda {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");

    private String id = UUID.randomUUID().toString();
    private String nome = "";
    private String data_criacao = sdf.format(new Timestamp(System.currentTimeMillis()));
    private String dono_uid = "";

    public Fazenda(){}

    public Fazenda(String nome){
        this.nome = nome;
    }

    public Fazenda(String nome, String dono_uid){
        this.nome = nome;
        this.dono_uid = dono_uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getData_criacao() {
        return data_criacao;
    }

    public void setData_criacao(String data_criacao) {
        this.data_criacao = data_criacao;
    }

    public String getId() {
        return id;
    }

    public String getDono_uid() {
        return dono_uid;
    }

    public void setDono_uid(String dono_uid) {
        this.dono_uid = dono_uid;
    }
}
