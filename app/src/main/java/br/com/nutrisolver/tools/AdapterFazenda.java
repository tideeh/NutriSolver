package br.com.nutrisolver.tools;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;

public class AdapterFazenda extends BaseAdapter {
    private final List<Fazenda> list_items;
    private final Activity act;

    public AdapterFazenda(Activity act){
        this.list_items = new ArrayList<>();
        this.act = act;
    }

    public AdapterFazenda(List<Fazenda> list_items, Activity act) {
        if(list_items == null)
            this.list_items = new ArrayList<>();
        else
            this.list_items = list_items;

        this.act = act;
    }

    @Override
    public int getCount() {
        return list_items.size();
    }

    @Override
    public Object getItem(int position) {
        return list_items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getItemIdString(int pos){
        return list_items.get(pos).getId();
    }

    public String getItemName(int pos){
        return list_items.get(pos).getNome();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = act.getLayoutInflater().inflate(R.layout.lista_fazenda_item, parent, false);

        Fazenda item = list_items.get(position);

        TextView nome = (TextView) view.findViewById(R.id.lista_fazenda_titulo);

        nome.setText(item.getNome());

        return view;
    }

    public void addItem(Fazenda item){
        this.list_items.add(item);
        this.notifyDataSetChanged();
    }
}