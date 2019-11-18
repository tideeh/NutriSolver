package br.com.nutrisolver.tools;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Lote;

public class AdapterLote extends BaseAdapter {
    private final List<Lote> lotes;
    private final Activity act;

    public AdapterLote(List<Lote> lotes, Activity act) {
        this.lotes = lotes;
        this.act = act;
    }

    @Override
    public int getCount() {
        return lotes.size();
    }

    @Override
    public Object getItem(int position) {
        return lotes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getItemIdString(int pos){
        return lotes.get(pos).getId();
    }

    public String getItemName(int pos){
        return lotes.get(pos).getNome();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = act.getLayoutInflater().inflate(R.layout.lista_lote_item, parent, false);

        Lote lote = lotes.get(position);

        TextView nome = (TextView) view.findViewById(R.id.lista_lote_titulo);

        nome.setText(lote.getNome());
        //descricao.setText(curso.getDescricao());
        //imagem.setImageResource(R.drawable.java);

        return view;
    }
}