package com.vizorteam.krsbreadapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class AdminSalesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private String selectedRoute;
    private View fragmentView;
    private Spinner routesDropdown;
    private double salesTotal;
    private DataSnapshot dbSnapshot;
    private TableLayout salesTable;
    private ArrayList<ArrayList<String>> salesData;

    private final List<String> routesList = Arrays.asList("A","B","C","D","E","F","G","H");

    public AdminSalesFragment() {
    }

    public static AdminSalesFragment newInstance(String param1, String param2) {
        AdminSalesFragment fragment = new AdminSalesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        fragmentView = inflater.inflate(R.layout.fragment_admin_sales, container, false);
        initTable(fragmentView);

        routesDropdown = (Spinner) fragmentView.findViewById(R.id.route_spinner);

        ArrayAdapter<String> routesAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.spinner_item, routesList);

        routesDropdown.setAdapter(routesAdapter);
        routesDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                selectedRoute = (String) parent.getItemAtPosition(pos);
                updateData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button clearBtn = (Button) fragmentView.findViewById(R.id.btn_clear_sales);
        clearBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearAnalytics();
            }
        });

        Button btnPrint = (Button) fragmentView.findViewById(R.id.btn_print);
        btnPrint.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PrintSales(getContext(), helpers.portName, helpers.portSettings);
            }
        });

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
        TextView tv2 = new TextView(getContext());
        tv1.setText("Qty");
        tv1.setTextColor(txtColor);
        tv1.setGravity(Gravity.CENTER);
        tv2.setText("Sales");
        tv2.setTextColor(txtColor);
        tv2.setGravity(Gravity.CENTER);
        tbrow0.addView(tv1);
        tbrow0.addView(tv2);
        salesTable.addView(tbrow0, pRowTop);
    }

    private void updateData() {
        salesTable.removeAllViews();
        int txtColor = Color.DKGRAY;
        TableRow header = new TableRow(getContext());
        TextView productColumn = new TextView(getContext());
        TextView qtyColumn = new TextView(getContext());
        TextView salesColumn = new TextView(getContext());
        productColumn.setText("Product");
        productColumn.setTextColor(txtColor);
        productColumn.setGravity(Gravity.LEFT);
        qtyColumn.setText("Qty");
        qtyColumn.setTextColor(txtColor);
        qtyColumn.setGravity(Gravity.CENTER);
        salesColumn.setText("Sales");
        salesColumn.setTextColor(txtColor);
        salesColumn.setGravity(Gravity.CENTER);
        header.addView(productColumn);
        header.addView(qtyColumn);
        header.addView(salesColumn);
        salesTable.addView(header);

        HashMap<String, HashMap> restaurantProducts = null;
        if (dbSnapshot != null && selectedRoute != null) {
            restaurantProducts = (HashMap) dbSnapshot.child(selectedRoute).getValue();
        }
        if (restaurantProducts == null) {
            return;
        }

        salesTotal = 0;
        for (String id : restaurantProducts.keySet()) {
            String productName = restaurantProducts.get(id).get("name").toString();
            String productQuantity = restaurantProducts.get(id).get("qty").toString();
            String productSales = "$" + String.format("%.2f", (double) restaurantProducts.get(id).get("sales"));
            int i = 1;
            TableRow tbrow = new TableRow(getContext());
            TextView tv1 = new TextView(getContext());
            TextView tv2 = new TextView(getContext());
            TextView tv3 = new TextView(getContext());
            tv1.setTextColor(Color.DKGRAY);
            tv1.setGravity(Gravity.LEFT);
            tv1.setText(productName);
            tv2.setTextColor(Color.DKGRAY);
            tv2.setGravity(Gravity.CENTER);
            tv2.setText(productQuantity);
            tv3.setTextColor(Color.DKGRAY);
            tv3.setGravity(Gravity.CENTER);
            tv3.setText(productSales);
            tbrow.addView(tv1);
            tbrow.addView(tv2);
            tbrow.addView(tv3);
            salesTable.addView(tbrow);
            salesTotal += Double.parseDouble(restaurantProducts.get(id).get("sales").toString());
        }
        ((TextView)fragmentView.findViewById(R.id.sales_tatal)).setText("Sales Total $" + String.format("%.2f", salesTotal));
    }

    private void clearAnalytics() {
        helpers.getFirebase().child("analytics").child(selectedRoute).setValue(null);
        salesTotal = 0;
        ((TextView)fragmentView.findViewById(R.id.sales_tatal)).setText("Sales Total $" + String.format("%.2f", salesTotal));
    }

    public boolean PrintSales(Context context, String portName, String portSettings) {
        loopTable();


            ArrayList<byte[]> list = new ArrayList<byte[]>();

            byte[] outputByteBuffer = null;
            String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());

            list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)

            list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)

            list.add(("\nKRS Bread Distributor\n" + "8300 Hall Road Ste. 200\n" + "Utica, MI 48317\n").getBytes());
            list.add(("(586)489-2454  (248)840-7292\n\n").getBytes());

            list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment

            list.add(new byte[] { 0x1b, 0x44, 0x02, 0x22, 0x00 }); // Setting Horizontal Tab

            list.add(("Date: " + date + " ").getBytes());


            list.add(new byte[]{0x1b, 0x45, 0x00}); // Set Emphasized Printing OFF (same command as on)

            list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Alignment
            list.add(("\n\nsales").getBytes());

            list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment
            list.add(("\n------------------------------------------------\n\n").getBytes());

            for (ArrayList<String> row : salesData)
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
                            list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Right Alignment
                            list.add((element).getBytes());
                            break;
                    }
                }
                list.add(("\n").getBytes());
            }

            list.add(new byte[] { 0x1d, 0x21, 0x11 }); // Width and Height Character Expansion <GS> ! n

            list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Right Alignment
            list.add(("$" + String.format("%.2f", salesTotal) + "\n").getBytes());

            list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Expansion - Reference Star Portable Printer Programming Manual

            list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment
            list.add(("\n------------------------------------------------").getBytes());

            list.add("\n\n\n\n".getBytes());
            return MiniPrinterFunctions.sendCommand(context, portName, portSettings, list);
    }

    private void loopTable() {
        TableLayout table = (TableLayout) fragmentView.findViewById(R.id.sales_table);
        salesData = new ArrayList<ArrayList<String>>();
        int tableRowIndex = 0;
        for(int i = 0, j = table.getChildCount(); i < j; i++) {
            View view = table.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                salesData.add(new ArrayList<String>());
                for (int k = 0, l = row.getChildCount(); k < l; k++) {
                    View field = row.getChildAt(k);
                    String fieldValue = "";
                    switch (field.getClass().getSimpleName()){
                        case "TextView":
                            TextView textView = (TextView) field;
                            fieldValue= textView.getText().toString();
                            break;
                    }
                    salesData.get(tableRowIndex).add(fieldValue);
                }
                tableRowIndex++;
            }
        }
    }
}