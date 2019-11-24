package br.com.nutrisolver.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Dieta implements Parcelable {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");

    private String id = UUID.randomUUID().toString();
    private boolean ativo = true;
    private String data_criacao = sdf.format(new Timestamp(System.currentTimeMillis()));
    private String nome = "";
    private String lote_id = "";
    private String fazenda_id = "";
    private List<String> ingredientes_nomes = new ArrayList<>(); // tambem serve como DocumentReference pois o id do ingrediente eh o seu nome

    public Dieta(){}

    public Dieta(String fazenda_id){
        this.fazenda_id = fazenda_id;
    }

    public Dieta(String fazenda_id, String lote_id){
        this.fazenda_id = fazenda_id;
        this.lote_id = lote_id;
    }

    public Dieta(String nome, String fazenda_id, String lote_id){
        this.nome = nome;
        this.fazenda_id = fazenda_id;
        this.lote_id = lote_id;
    }

    protected Dieta(Parcel in) {
        id = in.readString();
        ativo = in.readByte() != 0;
        data_criacao = in.readString();
        nome = in.readString();
        lote_id = in.readString();
        fazenda_id = in.readString();
        ingredientes_nomes = in.createStringArrayList();
    }

    public static final Creator<Dieta> CREATOR = new Creator<Dieta>() {
        @Override
        public Dieta createFromParcel(Parcel in) {
            return new Dieta(in);
        }

        @Override
        public Dieta[] newArray(int size) {
            return new Dieta[size];
        }
    };

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

    public String getFazenda_id() {
        return fazenda_id;
    }

    public void setFazenda_id(String fazenda_id) {
        this.fazenda_id = fazenda_id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (ativo ? 1 : 0));
        dest.writeString(data_criacao);
        dest.writeString(nome);
        dest.writeString(lote_id);
        dest.writeString(fazenda_id);
        dest.writeStringList(ingredientes_nomes);
    }
}
