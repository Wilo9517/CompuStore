package com.fiuady.hadp.compustore;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fiuady.db.Assembly;
import com.fiuady.db.Client;
import com.fiuady.db.CompuStore;
import com.fiuady.db.OrderAssembly;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by jessm on 18/04/2017.
 */


public class AddOrderActivity extends AppCompatActivity {

    private static final String KEY_POSITION = "position";
    public static final int REQUEST_CODE = 1;

    private int position;

    public static class mDialogFragment extends DialogFragment {

        private int Num, Qty, Aid;
        private String Desc;
        private NumberPicker picker;

        static mDialogFragment newInstance(int num, int qty, int aid) {
            mDialogFragment dF = new mDialogFragment();
            Bundle args = new Bundle();
            args.putInt("num", num);
            args.putInt("aid", aid);
            args.putInt("qty", qty);
            dF.setArguments(args);
            return dF;
        }

        static mDialogFragment newInstance2(int num, String description, int aid) {
            mDialogFragment dF = new mDialogFragment();
            Bundle args = new Bundle();
            args.putInt("num", num);
            args.putString("desc", description);
            args.putInt("aid", aid);
            dF.setArguments(args);
            return dF;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Num = getArguments().getInt("num");
            switch (Num) {
                case 0:
                    Qty = getArguments().getInt("qty");
                    Aid = getArguments().getInt("aid");
                    return DialogMod();
                case 1:
                    Desc = getArguments().getString("desc");
                    Aid = getArguments().getInt("aid");
                    return DialogErase();
            }
            return super.onCreateDialog(savedInstanceState);
        }

