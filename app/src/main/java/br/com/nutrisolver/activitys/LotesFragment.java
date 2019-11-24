package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.FazendaUtil;
import br.com.nutrisolver.tools.MyApplication;

public class LotesFragment extends Fragment {
    private View v;
    private ListView listView_lotes;
    private AdapterLote adapterLote = null;
    private MyApplication myApplication;
    Fazenda fazenda;
    private static final int CADASTRAR_LOTE_REQUEST = 1001;

    public LotesFragment() {
        Log.i("MY_TABS", "LotesFragment criado");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_lotes, container, false);

        myApplication = ((MyApplication) Objects.requireNonNull(getActivity()).getApplication());

        fazenda = myApplication.getFazenda_corrente();

        v.findViewById(R.id.fab_cadastrar_lote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), CadastrarLote.class);
                if (fazenda != null)
                    it.putExtra("faz_corrente_nome", fazenda.getNome());

                startActivityForResult(it, CADASTRAR_LOTE_REQUEST);
            }
        });

        configura_listView();

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_LOTE_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Lote l = (Lote) data.getSerializableExtra("lote_cadastrado");
            Log.i("MY_ACTIVITY_RESULT", "lote nome: " + l.getNome());

            adapterLote.addItem(l);
        }
    }

    private void configura_listView() {
        listView_lotes = (ListView) v.findViewById(R.id.lista_lotes);
        adapterLote = new AdapterLote(myApplication.getLotes(), getActivity());
        listView_lotes.setAdapter(adapterLote);
        listView_lotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent it = new Intent(view.getContext(), VisualizaLote.class);
                it.putExtra("lote_id", adapterLote.getItemIdString(position));
                it.putExtra("lote_nome", adapterLote.getItemName(position));
                startActivity(it);
            }
        });
    }

}
