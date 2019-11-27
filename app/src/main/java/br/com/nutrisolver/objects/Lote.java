package br.com.nutrisolver.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class Lote implements Parcelable {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");

    private String id = UUID.randomUUID().toString();
    private String dono_uid = ""; // necessario em todos objetos
    private String fazenda_id = "";

    private String nome = "";
    private String data_criacao = sdf.format(new Timestamp(System.currentTimeMillis()));


    public Lote(){}

    protected Lote(Parcel in) {
        id = in.readString();
        dono_uid = in.readString();
        fazenda_id = in.readString();
        nome = in.readString();
        data_criacao = in.readString();
    }

    public static final Creator<Lote> CREATOR = new Creator<Lote>() {
        @Override
        public Lote createFromParcel(Parcel in) {
            return new Lote(in);
        }

        @Override
        public Lote[] newArray(int size) {
            return new Lote[size];
        }
    };

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

    public String getDono_uid() {
        return dono_uid;
    }

    public void setDono_uid(String dono_uid) {
        this.dono_uid = dono_uid;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(dono_uid);
        dest.writeString(fazenda_id);
        dest.writeString(nome);
        dest.writeString(data_criacao);
    }
}
