package br.com.nutrisolver.tools;

import br.com.nutrisolver.objects.Fazenda;

public final class FazendaUtil {
    private static FazendaUtil INSTANCE = null;

    private Fazenda fazendaCorrente = null;

    private FazendaUtil(){
    }

    public static FazendaUtil getInstance(){
        if(INSTANCE == null)
            INSTANCE = new FazendaUtil();
        return INSTANCE;
    }

    public Fazenda getFazendaCorrente() {
        return fazendaCorrente;
    }

    public void setFazendaCorrente(Fazenda fazendaCorrente) {
        this.fazendaCorrente = fazendaCorrente;
    }
}
