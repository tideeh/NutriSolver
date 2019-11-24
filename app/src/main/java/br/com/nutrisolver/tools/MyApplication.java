package br.com.nutrisolver.tools;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;

public class MyApplication extends Application {

    private Fazenda fazenda_corrente = null;
    private List<Lote> lotes = new ArrayList<>();
    private List<Dieta> dietas = new ArrayList<>();

    public Fazenda getFazenda_corrente() {
        return fazenda_corrente;
    }

    public void setFazenda_corrente(Fazenda fazenda_corrente) {
        this.fazenda_corrente = fazenda_corrente;
    }

    public List<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(List<Lote> lotes) {
        this.lotes = lotes;
    }

    public void addLote(Lote lote){
        this.lotes.add(lote);
    }

    public void clearLotes(){
        this.lotes.clear();
    }

    public List<Dieta> getDietas() {
        return dietas;
    }

    public void setDietas(List<Dieta> dietas) {
        this.dietas = dietas;
    }

    public void addDieta(Dieta dieta){
        this.dietas.add(dieta);
    }

    public void clearDietas(){
        this.dietas.clear();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
