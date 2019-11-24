package br.com.nutrisolver.tools;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import br.com.nutrisolver.activitys.DietasFragment;
import br.com.nutrisolver.activitys.LotesFragment;
import br.com.nutrisolver.activitys.TestesFragment;

public class TabsAdapter extends FragmentPagerAdapter {
    private List<Fragment> listFragments = new ArrayList<>();
    private List<String> listFragmentsTitle =  new ArrayList<>();
    int numberOfTabs;

    public TabsAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numberOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        return listFragments.get(position);
    }

    public void add(Fragment frag, String title){
        this.listFragments.add(frag);
        this.listFragmentsTitle.add(title);
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return listFragmentsTitle.get(position);
    }
}
