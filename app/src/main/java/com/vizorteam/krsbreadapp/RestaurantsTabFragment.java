package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

public class RestaurantsTabFragment extends Fragment {

    private View fragmentView;
    DataSnapshot dbSnapshot;
    TableLayout salesTable;

    private OnFragmentInteractionListener mListener;

    public RestaurantsTabFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static RestaurantsTabFragment newInstance(String param1, String param2) {
        RestaurantsTabFragment fragment = new RestaurantsTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        fragmentView = inflater.inflate(R.layout.fragment_restaurants_tab, container, false);
        salesTable = (TableLayout) fragmentView.findViewById(R.id.table_main);
        return fragmentView;
    }

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

    private void updateData() {
        if (salesTable == null) {
            salesTable = (TableLayout) fragmentView.findViewById(R.id.table_main);
        }
            salesTable.removeAllViews();

            HashMap<String, HashMap> restaurants = null;
            if (dbSnapshot != null) {
                restaurants = (HashMap) dbSnapshot.getValue();
            }
            if (restaurants == null) {
                return;
            }

            for (String restaurantId : restaurants.keySet()) {
                int i = 1;
                String restaurantName = restaurants.get(restaurantId).get("name").toString();
                TableRow tbrow = new TableRow(getContext());
                TextView tv1 = new TextView(getContext());
                ImageButton deleteIcon = new ImageButton(getContext());
                tv1.setTextColor(Color.DKGRAY);
                tv1.setGravity(Gravity.LEFT);
                tv1.setTextSize(20);
                tv1.setText(restaurantName);
                deleteIcon.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_remove, null));
                deleteIcon.setMaxWidth(1);
                deleteIcon.setPadding(3,3,3,3);
                deleteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TableRow tableRow = (TableRow) view.getParent();
                        String restaurantName = ((TextView) tableRow.getChildAt(1)).getText().toString();
                        String restaurantId = null;
                        for (DataSnapshot child : dbSnapshot.getChildren()) {
                            if (child.child("name").getValue().toString().equals(restaurantName)){
                                restaurantId = child.getKey();
                            }
                        }
                        if (restaurantId == null) {
                            Toast.makeText(getActivity(), "Restaurant Not Found",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        salesTable.removeView(tableRow);
                        helpers.getFirebase().child("restaurants").child(restaurantId).removeValue();
                        helpers.getFirebase().child("products").child(restaurantId).removeValue();
                    }
                });
                tbrow.addView(deleteIcon);
                tbrow.addView(tv1);
                salesTable.addView(tbrow);
            }
    }
}
