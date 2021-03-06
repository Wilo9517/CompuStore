package com.fiuady.hadp.compustore;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fiuady.db.Category;
import com.fiuady.db.CompuStore;
import com.fiuady.db.Product;

import java.util.List;


public class AddProductToAssemblyActivity extends AppCompatActivity {

    public static final String EXTRA_PID = "com.fiuady.hadp.compustore.extra_pid";
    public static final int CODE_REQUEST = 1;
    private String QUERY = null;

    private class ProductHolder extends RecyclerView.ViewHolder {

        private TextView idtext, catidtext, desctext, pricetext, qtytext, qtytag;

        public ProductHolder(View itemView) {
            super(itemView);
            idtext = (TextView) itemView.findViewById(R.id.idPr_text);
            catidtext = (TextView) itemView.findViewById(R.id.categoryID_text);
            desctext = (TextView) itemView.findViewById(R.id.descriptionPr_text);
            pricetext = (TextView) itemView.findViewById(R.id.pricePr_text);
            qtytag = (TextView) itemView.findViewById(R.id.qty_tag);
            qtytext = (TextView) itemView.findViewById(R.id.qty_text);
        }

        private void bindProduct(final Product product) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popupMenu = new PopupMenu(AddProductToAssemblyActivity.this, itemView);
                    popupMenu.getMenuInflater().inflate(R.menu.add_prodtoassem, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent data = new Intent();
                            data.putExtra(EXTRA_PID, product.getId());
                            setResult(RESULT_OK,data);
                            finish();
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
            idtext.setText(String.valueOf(product.getId()));
            catidtext.setText(String.valueOf(product.getCategory_id()));
            pricetext.setText(String.valueOf(product.getPrice()));
            qtytext.setText(String.valueOf(product.getQuantity()));
            desctext.setText(product.getDescription());
            qtytag.setText("Cantidad Disponible: ");
        }
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductHolder> {

        private List<Product> products;

        public ProductAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_products, parent, false);
            return new ProductHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductHolder holder, int position) {
            holder.bindProduct(products.get(position));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }
    }

    private CompuStore compustore;
    private RecyclerView recyclerview;
    private ProductAdapter adapter;
    private Spinner spinner;
    private ImageButton search;
    private EditText searchtext;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        relativeLayout = (RelativeLayout) findViewById(R.id.rl_products);
        relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorAssembliesbg));

        spinner = (Spinner) findViewById(R.id.spinnerCat);
        search = (ImageButton) findViewById(R.id.buscarCat_button);
        searchtext = (EditText) findViewById(R.id.busquedaCat_text);
        compustore = new CompuStore(this);

        recyclerview = (RecyclerView) findViewById(R.id.productos_rv);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        arrayAdapter.add("Todas");
        final List<Category> categories = compustore.getAllCategoriesById();
        for (Category category : categories) {
            arrayAdapter.add(category.getDescription());
        }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (searchtext.getText()!= null){
                //    if (spinner.getSelectedItemPosition() != 0){
                //        adapter = new ProductAdapter(compustore.getProductByName(compustore.getOneCategory(spinner.getSelectedItem().toString()), String.valueOf(searchtext.getText())));
                //        recyclerview.setAdapter(adapter);
                //    }else{
                //        adapter = new ProductAdapter(compustore.getAllProductsByName(String.valueOf(searchtext.getText())));
                //        recyclerview.setAdapter(adapter);
                //    }
                //}
                //else{
                //    if (spinner.getSelectedItemPosition() != 0){
                //        adapter = new ProductAdapter(compustore.getAllProductsById(compustore.getOneCategory(spinner.getSelectedItem().toString())));
                //        recyclerview.setAdapter(adapter);
                //    }else{
                //        adapter = new ProductAdapter(compustore.getAllProducts());
                //        recyclerview.setAdapter(adapter);
                //    }
                //}
                searchtext.setHint(String.valueOf(searchtext.getText()));
                QUERY = searchtext.getText().toString();
                UpdateAdapter();
                searchtext.setText(null);
            }
        });
    }

    public void UpdateAdapter(){
        if (QUERY!= null){
            if (spinner.getSelectedItemPosition() != 0){
                adapter = new ProductAdapter(compustore.getProductByName(compustore.getOneCategory(spinner.getSelectedItem().toString()), String.valueOf(searchtext.getText())));
                recyclerview.setAdapter(adapter);
            }else{
                adapter = new ProductAdapter(compustore.getAllProductsByName(String.valueOf(searchtext.getText())));
                recyclerview.setAdapter(adapter);
            }
        }
        else{
            if (spinner.getSelectedItemPosition() != 0){
                adapter = new ProductAdapter(compustore.getAllProductsById(compustore.getOneCategory(spinner.getSelectedItem().toString())));
                recyclerview.setAdapter(adapter);
            }else{
                adapter = new ProductAdapter(compustore.getAllProducts());
                recyclerview.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
