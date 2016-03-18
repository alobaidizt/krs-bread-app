package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class ProductsTabFragment extends Fragment {


    private View fragmentView;
    private String selectedClient;
    private String selectedClientId;
    private DataSnapshot dbSnapshot;
    private DataSnapshot dbRestaurantsSnapshot;
    TableLayout salesTable;

    private Firebase krsRef;
    private ArrayList<String> clientsLabels;
    AutoCompleteTextView restaurants;

    private OnFragmentInteractionListener mListener;

    public ProductsTabFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static ProductsTabFragment newInstance(String param1, String param2) {
        ProductsTabFragment fragment = new ProductsTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        krsRef = helpers.getFirebase();
        krsRef.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dbRestaurantsSnapshot = snapshot;
            }
            @Override
            public void onCancelled(FirebaseError error) {
            }

        });
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
                for (DataSnapshot child : dbRestaurantsSnapshot.getChildren()) {
                    if (child.child("name").getValue().toString().equals(selectedClient)){
                        selectedClientId = child.getKey();
                    }
                }
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void setClientsList() {
        restaurants = (AutoCompleteTextView)
                fragmentView.findViewById(R.id.autoCompleteTextView1);

        clientsLabels = new ArrayList<String>();
        HashMap<String, HashMap> data = (HashMap) dbRestaurantsSnapshot.getValue();

        if (data == null) {
            Toast.makeText(getActivity().getBaseContext(), R.string.products_validator,
                    Toast.LENGTH_LONG).show();
            return;
        }
        for (String restaurant : data.keySet()) {
            String restaurantName = data.get(restaurant).get("name").toString();
            if (restaurantName != null) {
                clientsLabels.add(restaurantName);
            }
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
            restaurantProducts = (HashMap) dbSnapshot.child(selectedClientId).getValue();
        }
        if (restaurantProducts == null) {
            Toast.makeText(getActivity(), R.string.products_validator,
                    Toast.LENGTH_LONG).show();
            return;
        }

        for (String id : restaurantProducts.keySet()) {
            String productName = restaurantProducts.get(id).get("name").toString();
            int i = 1;
            TableRow tbrow = new TableRow(getContext());
            TextView tv1 = new TextView(getContext());
            ImageButton deleteIcon = new ImageButton(getContext());
            tv1.setTextColor(Color.DKGRAY);
            tv1.setGravity(Gravity.LEFT);
            tv1.setTextSize(20);
            tv1.setText(productName);
            deleteIcon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_remove, null));
            deleteIcon.setMaxWidth(1);
            deleteIcon.setPadding(3,3,3,3);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String productId = null;
                    TableRow tableRow = (TableRow) view.getParent();
                    String productName = ((TextView) tableRow.getChildAt(1)).getText().toString();
                    DataSnapshot parentRestaurant = dbSnapshot.child(selectedClientId);
                    for (DataSnapshot child : parentRestaurant.getChildren()){
                        if (child.child("name").getValue().toString().equals(productName)){
                            productId = child.getKey();
                            productName = child.child("name").getValue().toString();
                        }
                    }

                    if (productId == null) {
                        Toast.makeText(getActivity(), "Product Not Found!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    salesTable.removeView(tableRow);
                    krsRef.child("products").child(selectedClientId).child(productId).removeValue();
                }
            });
            tbrow.addView(deleteIcon);
            tbrow.addView(tv1);
            salesTable.addView(tbrow);
        }
    }
}
