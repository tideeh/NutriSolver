package br.com.nutrisolver.activitys;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.nutrisolver.R;

public class TestesFragment extends Fragment {

    public TestesFragment() {
        Log.i("MY_TABS", "TestesFragment criado");
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_testes, container, false);
    }

}
