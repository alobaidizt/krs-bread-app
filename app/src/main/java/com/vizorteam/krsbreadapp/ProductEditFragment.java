package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


public class ProductEditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View fragmentView;
    private EditText inputName, inputPrice;
    private TextInputLayout inputLayoutName, inputLayoutPrice;
    private Firebase krsRef;
    private ArrayList<String> restaurantLabels;
    private ArrayList<Object> restaurantValues;
    AutoCompleteTextView restaurants;
    private ArrayAdapter<String> restaurantsAdapter;
    private String selectedRestaurant;
    private String selectedRestaurantId;
    private DataSnapshot dbSnapshot;
    private DataSnapshot dbProductsSnapshot;

    private OnFragmentInteractionListener mListener;

    public ProductEditFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static ProductEditFragment newInstance(String param1, String param2) {
        ProductEditFragment fragment = new ProductEditFragment();
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

//        Firebase.setAndroidContext(getActivity());
        krsRef = helpers.getFirebase();
        krsRef.child("products").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dbProductsSnapshot = snapshot;
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
        krsRef.child("restaurants").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dbSnapshot = snapshot;
                setRestaurantList();
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_edit, container, false);
        fragmentView = view;

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
                selectedRestaurant = (String) parent.getItemAtPosition(pos);
                for (DataSnapshot child : dbSnapshot.getChildren()) {
                    if (child.child("name").getValue().toString().equals(selectedRestaurant)) {
                        selectedRestaurantId = child.getKey();
                    }
                }
            }
        });

        inputLayoutName = (TextInputLayout) view.findViewById(R.id.input_layout_product_name);
        inputLayoutPrice = (TextInputLayout) view.findViewById(R.id.input_layout_product_price);
        inputName = (EditText) view.findViewById(R.id.input_product_name);
        inputPrice = (EditText) view.findViewById(R.id.input_product_price);

        Button btnAdd = (Button) view.findViewById(R.id.btn_add_product);
        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addProduct(v);
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


    public void addProduct(View view) {
        if (inputPrice.getText().toString() == null || inputPrice.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "No Proper Price Entered", Toast.LENGTH_SHORT).show();
            return;
        }
        Double price;
        try {
            price = Double.parseDouble(inputPrice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity().getApplicationContext(), "No Proper Price Entered", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = inputName.getText().toString();

        if (name == null || name.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "Please Enter Product Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRestaurant == null || selectedRestaurant.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "Client Entered Not Found", Toast.LENGTH_SHORT).show();
            return;
        }

        Firebase productRef = null;

        for (DataSnapshot child : dbProductsSnapshot.child(selectedRestaurantId).getChildren()) {
            if (child.child("name").getValue().toString().equals(name)) {
                productRef = child.getRef();
            }
        }
        if (productRef == null) {
            productRef = krsRef.child("products").child(selectedRestaurantId).push();
        }

        Map<Object, Object> productDetails = new HashMap<Object, Object>();
        productDetails.put("name", name);
        productDetails.put("price", price);
        productRef.setValue(productDetails);
        Toast.makeText(getActivity().getApplicationContext(), "Product Added to " + selectedRestaurant + "!", Toast.LENGTH_SHORT).show();

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setRestaurantList() {
        restaurantLabels = new ArrayList<String>();
        HashMap<String, HashMap> data = (HashMap) dbSnapshot.getValue();

        if (data == null) {
            Toast.makeText(getActivity(), R.string.add_restaurant_validator,
                    Toast.LENGTH_LONG).show();
            return;
        }
        for (String restaurant : data.keySet()) {
            String restaurantName = data.get(restaurant).get("name").toString();
            restaurantLabels.add(restaurantName);
        }

        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>
                (getContext(),android.R.layout.select_dialog_item, restaurantLabels);

        restaurants.setThreshold(0);
        restaurants.setAdapter(autoCompleteAdapter);
    }
}
