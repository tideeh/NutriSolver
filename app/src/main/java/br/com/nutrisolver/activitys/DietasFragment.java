package br.com.nutrisolver.activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Dieta;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.objects.Lote;
import br.com.nutrisolver.tools.AdapterDieta;
import br.com.nutrisolver.tools.DataBaseUtil;

public class DietasFragment extends Fragment implements NovaMainActivity.DataFromActivityToFragment {
    private View view;
    private ListView listView_dietas;
    private AdapterDieta adapterDieta = null;
    private ArrayList<Dieta> lista_dietas;
    Fazenda fazenda;
    private static final int CADASTRAR_DIETA_REQUEST = 1001;

    private ProgressBar progressBar;
    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private boolean from_onSaveInstanceState = false;

    public DietasFragment() {
        Log.i("MY_TABS", "DietasFragment criado");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("MY_TABS", "DietasFragment onCreateView");
        return inflater.inflate(R.layout.fragment_dietas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        from_onSaveInstanceState = false;
        lista_dietas = null;
        if(savedInstanceState != null){
            lista_dietas = savedInstanceState.getParcelableArrayList("lista_dietas");
            from_onSaveInstanceState = savedInstanceState.getBoolean("from_onSaveInstanceState");
        }

        sharedpreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");
        progressBar = view.findViewById(R.id.progress_bar);

        configura_listView();
        atualiza_lista_de_dietas();

        view.findViewById(R.id.fab_cadastrar_dieta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), CadastrarDieta.class);
                startActivityForResult(it, CADASTRAR_DIETA_REQUEST);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("lista_dietas", adapterDieta.getList_items());
        outState.putBoolean("from_onSaveInstanceState", true);
    }

    private void atualiza_lista_de_dietas() {
        progressBar.setVisibility(View.VISIBLE);

        if(from_onSaveInstanceState){
            from_onSaveInstanceState = false;
            Log.i("MY_SAVED", "dietas vieram do saved!");
            for(Dieta dieta : lista_dietas){
                adapterDieta.addItem(dieta);
            }
            progressBar.setVisibility(View.GONE);
        }
        else {

            DataBaseUtil.getInstance().getDocumentsWhereEqualTo("dietas", new String[]{"fazenda_id", "ativo"}, new Object[]{fazenda_corrente_id, true})
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                adapterDieta.clear();
                                if (task.getResult() != null) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        adapterDieta.addItem(document.toObject(Dieta.class));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CADASTRAR_DIETA_REQUEST && resultCode == 1) { // foi cadastrado um lote novo, adiciona ele na lista de lotes e atualizar o adapter da listView
            Dieta d = (Dieta) data.getParcelableExtra("dieta_cadastrada");
            Log.i("MY_ACTIVITY_RESULT", "Dieta nome: " + d.getNome());

            adapterDieta.addItem(d);
        }
    }

    private void configura_listView() {
        listView_dietas = (ListView) view.findViewById(R.id.lista_dietas);
        adapterDieta = new AdapterDieta(getActivity());
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

    @Override
    public void sendData(String data) {
        switch (data){
            case "atualiza_dietas":
                fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");
                atualiza_lista_de_dietas();
                break;

            default:
                break;
        }
    }
}
