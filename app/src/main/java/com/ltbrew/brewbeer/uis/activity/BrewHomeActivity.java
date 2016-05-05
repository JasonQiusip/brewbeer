package com.ltbrew.brewbeer.uis.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.interfaceviews.BrewHomeView;
import com.ltbrew.brewbeer.presenter.BrewHomePresenter;
import com.ltbrew.brewbeer.presenter.model.Device;
import com.ltbrew.brewbeer.uis.adapter.SectionsPagerAdapter;
import com.ltbrew.brewbeer.uis.fragment.BrewSessionFragment;
import com.ltbrew.brewbeer.uis.fragment.RecipeFragment;

import java.util.List;

public class BrewHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BrewHomeView {

    private BrewSessionFragment brewSessionFragment = new BrewSessionFragment();
    private RecipeFragment recipeFragment = new RecipeFragment();
    private Fragment[] fragments = new Fragment[]{brewSessionFragment, recipeFragment};
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private BrewHomePresenter brewHomePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brew_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.setFragments(fragments);
        mViewPager = (ViewPager) findViewById(R.id.brewHomeContainer);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
        mViewPager.setOffscreenPageLimit(2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        brewHomePresenter = new BrewHomePresenter(this);
        brewHomePresenter.getDevs();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }


    @Override
    public void onGetDevsSuccess(List<Device> devices) {

    }

    @Override
    public void onGetDevsFailed(String message) {

    }
}
