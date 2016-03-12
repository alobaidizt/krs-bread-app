package com.vizorteam.krsbreadapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.app.FragmentManager;

import com.firebase.client.Firebase;

import com.vizorteam.krsbreadapp.MiniPrinterFunctions.Alignment;
import com.vizorteam.krsbreadapp.dummy.DummyContent;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        InvoiceFragment.OnFragmentInteractionListener, RestaurntEditFragment.OnFragmentInteractionListener,
        ItemFragment.OnListFragmentInteractionListener, ProductEditFragment.OnFragmentInteractionListener,
        AdminSalesFragment.OnFragmentInteractionListener, AdminUpdateFragment.OnFragmentInteractionListener,
        RestaurantsTabFragment.OnFragmentInteractionListener, ProductsTabFragment.OnFragmentInteractionListener {

    private FragmentManager fm;
    private Fragment invoiceFragment;
//    private Firebase krsRef;
    private String routeString;
//    public static final String DEVICE_CONFIG = "DeviceConfig";
//    public JobScheduler mJobScheduler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helpers.setFirebase();
//        krsRef = helpers.getFirebase();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        findViewById(R.id.image_background).setVisibility(View.VISIBLE);

        // Restore preferences
//        SharedPreferences settings = getSharedPreferences(DEVICE_CONFIG, 0);
//        routeString = settings.getString("route", "");

        routeString = helpers.getRouteString(getBaseContext());

        if (routeString.isEmpty()) {
            findViewById(R.id.invisible_layout).setVisibility(View.VISIBLE);
        }

        fm = getSupportFragmentManager();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (routeString.isEmpty()) {
                    Toast.makeText(getBaseContext(), "Please Set Route Name!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                startNewInvoice(view);
            }
        });

        Button saveBtn = (Button) findViewById(R.id.btn_route_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputRoutString = ((EditText) findViewById(R.id.input_route_name)).getText().toString();
                if (inputRoutString.isEmpty()) {
                    Toast.makeText(getBaseContext(), "Please Set Route Name!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                routeString = helpers.setRouteString(getBaseContext(), inputRoutString);

                findViewById(R.id.invisible_layout).setVisibility(View.INVISIBLE);
                Toast.makeText(getBaseContext(), "Route Set For This Device!",
                        Toast.LENGTH_LONG).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onResume() {
       super.onResume();
        long date = System.currentTimeMillis();
        TextView dateView = (TextView) this.findViewById(R.id.title_date);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String dateString = sdf.format(date);
        dateView.setText(dateString);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (routeString.isEmpty()) {
            Toast.makeText(getBaseContext(), "Please Set Route Name!",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (id == R.id.nav_current_invoice) {
            this.findViewById(R.id.home_fragment).setVisibility(View.GONE);
//            this.findViewById(R.id.image_background).setVisibility(View.GONE);
            this.findViewById(R.id.invoice_fragment).setVisibility(View.VISIBLE);
            FragmentTransaction ft = fm.beginTransaction();
            if (invoiceFragment != null) {
                ft.show(invoiceFragment).addToBackStack("invoice").commit();
            } else {
                invoiceFragment = new InvoiceFragment();
                ft.add(R.id.invoice_fragment, invoiceFragment).addToBackStack("invoice").commit();
            }

//            Intent myIntent = new Intent(this, TestActivity.class);
//            startActivityFromChild(this, myIntent, 0);

        } else if (id == R.id.nav_restaurant) {
            this.findViewById(R.id.home_fragment).setVisibility(View.VISIBLE);
            this.findViewById(R.id.invoice_fragment).setVisibility(View.GONE);
//            this.findViewById(R.id.image_background).setVisibility(View.GONE);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.home_fragment, new RestaurntEditFragment()).addToBackStack("home").commit();

        } else if (id == R.id.nav_products) {
            this.findViewById(R.id.home_fragment).setVisibility(View.VISIBLE);
            this.findViewById(R.id.invoice_fragment).setVisibility(View.GONE);
//            this.findViewById(R.id.image_background).setVisibility(View.GONE);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.home_fragment, new ProductEditFragment()).addToBackStack("home").commit();

        } else if (id == R.id.nav_update) {
            this.findViewById(R.id.home_fragment).setVisibility(View.VISIBLE);
            this.findViewById(R.id.invoice_fragment).setVisibility(View.GONE);
//            this.findViewById(R.id.image_background).setVisibility(View.GONE);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.home_fragment, new AdminUpdateFragment()).addToBackStack("home").commit();

        } else if (id == R.id.nav_sales) {
            this.findViewById(R.id.home_fragment).setVisibility(View.VISIBLE);
            this.findViewById(R.id.invoice_fragment).setVisibility(View.GONE);
//            this.findViewById(R.id.image_background).setVisibility(View.GONE);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.home_fragment, new AdminSalesFragment()).addToBackStack("home").commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startNewInvoice(View view) {

        this.findViewById(R.id.home_fragment).setVisibility(View.GONE);
//        this.findViewById(R.id.image_background).setVisibility(View.GONE);
        this.findViewById(R.id.invoice_fragment).setVisibility(View.VISIBLE);
        FragmentTransaction ft = fm.beginTransaction();
        if (invoiceFragment != null) {
            invoiceFragment = new InvoiceFragment();
            ft.replace(R.id.invoice_fragment, invoiceFragment).addToBackStack("invoice").commit();
        } else {
            invoiceFragment = new InvoiceFragment();
            ft.add(R.id.invoice_fragment, invoiceFragment).addToBackStack("invoice").commit();
        }
    }

    public void onFrragmentInteraction (Uri uri) {
    }

    public void PrintText(View view) {
        /*if (!checkClick.isClickEvent()) {
            return;
        }*/

//        String portName = PrinterTypeActivity.getPortName();
//        String portSettings = PrinterTypeActivity.getPortSettings();



        String portName = "BT:KRS Bread A";
        String portSettings = "portable;escpos";


        TextView edittext_texttoprint = (TextView) findViewById(R.id.editText);
        byte[] texttoprint = edittext_texttoprint.getText().toString().getBytes();

        MiniPrinterFunctions.PrintText(this, portName, portSettings, false, false, false, false, (byte)0, (byte)0, 0, Alignment.Center, texttoprint);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    private void clearBackStack(int i) {
        Log.e("fragment Id: ", Integer.toString(i));
        if (fm.getFragments() == null) {
            return;
        }
        int count = fm.getFragments().size();
        Log.e("fragments count: ", Integer.toString(fm.getFragments().size()));
        if (fm.getFragments().size() > 0) {
            Log.e("coun before poping: ", Integer.toString(fm.getFragments().size()));
            fm.popBackStack();
            Log.e("coun after poping: ", Integer.toString(fm.getFragments().size()));
        }
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
