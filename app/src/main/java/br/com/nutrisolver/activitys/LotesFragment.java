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
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.AdapterLote;
import br.com.nutrisolver.tools.DataBaseUtil;

public class LotesFragment extends Fragment implements NovaMainActivity.DataFromActivityToFragment {
    private View view;
    private ListView listView_lotes;
    private AdapterLote adapterLote = null;
    private ArrayList<Lote> lista_lotes;

    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;

    private static final int CADASTRAR_LOTE_REQUEST = 1001;

    private ProgressBar progressBar;
    private boolean from_onSaveInstanceState = false;

    public LotesFragment() {
        Log.i("MY_TABS", "LotesFragment criado");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MY_TABS", "LotesFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("MY_TABS", "LotesFragment onCreateView");
        return inflater.inflate(R.layout.fragment_lotes, container, false);
    }

    // aqui a view ja esta criada e nao tem risco de nullpoint
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        from_onSaveInstanceState = false;
        lista_lotes = null;
        if(savedInstanceState != null){
            lista_lotes = savedInstanceState.getParcelableArrayList("lista_lotes");
            from_onSaveInstanceState = savedInstanceState.getBoolean("from_onSaveInstanceState");
        }

        sharedpreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");
        progressBar = view.findViewById(R.id.progress_bar);

        configura_listView();
        atualiza_lista_de_lotes();

        view.findViewById(R.id.fab_cadastrar_lote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), CadastrarLote.class);
                startActivity(it);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("lista_lotes", adapterLote.getList_items());
        outState.putBoolean("from_onSaveInstanceState", true);
    }

    private void atualiza_lista_de_lotes() {
        progressBar.setVisibility(View.VISIBLE);

        if(from_onSaveInstanceState){
            from_onSaveInstanceState = false;
            Log.i("MY_SAVED", "lotes vieram do saved!");
            for(Lote lote : lista_lotes){
                adapterLote.addItem(lote);
            }
            progressBar.setVisibility(View.GONE);
        }
        else {
            DataBaseUtil.getInstance().getDocumentsWhereEqualTo("lotes", "fazenda_id", fazenda_corrente_id)
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                adapterLote.clear();
                                if (task.getResult() != null) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        adapterLote.addItem(document.toObject(Lote.class));
                                        Log.i("MY_FIRESTORE", "lotes do db: " + document.toObject(Lote.class).getNome());
                                    }
                                }
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Log.i("MY_FIRESTORE", "Error getting documents: " + task.getException());
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_LOTE_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Lote l = (Lote) data.getParcelableExtra("lote_cadastrado");
            Log.i("MY_ACTIVITY_RESULT", "lote nome: " + l.getNome());

            adapterLote.addItem(l);
        }
    }

 */

    private void configura_listView() {
        listView_lotes = (ListView) view.findViewById(R.id.lista_lotes);
        adapterLote = new AdapterLote(getActivity());
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

    @Override
    public void sendData(String data, Object object) {
        switch (data){
            case "atualiza_lotes":
                Log.i("MY_SENDDATA", "atualiza_lotes");
                fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");
                atualiza_lista_de_lotes();
                break;

            case "adiciona_lote":
                if(adapterLote != null)
                    adapterLote.addItem((Lote) object);
                break;

            default:
                break;
        }
    }
}