        public AlertDialog DialogMod() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.tag_cantidad));
            picker = new NumberPicker(getActivity());
            picker.setMinValue(1);
            picker.setMaxValue(Qty + 10);
            picker.setValue(Qty + 1);
            //compustore = new CompuStore(getActivity());

            builder.setCancelable(false);
            builder.setNegativeButton(R.string.texto_cancelar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }).setPositiveButton(R.string.texto_guardar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((AddOrderActivity) getActivity()).UpdateAssemblie(Aid, picker.getValue());
                    //compustore.AddQtyProductInAssembly(Pid, Id, picker.getValue());
                    ((AddOrderActivity) getActivity()).UpdateAdapter();
                    Toast.makeText(getActivity(), R.string.Confirma_operacion, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setView(picker);
            return builder.create();
        }

        public AlertDialog DialogErase() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.Elimina_ensamble));
            builder.setMessage("El siguiente ensamble será eliminado de la orden: " + Desc);
            //compustore = new CompuStore(getActivity());

            builder.setNegativeButton(R.string.texto_cancelar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }).setPositiveButton(R.string.texto_eliminar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((AddOrderActivity) getActivity()).DeleteAssembly(Aid);
                    //compustore.DeleteProductInAssembly(Pid,Id);
                    ((AddOrderActivity) getActivity()).UpdateAdapter();
                    Toast.makeText(getActivity(), R.string.Confirma_operacion, Toast.LENGTH_SHORT).show();
                }
            });
            return builder.create();
        }
    }

    private class AssemblyHolder extends RecyclerView.ViewHolder {

        private TextView qtytag, qtyPrtext, Desctext;

        public AssemblyHolder(View itemView) {
            super(itemView);
            qtytag = (TextView) itemView.findViewById(R.id.idAs_tag);
            qtyPrtext = (TextView) itemView.findViewById(R.id.idAs_text);
            Desctext = (TextView) itemView.findViewById(R.id.descriptionAs_text);


        }

        private void bindOrder(final Assembly assembly) {

            qtytag.setText(R.string.tag_cantidad);
            for (OrderAssembly orderAssembly : orderassemblies) {
                if (orderAssembly.getAssembly_id() == assembly.getId()) {
                    qtyPrtext.setText(String.valueOf(orderAssembly.getQty()));
                }
            }
            Desctext.setText((assembly.getDescripcion()));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu menu3 = new PopupMenu(AddOrderActivity.this, itemView);
                    menu3.getMenuInflater().inflate(R.menu.menu_option_catassem, menu3.getMenu());
                    menu3.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals(menu3.getMenu().getItem(0).getTitle())) {
                                mDialogFragment fragment = mDialogFragment.newInstance(0, Integer.valueOf(qtyPrtext.getText().toString()), assembly.getId());
                                fragment.show(getFragmentManager(), "QtyDialog");
                            } else {
                                mDialogFragment fragment = mDialogFragment.newInstance2(1, assembly.getDescripcion(), assembly.getId());
                                fragment.show(getFragmentManager(), "EraseDialog");
                            }
                            return true;
                        }
                    });
                    menu3.show();
                }
            });
        }
    }

    private class AssemblyAdapter extends RecyclerView.Adapter<AssemblyHolder> {

        private List<Assembly> orders;

        public AssemblyAdapter(List<Assembly> orders) {
            this.orders = orders;
        }

        @Override
        public AssemblyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_assembly, parent, false);
            return new AssemblyHolder(view);
        }

        @Override
        public void onBindViewHolder(AssemblyHolder holder, int position) {
            holder.bindOrder(orders.get(position));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }
    }

    private CompuStore compustore;

    private RecyclerView recyclerview;
    private AssemblyAdapter adapter;
    private Spinner spinner_clients;
    private ArrayList<OrderAssembly> orderassemblies;
    private ArrayList<Assembly> assemblies;
    private Button btnsave, btncancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.new_order);
        super.onCreate(savedInstanceState);
        compustore = new CompuStore(this);

        recyclerview = (RecyclerView) findViewById(R.id.clients_rv);
        btncancel = (Button) findViewById(R.id.Btn_cancelarA);
        btnsave = (Button) findViewById(R.id.Btn_guardarA);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerview.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerview.setLayoutManager(new GridLayoutManager(this, 2));
        }

        if (savedInstanceState != null) {
            orderassemblies = new ArrayList<>(compustore.RestoreOrderAssemblies());
            assemblies = new ArrayList<>();
            for (Assembly assembly : compustore.getAllAssemblies()) {
                for (OrderAssembly orderAssembly : orderassemblies) {
                    if (assembly.getId() == orderAssembly.getAssembly_id()) {
                        assemblies.add(assembly);
                    }
                }
            }
            position = 0;
        } else {
            orderassemblies = new ArrayList<>();
            assemblies = new ArrayList<>();
        }

        spinner_clients = (Spinner) findViewById(R.id.spinner_clients);

        UpdateAdapter();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        spinner_clients.setAdapter(arrayAdapter);

        final List<Client> clients = compustore.getAllClients();
        for (Client client : clients) {
            arrayAdapter.add(client.getFirst_name() + " " + client.getLast_name());
        }

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = -1;
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date date = new Date(year - 1900, month, day);
                String Date = sdf.format(date);
                id = compustore.InsertOrder(spinner_clients.getSelectedItem().toString(), Date);
                for (Assembly assembly : assemblies) {
                    for (OrderAssembly orderAssembly : orderassemblies) {
                        if (orderAssembly.getAssembly_id() == assembly.getId()) {
                            compustore.InsertOrderAssembly(id, assembly.getId(), orderAssembly.getQty());
                            break;
                        }
                    }
                }
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.agregar, menu);
        return true;
    }
  /*  @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerview.setLayoutManager(new LinearLayoutManager(this));
        }
        else {
            recyclerview.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(AddOrderActivity.this, AddAssemblyToOrderActivity.class);
        startActivityForResult(i, AddAssemblyToOrderActivity.REQUEST_CODE);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            boolean repeated = false;
            for (OrderAssembly assembly : orderassemblies) {
                if (assembly.getAssembly_id() == data.getIntExtra(AddAssemblyToOrderActivity.EXTRA_AID, -1)) {
                    Toast.makeText(AddOrderActivity.this, "El ensamble ya se encuentra en la orden", Toast.LENGTH_SHORT).show();
                    repeated = true;
                    break;
                }
            }
            if (!repeated) {
                for (Assembly assembly : compustore.getAllAssemblies()) {
                    if (assembly.getId() == data.getIntExtra(AddAssemblyToOrderActivity.EXTRA_AID, -1)) {
                        OrderAssembly oa = new OrderAssembly(0, assembly.getId(), 1);
                        assemblies.add(assembly);
                        orderassemblies.add(oa);
                        break;
                    }
                }
            }
            UpdateAdapter();
        }
        if(position ==1){
            compustore.RestoreOrderAssemblies();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        compustore.SaveOrderAssemblies(orderassemblies);
        position=1;
        super.onSaveInstanceState(outState);
    }

    public void UpdateAdapter() {
        Collections.sort(assemblies, new Comparator<Assembly>() {
            @Override
            public int compare(Assembly o1, Assembly o2) {
                return o1.getDescripcion().compareTo(o2.getDescripcion());
            }
        });
        adapter = new AssemblyAdapter(assemblies);
        recyclerview.setAdapter(adapter);
    }

    public void DeleteAssembly(int aid) {
        int erase = -1;
        for (Assembly assembly : assemblies) {
            if (assembly.getId() == aid) {
                erase = assemblies.indexOf(assembly);
            }
        }
        assemblies.remove(erase);
        for (OrderAssembly orderAssembly : orderassemblies) {
            if (orderAssembly.getAssembly_id() == aid) {
                erase = orderassemblies.indexOf(orderAssembly);
            }
        }
        orderassemblies.remove(erase);
    }

    public void UpdateAssemblie(int aid, int qty) {
        for (OrderAssembly orderAssembly : orderassemblies) {
            if (orderAssembly.getAssembly_id() == aid) {
                orderAssembly.setQty(qty);
            }
        }
    }

}


