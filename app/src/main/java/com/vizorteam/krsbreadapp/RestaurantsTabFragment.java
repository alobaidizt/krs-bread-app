package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RestaurantsTabFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RestaurantsTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RestaurantsTabFragment extends Fragment {
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
    private String invoiceStr;
    private Spinner routes;
    private MainActivity mainActivity;
    DataSnapshot dbSnapshot;
    private List<String> routesList;
    TableLayout salesTable;

    private OnFragmentInteractionListener mListener;

    public RestaurantsTabFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static RestaurantsTabFragment newInstance(String param1, String param2) {
        RestaurantsTabFragment fragment = new RestaurantsTabFragment();
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

//        mainActivity = ((MainActivity) getActivity());
        helpers.getFirebase().child("restaurants").addValueEventListener(new ValueEventListener() {

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
        fragmentView = inflater.inflate(R.layout.fragment_restaurants_tab, container, false);
        salesTable = (TableLayout) fragmentView.findViewById(R.id.table_main);
//        updateData();
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void updateData() {
        if (salesTable == null) {
            salesTable = (TableLayout) fragmentView.findViewById(R.id.table_main);
        }
            salesTable.removeAllViews();

            HashMap<String, HashMap> restaurantProducts = null;
            if (dbSnapshot != null) {
                restaurantProducts = (HashMap) dbSnapshot.getValue();
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
                tv1.setTextSize(20);
                tv1.setText(product);
                deleteIcon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_remove, null));
                deleteIcon.setMaxWidth(1);
                deleteIcon.setPadding(3,3,3,3);
                deleteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TableRow tableRow = (TableRow) view.getParent();
                        salesTable.removeView(tableRow);
                        helpers.getFirebase().child("restaurants").child(((TextView) tableRow.getChildAt(1)).getText().toString()).removeValue();
                        helpers.getFirebase().child("products").child(((TextView) tableRow.getChildAt(1)).getText().toString()).removeValue();
                    }
                });
                tbrow.addView(deleteIcon);
                tbrow.addView(tv1);
                salesTable.addView(tbrow);
            }
    }
}
