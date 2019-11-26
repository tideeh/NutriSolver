package br.com.nutrisolver.activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.nutrisolver.R;
import br.com.nutrisolver.objects.Fazenda;
import br.com.nutrisolver.tools.DataBaseUtil;
import br.com.nutrisolver.tools.TabsAdapter;
import br.com.nutrisolver.tools.UserUtil;

public class NovaMainActivity extends AppCompatActivity {
    LotesFragment lotesFragment;
    DietasFragment dietasFragment;
    TestesFragment testesFragment;
    static DataFromActivityToFragment dataFromActivityToLotesFragment;
    static DataFromActivityToFragment dataFromActivityToDietasFragment;
    static DataFromActivityToFragment dataFromActivityToTestesFragment;
    private boolean first_select_ignored;

    private int[] tabIcons = {
            R.drawable.tab_lotes2,
            R.drawable.tab_dietas2,
            R.drawable.tab_testes2
    };

    private String[] tabTexts = {
            "LOTES",
            "DIETAS",
            "TESTES"
    };

    private SharedPreferences sharedpreferences;
    private String fazenda_corrente_id;
    private String fazenda_corrente_nome;
    private TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager(), 3);
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<String> fazendas_nomes;
    private List<String> fazendas_ids;
    private Spinner spinner;
    private boolean jaconfigurou = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_main);

        first_select_ignored = false;

        sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1");
        fazenda_corrente_nome = sharedpreferences.getString("fazenda_corrente_nome", "-1");
        spinner = findViewById(R.id.spn_fazendas);

        configura_toolbar_com_nav_drawer();

        if (savedInstanceState != null) {
            // se ja foram criados, utiliza eles
            lotesFragment = (LotesFragment) getSupportFragmentManager().getFragment(savedInstanceState, "lotesFragment");
            dietasFragment = (DietasFragment) getSupportFragmentManager().getFragment(savedInstanceState, "dietasFragment");
            testesFragment = (TestesFragment) getSupportFragmentManager().getFragment(savedInstanceState, "testesFragment");
        }
        else{
            lotesFragment = new LotesFragment();
            dietasFragment = new DietasFragment();
            testesFragment = new TestesFragment();
        }

        dataFromActivityToLotesFragment = (DataFromActivityToFragment) lotesFragment;
        dataFromActivityToDietasFragment = (DataFromActivityToFragment) dietasFragment;
        dataFromActivityToTestesFragment = (DataFromActivityToFragment) testesFragment;

        inicia_tab_fragments();

        configura_spinner_fazendas();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // salva os fragmentos
        getSupportFragmentManager().putFragment(outState, "lotesFragment", lotesFragment);
        getSupportFragmentManager().putFragment(outState, "dietasFragment", dietasFragment);
        getSupportFragmentManager().putFragment(outState, "testesFragment", testesFragment);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // fecha o navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
    }

    private void inicia_tab_fragments() {
        tabsAdapter = new TabsAdapter(getSupportFragmentManager(), 3);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabLayout);

        tabsAdapter.add(lotesFragment, "Lotes");
        tabsAdapter.add(dietasFragment, "Dietas");
        tabsAdapter.add(testesFragment, "Testes");
        viewPager.setAdapter(tabsAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();
    }

    private void setupTabIcons() {

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);

        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.tabLayout_selected_icon_color), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(getResources().getColor(R.color.tabLayout_unselected_icon_color), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(getResources().getColor(R.color.tabLayout_unselected_icon_color), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.tabLayout_selected_icon_color), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.tabLayout_unselected_icon_color), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void configura_toolbar_com_nav_drawer() {
        Toolbar my_toolbar = findViewById(R.id.my_toolbar_main);
        setSupportActionBar(my_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, my_toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        getSupportActionBar().setTitle("Fazenda: " + fazenda_corrente_nome);
    }

    private void configura_spinner_fazendas() {

        DataBaseUtil.getInstance().getDocumentsWhereEqualTo("fazendas", "dono_uid", UserUtil.getCurrentUser().getUid())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            fazendas_nomes = new ArrayList<>();
                            fazendas_ids = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                fazendas_nomes.add(document.toObject(Fazenda.class).getNome());
                                fazendas_ids.add(document.toObject(Fazenda.class).getId());
                            }
                            String[] opcoes = fazendas_nomes.toArray(new String[0]);
                            ArrayAdapter<String> spn_adapter = new ArrayAdapter<String>(NovaMainActivity.this, R.layout.spinner_layout, opcoes);
                            spinner.setAdapter(spn_adapter);

                            spinner.setSelection(fazendas_ids.indexOf(fazenda_corrente_id));

                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (first_select_ignored) {

                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString("fazenda_corrente_id", fazendas_ids.get(position));
                                        editor.putString("fazenda_corrente_nome", fazendas_nomes.get(position));
                                        editor.apply();
                                        // fecha o navigation drawer
                                        mDrawerLayout = findViewById(R.id.drawer_layout);
                                        mDrawerLayout.closeDrawers();

                                        fazenda_corrente_nome = fazendas_nomes.get(position);
                                        fazenda_corrente_id = fazendas_ids.get(position);

                                        //envia_sinal_pros_fragments();
                                        dataFromActivityToLotesFragment.sendData("atualiza_lotes", null);
                                        dataFromActivityToDietasFragment.sendData("atualiza_dietas", null);

                                        getSupportActionBar().setTitle("Fazenda: " + fazenda_corrente_nome);
                                    }
                                    first_select_ignored = true;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            Log.i("MY_FIRESTORE", "Erro ao recuperar documentos Fazendas: " + task.getException());
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.miDeslogar:
                logout();
                return true;

            case R.id.mi_refresh:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        UserUtil.logOut();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove("fazenda_corrente_id");
        editor.remove("fazenda_corrente_nome");
        editor.apply();

        startActivity(new Intent(this, Login.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_itens, menu);

        MenuItem mSearch = menu.findItem(R.id.mi_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //mAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public interface DataFromActivityToFragment {
        void sendData(String data, Object object);
    }

    public void sidebar_testar_amostra(View v) {
        startActivity(new Intent(this, ExecutarTeste1.class));
    }

    public static void sendData(String fragment, String data, Object object){
        switch (fragment){
            case "DietasFragment":
                if(dataFromActivityToDietasFragment != null)
                    dataFromActivityToDietasFragment.sendData(data, object);
                break;

            case "LotesFragment":
                if(dataFromActivityToLotesFragment != null)
                    dataFromActivityToLotesFragment.sendData(data, object);
                break;

            case "TestesFragment":
                if(dataFromActivityToTestesFragment != null)
                    dataFromActivityToTestesFragment.sendData(data, object);
                break;

                default:
                    break;
        }
    }
}
