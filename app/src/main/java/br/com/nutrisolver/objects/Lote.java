package br.com.nutrisolver.objects;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class Lote {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");

    private String id;
    private String nome;
    //private List<Dieta> dietas;
    private String data_criacao;
    private String fazenda_id;

    public Lote(){
        this.id = UUID.randomUUID().toString();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.data_criacao = sdf.format(timestamp);
    }

    public Lote(String nome){
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.data_criacao = sdf.format(timestamp);
    }

    public Lote(String nome, String fazenda_id){
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.data_criacao = sdf.format(timestamp);
        this.fazenda_id = fazenda_id;
    }

    public String getId() {
        return id;
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

    public String getFazenda_id() {
        return fazenda_id;
    }

    public void setFazenda_id(String fazenda_id) {
        this.fazenda_id = fazenda_id;
    }
}
