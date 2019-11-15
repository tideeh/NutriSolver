package br.com.nutrisolver.tools;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;

public class AdapterFazenda extends BaseAdapter {
    private final List<Fazenda> fazendas;
    private final Activity act;

    public AdapterFazenda(List<Fazenda> fazendas, Activity act) {
        this.fazendas = fazendas;
        this.act = act;
    }

    @Override
    public int getCount() {
        return fazendas.size();
    }

    @Override
    public Object getItem(int position) {
        return fazendas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getItemIdString(int pos){
        return fazendas.get(pos).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = act.getLayoutInflater().inflate(R.layout.lista_fazenda_item, parent, false);

        Fazenda fazenda = fazendas.get(position);

        TextView nome = (TextView) view.findViewById(R.id.lista_fazenda_titulo);

        nome.setText(fazenda.getNome());
        //descricao.setText(curso.getDescricao());
        //imagem.setImageResource(R.drawable.java);

        return view;
    }
}