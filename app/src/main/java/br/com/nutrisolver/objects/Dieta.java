package br.com.nutrisolver.objects;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dieta implements Serializable {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");

    private String id = UUID.randomUUID().toString();
    private boolean ativo = true;
    private String data_criacao = sdf.format(new Timestamp(System.currentTimeMillis()));
    private String lote_id = "";
    private List<String> ingredientes_nomes = new ArrayList<>(); // tambem serve como DocumentReference pois o id do ingrediente eh o seu nome

    public Dieta(){}

    public Dieta(String lote_id){
        this.lote_id = lote_id;
    }

    public List<String> getIngredientes_nomes() {
        return ingredientes_nomes;
    }

    public void setIngredientes_nomes(List<String> ingredientes_nomes) {
        this.ingredientes_nomes = ingredientes_nomes;
    }

    public void addIngrediente_nome(String ingrediente_nome){
        if(this.ingredientes_nomes == null)
            this.ingredientes_nomes = new ArrayList<>();

        this.ingredientes_nomes.add(ingrediente_nome);
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
