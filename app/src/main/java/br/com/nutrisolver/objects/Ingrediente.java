package br.com.nutrisolver.objects;

public class Ingrediente {

    private String nome = "";
    private boolean ativo = true;

    public Ingrediente(){}

    public Ingrediente(String nome){
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
