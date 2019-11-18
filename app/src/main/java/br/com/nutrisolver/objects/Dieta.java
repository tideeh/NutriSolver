package br.com.nutrisolver.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dieta implements Serializable {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");

    private String id;
    private boolean ativo;
    private String data_criacao;
    private String lote_id;
    private List<String> ingredientes;

    public Dieta(){
        this.id = UUID.randomUUID().toString();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.data_criacao = sdf.format(timestamp);
        this.ingredientes = new ArrayList<>();
        this.ativo = true;
    }

    public Dieta(String lote_id){
        this.id = UUID.randomUUID().toString();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.data_criacao = sdf.format(timestamp);
        this.ingredientes = new ArrayList<>();
        this.ativo = true;
        this.lote_id = lote_id;
    }

    public List<String> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<String> ingredientes) {
        this.ingredientes = ingredientes;
    }

    public void addIngrediente(String ingrediente){
        if(this.ingredientes == null)
            this.ingredientes = new ArrayList<>();

        this.ingredientes.add(ingrediente);
    }

    public String getId() {
        return id;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getData_criacao() {
        return data_criacao;
    }

    public void setData_criacao(String data_criacao) {
        this.data_criacao = data_criacao;
    }

    public String getLote_id() {
        return lote_id;
    }

    public void setLote_id(String lote_id) {
        this.lote_id = lote_id;
    }
}
