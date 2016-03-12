package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProductsTabFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    String selectedRoute;
    private View fragmentView;
    private double invoiceTotal;
    private int invoiceNum;
    private String selectedClient;
    private Spinner routes;
    private MainActivity mainActivity;
    DataSnapshot dbSnapshot;
    private List<String> routesList;
    TableLayout salesTable;

    private EditText inputName, inputPrice;
    private TextInputLayout inputLayoutName, inputLayoutPrice;
    private Firebase krsRef;
    private ArrayList<String> clientsLabels;
    private ArrayList<Object> restaurantValues;
    AutoCompleteTextView restaurants;

    private OnFragmentInteractionListener mListener;

    public ProductsTabFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProductsTabFragment newInstance(String param1, String param2) {
        ProductsTabFragment fragment = new ProductsTabFragment();
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

        krsRef = helpers.getFirebase();
        krsRef.child("products").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dbSnapshot = snapshot;
                setClientsList();
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
        fragmentView = inflater.inflate(R.layout.fragment_products_tab, container, false);
        restaurants = (AutoCompleteTextView)
                fragmentView.findViewById(R.id.autoCompleteTextView1);

        restaurants.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                restaurants.showDropDown();
                return false;
            }
        });


        restaurants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                selectedClient = (String) parent.getItemAtPosition(pos);
                updateData();
            }
        });
        return fragmentView;
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
        void onFragmentInteraction(Uri uri);
    }

    private void setClientsList() {
        restaurants = (AutoCompleteTextView)
                fragmentView.findViewById(R.id.autoCompleteTextView1);

        clientsLabels = new ArrayList<String>();
        HashMap<String, HashMap> data = (HashMap) dbSnapshot.getValue();
        for (String restaurant : data.keySet()) {
            clientsLabels.add(restaurant);
        }


        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>
                (getContext(),android.R.layout.select_dialog_item, clientsLabels);


        restaurants.setThreshold(0);
        restaurants.setAdapter(autoCompleteAdapter);
    }

    private void updateData() {
        if (salesTable == null) {
            salesTable = (TableLayout) fragmentView.findViewById(R.id.table_main);
        }
        salesTable.removeAllViews();

        HashMap<String, HashMap> restaurantProducts = null;
        if (dbSnapshot != null || selectedClient != null) {
            restaurantProducts = (HashMap) dbSnapshot.child(selectedClient).getValue();
        }
        if (restaurantProducts == null) {
            return;
        }

        for (String product : restaurantProducts.keySet()) {
            int i = 1;
            TableRow tbrow = new TableRow(getContext());
            TextView tv1 = new TextView(getContext());
            ImageButton deleteIcon = new ImageButton(getContext());
            tv1.setTextColor(Color.DKGRAY);
            tv1.setGravity(Gravity.LEFT);
            tv1.setText(product);
            deleteIcon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_remove, null));
            deleteIcon.setMaxWidth(1);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TableRow tableRow = (TableRow) view.getParent();
                    salesTable.removeView(tableRow);
                    krsRef.child("products").child(selectedClient).child(((TextView) tableRow.getChildAt(0)).getText().toString()).removeValue();
                }
            });
            tbrow.addView(tv1);
            tbrow.addView(deleteIcon);
            salesTable.addView(tbrow);
        }
    }
}
