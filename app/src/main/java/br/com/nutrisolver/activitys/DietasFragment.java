package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.AdapterDieta;
import br.com.nutrisolver.tools.AdapterDietaAtual;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.MyApplication;

public class DietasFragment extends Fragment {
    private View v;
    private ListView listView_dietas;
    private AdapterDieta adapterDieta = null;
    private MyApplication myApplication;
    Fazenda fazenda;
    private static final int CADASTRAR_DIETA_REQUEST = 1001;

    public DietasFragment() {
        Log.i("MY_TABS", "DietasFragment criado");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_dietas, container, false);

        myApplication = ((MyApplication) Objects.requireNonNull(getActivity()).getApplication());

        fazenda = myApplication.getFazenda_corrente();

        v.findViewById(R.id.fab_cadastrar_dieta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), CadastrarDieta.class);
                if (fazenda != null)
                    it.putExtra("faz_corrente_nome", fazenda.getNome());

                startActivityForResult(it, CADASTRAR_DIETA_REQUEST);
            }
        });

        configura_listView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_DIETA_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Dieta d = (Dieta) data.getSerializableExtra("dieta_cadastrada");
            Log.i("MY_ACTIVITY_RESULT", "Dieta nome: " + d.getNome());

            adapterDieta.addItem(d);
        }
    }

    private void configura_listView() {
        listView_dietas = (ListView) v.findViewById(R.id.lista_dietas);
        adapterDieta = new AdapterDieta(myApplication.getDietas(), getActivity());
        listView_dietas.setAdapter(adapterDieta);
        listView_dietas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Intent it = new Intent(view.getContext(), VisualizaDieta.class);
                //it.putExtra("lote_id", adapterLote.getItemIdString(position));
                //it.putExtra("lote_nome", adapterLote.getItemName(position));
                //startActivity(it);
            }
        });
    }
}
