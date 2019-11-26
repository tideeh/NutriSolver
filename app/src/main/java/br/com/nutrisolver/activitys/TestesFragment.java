package br.com.nutrisolver.activitys;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.nutrisolver.R;

public class TestesFragment extends Fragment implements NovaMainActivity.DataFromActivityToFragment {
    private View view;

    public TestesFragment() {
        Log.i("MY_TABS", "TestesFragment criado");
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        view.findViewById(R.id.btn_executar_teste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getActivity(), ExecutarTeste1.class);
                startActivity(it);
            }
        });
    }

    @Override
    public void sendData(String data, Object object) {

    }
}
