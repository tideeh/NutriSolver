package br.com.nutrisolver.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import br.com.nutrisolver.R;

public class AdapterPossiveisIngredientes extends ArrayAdapter<String> {

    public AdapterPossiveisIngredientes(Context context, List<String> possiveis_ingredientes){
        super(context, 0, possiveis_ingredientes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String ing = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista_possiveis_ingredientes_item, parent, false);
        }

        TextView ing_nome = (TextView) convertView.findViewById(R.id.lista_ingrediente_nome);

        ing_nome.setText(ing);

        return convertView;
    }
}
