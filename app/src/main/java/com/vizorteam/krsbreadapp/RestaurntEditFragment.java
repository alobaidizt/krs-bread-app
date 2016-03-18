package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RestaurntEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RestaurntEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RestaurntEditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Toolbar toolbar;
    private EditText inputName, inputAddress, inputPhone;
    private TextInputLayout inputLayoutName, inputLayoutAddress, inputLayoutPhone;
    private Button btnSignUp;
    private DataSnapshot dbSnapshot;
    Firebase krsRef;

    public RestaurntEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RestaurntEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RestaurntEditFragment newInstance(String param1, String param2) {
        RestaurntEditFragment fragment = new RestaurntEditFragment();
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

//        krsRef = new Firebase("https://krs-bread-app.firebaseio.com/");
        krsRef = helpers.getFirebase();
        krsRef.child("restaurants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dbSnapshot = snapshot;
            }
            @Override public void onCancelled(FirebaseError error) { }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurnt_edit, container, false);

        inputLayoutName = (TextInputLayout) view.findViewById(R.id.input_layout_name);
        inputLayoutAddress = (TextInputLayout) view.findViewById(R.id.input_layout_address);
        inputLayoutPhone = (TextInputLayout) view.findViewById(R.id.input_layout_phone);
        inputName = (EditText) view.findViewById(R.id.restaurant_name);
        inputAddress = (EditText) view.findViewById(R.id.input_address);
        inputPhone = (EditText) view.findViewById(R.id.input_phone);
        btnSignUp = (Button) view.findViewById(R.id.btn_add);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputAddress.addTextChangedListener(new MyTextWatcher(inputAddress));
        inputPhone.addTextChangedListener(new MyTextWatcher(inputPhone));

        btnSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitForm();
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


    /**
     * Validating form
     */
    private void submitForm() {
        if (!validateName()) {
            return;
        }

        if (!validateAddress()) {
            return;
        }

        if (!validatePhone()) {
            return;
        }

        String name = inputName.getText().toString();
        String address = inputAddress.getText().toString();
        String phone = inputPhone.getText().toString();
        Restaurant restaurantDetails = new Restaurant(name, address, phone);
        Firebase restaurantRef = null;

        for (DataSnapshot child : dbSnapshot.getChildren()) {
            if (child.child("name").getValue().toString().equals(name)) {
                restaurantRef = child.getRef();
            }
        }
        if (restaurantRef == null) {
            restaurantRef = krsRef.child("restaurants").push();
        }
        restaurantRef.setValue(restaurantDetails);
        Toast.makeText(getActivity().getApplicationContext(), "Restaurant Added!", Toast.LENGTH_SHORT).show();
    }

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateAddress() {
        String email = inputAddress.getText().toString().trim();

        if (email.isEmpty()) {
            inputLayoutAddress.setError(getString(R.string.err_msg_address));
            requestFocus(inputAddress);
            return false;
        } else {
            inputLayoutAddress.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePhone() {
        String phone = inputPhone.getText().toString().trim();

        if (!isValidPhone(phone)) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone));
            requestFocus(inputPhone);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.restaurant_name:
                    validateName();
                    break;
                case R.id.input_address:
                    validateAddress();
                    break;
                case R.id.input_phone:
                    validatePhone();
                    break;
            }
        }
    }
    public class Restaurant {
        private String name;
        private String address;
        private String phone;
        public Restaurant() {}
        public Restaurant(String name, String address, String phone) {
            this.name = name;
            this.address = address;
            this.phone = phone;
        }
        public String getName() {
            return name;
        }
        public String getAddress() {
            return address;
        }
        public String getPhone() {
            return phone;
        }
    }

}
