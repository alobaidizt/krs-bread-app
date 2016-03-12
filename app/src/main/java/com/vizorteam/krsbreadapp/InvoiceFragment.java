package com.vizorteam.krsbreadapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class InvoiceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    View fragmentView;
    TableLayout invoiceTable;

    private OnFragmentInteractionListener mListener;
    private ArrayList<String> restaurantLabels;
    private ArrayList<String> products;
    private HashMap<String, Double> productPriceHash;
    DataSnapshot dbSnapshot;
//    Spinner restaurants;
    String selectedRestaurant;
    private ArrayList<ArrayList<String>> recieptData;
    private double invoiceTotal;
    private int invoiceNum;
    private TextView invoiceLabel;
    private String invoiceStr;
    private AutoCompleteTextView restaurants;
//    private MainActivity mainActivity;
    private Firebase krsRef;

    public InvoiceFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static InvoiceFragment newInstance(String param1, String param2) {
        InvoiceFragment fragment = new InvoiceFragment();
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

//        helpers helper = new helpers();
        krsRef = helpers.getFirebase();

//        mainActivity = ((MainActivity) getActivity());
//        ((krsApp)).initFirebase();
//        mainActivity.getKrsRef().child("products").addValueEventListener(new ValueEventListener() {
        krsRef.child("products").addValueEventListener(new ValueEventListener() {

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invoice, container, false);
        fragmentView = view;
        initTable(view);

//        restaurants = (Spinner) fragmentView.findViewById(R.id.restaurant_spinner);
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
                updateData();
            }
        });

        final String portName = "BT:KRS Bread A";
        final String portSettings = "portable;escpos";

        invoiceLabel = (TextView) fragmentView.findViewById(R.id.invoice_num_label);

        Button btnPrint = (Button) view.findViewById(R.id.btn_print);
        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PrintReceipt(getContext(), portName,portSettings);
            }
        });

        Button btnAdd = (Button) view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addItem(v);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (StringUtils.isNotBlank(readSavedData())) {
            invoiceNum = Integer.parseInt(readSavedData().substring(1));
        } else {
            invoiceNum = 0;
        }
        if (StringUtils.isBlank(Integer.valueOf(invoiceNum).toString())) {
            writeData("A0000000");
            invoiceNum = Integer.parseInt(readSavedData());
        }
        invoiceStr = "A" + String.format("%07d", invoiceNum);
        invoiceLabel.setText(invoiceStr);
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
    }
    public void initTable(View view){
        int txtColor = Color.DKGRAY;
        invoiceTable = (TableLayout) view.findViewById(R.id.table_main);
        TableRow tbrow0 = new TableRow(getContext());
        TextView tv0 = new TextView(getContext());
        tv0.setText(R.string.qty);
        tv0.setTextColor(txtColor);
        TableLayout.LayoutParams pRowTop = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        pRowTop.weight = 1;
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(getContext());
        tv1.setText(R.string.product);
        tv1.setTextColor(txtColor);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(getContext());
        tv2.setText(R.string.price);
        tv2.setTextColor(txtColor);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(getContext());
        tv3.setText(R.string.total);
        tv3.setTextColor(txtColor);
        tbrow0.addView(tv3);
        invoiceTable.addView(tbrow0, pRowTop);
    }

    public void addItem(View view) {
        if (selectedRestaurant == null) {
            Toast.makeText(getActivity(), R.string.toast_select_restaurant,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (selectedRestaurant.isEmpty()) {
            Toast.makeText(getActivity(), R.string.toast_select_restaurant,
                    Toast.LENGTH_LONG).show();
            return;
        }

        int i = 1;
        TableRow tbrow = new TableRow(getContext());
        TextInputLayout til = new TextInputLayout(getContext());
        TextView t3v = new TextView(getContext());
        TextView t4v = new TextView(getContext());
        EditText et = new EditText(getContext());
        ImageButton deleteIcon = new ImageButton(getContext());
        deleteIcon.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_action_remove, null));
        deleteIcon.setMaxWidth(1);
        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TableRow tableRow = (TableRow) view.getParent();
                invoiceTable.removeView(tableRow);
            }
        });
        Spinner dropDown = new Spinner(getContext());
        dropDown.setLayoutMode(Spinner.MODE_DROPDOWN);
        et.setHint(" Num ");
        et.setTextColor(Color.DKGRAY);
        et.setGravity(Gravity.CENTER);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, products);
        dropDown.setAdapter(adapter);
        dropDown.setOnItemSelectedListener(new productWatcher(t3v));
        et.addTextChangedListener(new qtyWatcher(et, t4v, t3v));
        til.addView(et);

        tbrow.addView(til);
        tbrow.addView(dropDown);
        t3v.setTextColor(Color.DKGRAY);
        t3v.setGravity(Gravity.CENTER);
        tbrow.addView(t3v);
        t4v.setTextColor(Color.DKGRAY);
        t4v.setGravity(Gravity.CENTER);
        tbrow.addView(t4v);
        tbrow.addView(deleteIcon);
        invoiceTable.addView(tbrow);
    }
    private class productWatcher implements AdapterView.OnItemSelectedListener {
        TextView tview;
        public productWatcher(TextView t3v) {
           this.tview = t3v;
        }

        @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String bread = parent.getItemAtPosition(position).toString();
                if (productPriceHash.get(bread) == null) {return;}
                double price = productPriceHash.get(bread);
                tview.setText("$" + price);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        }

    private class qtyWatcher implements TextWatcher {
        TextView tv, priceView;
        EditText et;

        public qtyWatcher(EditText et, TextView tv, TextView priceView) {

            Log.d(getResources().getString(R.string.logging), et.getText().toString());

            this.tv = tv;
            this.et = et;
            this.priceView = priceView;
        }

        @Override
            public void afterTextChanged(Editable s) {

                int qtyValue;
                String priceStr = priceView.getText().toString().replace("$","");
                double price = Double.parseDouble(priceStr);
                if (Double.isNaN(price)) {
                    price = 0;
                }
                if (et.getText().toString().isEmpty()) {
                    qtyValue = 0;
                } else {
                    qtyValue = Integer.parseInt(et.getText().toString());
                }
                tv.setText("$" + String.format("%.2f", (double) (qtyValue * price)));

        }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0) {
                }
            }
        }

    private void setRestaurantList() {
//        restaurants = (AutoCompleteTextView)
//                fragmentView.findViewById(R.id.autoCompleteTextView1);

        restaurantLabels = new ArrayList<String>();
        HashMap<String, HashMap> data = (HashMap) dbSnapshot.getValue();
        for (String restaurant : data.keySet()) {
            restaurantLabels.add(restaurant);
        }

//        restaurantLabels.add("Select a client");
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
//                android.R.layout.simple_spinner_dropdown_item) {
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//
//                View v = super.getView(position, convertView, parent);
//                if (position == getCount()) {
//                    ((TextView) v.findViewById(android.R.id.text1)).setText("");
//                    ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
//                }
//
//                return v;
//            }
//
//            @Override
//            public int getCount() {
//                return super.getCount() - 1; // you dont display last item. It is used as hint.
//            }
//
//        };

//        adapter.addAll(restaurantLabels);
//        restaurants.setAdapter(adapter);
//        restaurants.setSelection(adapter.getCount()); // display hint

        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>
                (getContext(),android.R.layout.select_dialog_item, restaurantLabels);


        restaurants.setThreshold(0);
        restaurants.setAdapter(autoCompleteAdapter);
    }


    private void updateData() {
        products = new ArrayList<String>();
        HashMap<String, HashMap> restaurantProducts = (HashMap) dbSnapshot.child(selectedRestaurant).getValue();
        if (restaurantProducts == null) {
            return;
        }
            productPriceHash = new HashMap<String, Double>();
            for (String product : restaurantProducts.keySet()) {
                products.add(product);
                double price = 0;
                price = Double.parseDouble(restaurantProducts.get(product).get("price").toString());
                productPriceHash.put(product, price);
            }
        }


    /**
     * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
     *
     * @param context
     *     Activity for displaying messages to the user
     * @param portName
     *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
     * @param portSettings
     *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
     */
    public boolean PrintReceipt(Context context, String portName, String portSettings) {
        if (selectedRestaurant == "Select a client") {
            Toast.makeText(getActivity(), "Please select a restaurant",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        boolean isGood = false;
        loopTable();
        updateAnalytics();
//        if (true)
//            return true;

        invoiceStr = "A" + String.format("%07d", invoiceNum);
        writeData(invoiceStr);

        ArrayList<String> copiesList = new ArrayList<>(Arrays.asList(selectedRestaurant + " Copy", "KRS Bread Copy"));
        for (int i=0; i<2; i++) {

            ArrayList<byte[]> list = new ArrayList<byte[]>();

                byte[] outputByteBuffer = null;
                String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
                String time = new SimpleDateFormat("hh:mm a").format(new Date());

                list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)

                list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)

                list.add(("\nKRS Bread Distributor\n" + "8300 Hall Road Ste. 200\n" + "Utica, MI 48317\n").getBytes());
                list.add(("(586)489-2454  (248)840-7292\n\n").getBytes());

                list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment

                list.add(new byte[] { 0x1b, 0x44, 0x02, 0x07, 0x22, 0x00 }); // Setting Horizontal Tab

                list.add(("Date: " + date + " ").getBytes());

                list.add(new byte[] { 0x09 }); // Left Alignment"

                list.add(("Time: " + time + " \n" + "ORDER FOR: " + selectedRestaurant).getBytes());
                list.add(("\n------------------------------------------------ \n").getBytes());

                list.add(new byte[] { 0x1b, 0x45, 0x01 }); // Set Emphasized Printing ON

                list.add("SALE\n".getBytes());

                list.add(new byte[]{0x1b, 0x45, 0x00}); // Set Emphasized Printing OFF (same command as on)

                // This uses iterator behind the scene.
                for (ArrayList<String> row : recieptData)
                {
                    int index = 0;
                    list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment
                    for (String element : row)
                    {
                        switch (index++) {
                            case 0:
                                list.add(element.getBytes());
                                list.add(new byte[] { 0x09}); // Setting Horizontal Tab
                                break;
                            case 1:
                                list.add(element.getBytes());
                                list.add(new byte[] { 0x09}); // Setting Horizontal Tab
                                break;
                            case 2:
                                list.add((element).getBytes());
                                break;
                            case 3:
                                list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Right Alignment
                                list.add((element).getBytes());
                                break;
                        }
                    }
                    list.add(("\n").getBytes());
                }

                list.add(new byte[] { 0x1d, 0x21, 0x11 }); // Width and Height Character Expansion <GS> ! n

                list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Right Alignment
                list.add(("$" + invoiceTotal + "\n\n").getBytes());

                list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Expansion - Reference Star Portable Printer Programming Manual

                list.add(("------------------------------------------------ \n").getBytes());

                list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Right Alignment
                list.add(("inv. #: " + invoiceStr + "\n").getBytes());
                list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Right Alignment

                list.add("\n\n".getBytes());
                list.add(("------------------------------------------------ \n").getBytes());
                list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification
                list.add((copiesList.get(i) + "\n").getBytes());
                list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment
                list.add(("------------------------------------------------ \n").getBytes());

                list.add("\n\n\n\n".getBytes());
                isGood = MiniPrinterFunctions.sendCommand(context, portName, portSettings, list);

                try {
                    Thread.sleep(3000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
        }

        invoiceNum++;
        invoiceStr = "A" + String.format("%07d", invoiceNum);
        writeData(invoiceStr);
        invoiceLabel.setText(invoiceStr);
        return isGood;
    }

    private void loopTable() {
        invoiceTotal = 0;
        TableLayout table = (TableLayout) fragmentView.findViewById(R.id.table_main);
        recieptData = new ArrayList<ArrayList<String>>();
        int tableRowIndex = 0;
        for(int i = 0, j = table.getChildCount(); i < j; i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                if ( (i > 0) && StringUtils.isBlank(((TextInputLayout) row.getChildAt(0)).getEditText().getText().toString())) {
                    continue;
                }
                recieptData.add(new ArrayList<String>());
                for (int k = 0, l = row.getChildCount(); k < l; k++) {
                    View field = row.getChildAt(k);
                    String fieldValue = "";
                   switch (field.getClass().getSimpleName()){
                       case "TextView":
                           TextView textView = (TextView) field;
                           fieldValue= textView.getText().toString();
                           break;
                       case "Spinner":
                           Spinner spinnerView = (Spinner) field;
                           fieldValue = spinnerView.getSelectedItem().toString();
                           break;
                       case "TextInputLayout":
                           TextInputLayout textInputView = (TextInputLayout) field;
                           fieldValue = textInputView.getEditText().getText().toString();
                           break;
                   }
                    if (k == 3 && i > 0 && fieldValue != "") {
                        String priceStr = fieldValue.replace("$", "");
                        double itemTotal = Double.parseDouble(priceStr);
                        invoiceTotal += itemTotal;
                    }
                    recieptData.get(tableRowIndex).add(fieldValue);
                }
                tableRowIndex++;
            }
        }
    }

    public void updateAnalytics() {
        double qty, price, total;
        String product = "unaccounted";
        price = 0; qty = 0; total = 0;
        int rowIndex = 0;
        Log.d(getResources().getString(R.string.logging), selectedRestaurant);
        for (ArrayList<String> row : recieptData) {
            int index = 0;
            if (rowIndex++ == 0) continue;
            for (String element : row) {
                index++;
                int itemIndex = index % 4;
                Log.d(getResources().getString(R.string.logging), Integer.valueOf(itemIndex).toString());
                Log.d(getResources().getString(R.string.logging), element);
                switch (itemIndex) {
                    case 1:
                        if (element.isEmpty()) { break;}
                        qty = Double.parseDouble(element.toString());
                        break;
                    case 2:
                        product = element.toString();
                        break;
                    case 3:
                        price = Double.parseDouble(element.toString().replace("$", ""));
                        break;
                    case 0:
                        total = Double.parseDouble(element.toString().replace("$", ""));
                        break;
                }

            }
            incrementCounter(product, total);
        }
    }

    public void incrementCounter(String product, final double amount) {
        String route = helpers.getRouteString(getActivity().getBaseContext());
        Log.d("logging", "Route:  " + route);
       Firebase dbValue = helpers.getFirebase().child("analytics").child(route).child(product);
        dbValue.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(amount);
                }
                else {
                    Log.d("logging", "current data: " + currentData.toString());
                    Log.d("logging", "current db value: " + currentData.getValue().toString());
                    Log.d("logging", "amount: " + Double.valueOf(amount).toString());
                    currentData.setValue((double) currentData.getValue() + amount);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (firebaseError != null) {
                    Log.d("logging", "Firebase counter increment failed.");
                } else {
                    Log.d("logging", "Firebase counter increment succeeded.");
                }
            }
        });
    }

    public void writeData ( String data ) {
        try {
            FileOutputStream fOut = getContext().openFileOutput("KRS_DATA.dat", getContext().MODE_PRIVATE) ;
            OutputStreamWriter osw = new OutputStreamWriter ( fOut ) ;
            osw.write ( data ) ;
            osw.flush ( ) ;
            osw.close ( ) ;
        } catch ( Exception e ) {
            e.printStackTrace ( ) ;
        }
    }

    public String readSavedData ( ) {
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = getContext().openFileInput("KRS_DATA.dat") ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return datax.toString() ;
    }
}
