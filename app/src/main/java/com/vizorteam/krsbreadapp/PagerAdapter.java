package com.vizorteam.krsbreadapp;

/**
 * Created by ziyad on 3/10/16.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                RestaurantsTabFragment restaurantsTab = new RestaurantsTabFragment();
                return restaurantsTab;
            case 1:
                ProductsTabFragment productsTab = new ProductsTabFragment();
                return productsTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
