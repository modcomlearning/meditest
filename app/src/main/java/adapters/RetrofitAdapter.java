package adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.modcom.meditest.R;
import com.modcom.meditest.TestDescActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import android.widget.Filter;
import android.content.Intent;
import android.widget.Toast;

import helpers.StoreDatabase;
import models.ModelRecycler;

public class RetrofitAdapter extends RecyclerView.Adapter<RetrofitAdapter.MyViewHolder> implements Filterable {

    private LayoutInflater inflater;
    private ArrayList<ModelRecycler> dataModelArrayList;
    private ArrayList<ModelRecycler> contactListFiltered;
    private StoreDatabase dbHelper;
    Context context;
    public RetrofitAdapter(Context ctx, ArrayList<ModelRecycler> dataModelArrayList){

        inflater = LayoutInflater.from(ctx);
        this.dataModelArrayList = dataModelArrayList;
        this.contactListFiltered = dataModelArrayList;
        this.context = ctx;
    }

    @Override
    public RetrofitAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.listcard, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RetrofitAdapter.MyViewHolder holder, int position) {

     try {
         Picasso.get().load(contactListFiltered.get(position).getImgURL()).into(holder.iv);
         holder.name.setText(contactListFiltered.get(position).getName());
         holder.city.setText("KES " + contactListFiltered.get(position).getCity());
         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 // Toast.makeText(context, "ID "+contactListFiltered.get(position).getId(), Toast.LENGTH_SHORT).show();
                 Intent x = new Intent(context, TestDescActivity.class);
                 Bundle b = new Bundle();
                 b.putString("id", contactListFiltered.get(position).getId());
                 x.putExtras(b);
                 context.startActivity(x);
             }
         });

     }
     catch (NullPointerException e){
         Toast.makeText(context, "Server processing Error!", Toast.LENGTH_SHORT).show();
     }

    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView name, city;
        ImageView iv;
        CheckBox add_cart;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tvTitle);
            city = (TextView) itemView.findViewById(R.id.tvDescription);
            iv = (ImageView) itemView.findViewById(R.id.imageView);

            //checkbox click event handling

        }

    }

//    @Override
//    public Filter getFilter() {
//        return exampleFilter;
//    }
//    private Filter exampleFilter = new Filter() {
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//            ArrayList<ModelRecycler> filteredList = new ArrayList<>();
//            if (constraint == null || constraint.length() == 0) {
//                filteredList.addAll(dataModelArrayList);
//            } else {
//                String filterPattern = constraint.toString().toLowerCase().trim();
//                for (ModelRecycler item : dataModelArrayList) {
//                    if (item.getName().toLowerCase().contains(filterPattern)) {
//                        filteredList.add(item);
//                    }
//                }
//            }
//            FilterResults results = new FilterResults();
//            results.values = filteredList;
//            return results;
//        }
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            dataModelArrayList.clear();
//            dataModelArrayList.addAll((ArrayList) results.values);
//            notifyDataSetChanged();
//        }
//    };

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString = charSequence.toString();

                if (searchString.isEmpty()) {
                    contactListFiltered = dataModelArrayList;

                } else {

                    ArrayList<ModelRecycler> tempFilteredList = new ArrayList<>();
                    for (ModelRecycler user : dataModelArrayList) {

                        // search for user title
                        if (user.getName().toLowerCase().contains(searchString)) {

                            tempFilteredList.add(user);
                        }
                    }
                    contactListFiltered = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //contactListFiltered.clear();
                contactListFiltered = (ArrayList<ModelRecycler>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}