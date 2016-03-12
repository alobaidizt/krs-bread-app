package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminSalesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminSalesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminSalesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    String selectedRoute;
    private View fragmentView;
    private double invoiceTotal;
    private int invoiceNum;
    private String invoiceStr;
    private Spinner routes;
    private MainActivity mainActivity;
    DataSnapshot dbSnapshot;
    private List<String> routesList;
    TableLayout salesTable;

    public AdminSalesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminSalesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminSalesFragment newInstance(String param1, String param2) {
        AdminSalesFragment fragment = new AdminSalesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Firebase.setAndroidContext(getActivity());

//        mainActivity = ((MainActivity) getActivity());
        helpers.getFirebase().child("analytics").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dbSnapshot = snapshot;
                updateData();
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }

        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_sales, container, false);
        fragmentView = view;
        initTable(view);

//        restaurants = (Spinner) fragmentView.findViewById(R.id.restaurant_spinner);
        routes = (Spinner)
                fragmentView.findViewById(R.id.route_spinner);

        routesList = Arrays.asList("A","B","C","D","E","F","G","H");
        ArrayAdapter<String> routesAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, routesList);

        routes.setAdapter(routesAdapter);


        routes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                selectedRoute = (String) parent.getItemAtPosition(pos);
                updateData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // sometimes you need nothing here
            }
        });

        Button clearBtn = (Button) view.findViewById(R.id.btn_clear_sales);
        clearBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearAnalytics();
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void initTable(View view){
        int txtColor = Color.DKGRAY;
        salesTable = (TableLayout) view.findViewById(R.id.sales_table);
        TableRow tbrow0 = new TableRow(getContext());
        TextView tv0 = new TextView(getContext());
        tv0.setText("Product");
        tv0.setTextColor(txtColor);
        TableLayout.LayoutParams pRowTop = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        pRowTop.weight = 1;
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(getContext());
        tv1.setText("Sales");
        tv1.setTextColor(txtColor);
        tv1.setGravity(Gravity.CENTER);
        tbrow0.addView(tv1);
        salesTable.addView(tbrow0, pRowTop);
    }

    private void updateData() {
        salesTable.removeAllViews();
        int txtColor = Color.DKGRAY;
        TableRow header = new TableRow(getContext());
        TextView productColumn = new TextView(getContext());
        TextView salesColumn = new TextView(getContext());
        productColumn.setText("Product");
        productColumn.setTextColor(txtColor);
        productColumn.setGravity(Gravity.LEFT);
        salesColumn.setText("Sales");
        salesColumn.setTextColor(txtColor);
        salesColumn.setGravity(Gravity.CENTER);
        header.addView(productColumn);
        header.addView(salesColumn);
        salesTable.addView(header);

        HashMap<String, Double> restaurantProducts = null;
        if (dbSnapshot != null && selectedRoute != null) {
            restaurantProducts = (HashMap) dbSnapshot.child(selectedRoute).getValue();
        }
        if (restaurantProducts == null) {
            return;
        }
//        productPriceHash = new HashMap<String, Double>();
        for (String product : restaurantProducts.keySet()) {
            int i = 1;
            TableRow tbrow = new TableRow(getContext());
            TextView tv1 = new TextView(getContext());
            TextView tv2 = new TextView(getContext());
            tv1.setTextColor(Color.DKGRAY);
            tv1.setGravity(Gravity.LEFT);
            tv1.setText(product);
            tv2.setTextColor(Color.DKGRAY);
            tv2.setGravity(Gravity.CENTER);
            tv2.setText("$" + restaurantProducts.get(product).toString());
            tbrow.addView(tv1);
            tbrow.addView(tv2);
            salesTable.addView(tbrow);
            Log.d("logging", product);
            Log.d("logging", restaurantProducts.get(product).toString());
        }
    }

    private void clearAnalytics() {
        helpers.getFirebase().child("analytics").setValue(null);
    }
}
