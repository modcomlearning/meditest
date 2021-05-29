package adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.modcom.meditest.R;
import com.modcom.meditest.TestDescActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import helpers.StoreDatabase;
import models.ModelRecycler;

public class RetrofitAdapter1 extends RecyclerView.Adapter<RetrofitAdapter1.MyViewHolder> implements Filterable {

    private LayoutInflater inflater;
    private ArrayList<ModelRecycler> dataModelArrayList;
    private ArrayList<ModelRecycler> contactListFiltered;
    private StoreDatabase dbHelper;
    Context context;
    public RetrofitAdapter1(Context ctx, ArrayList<ModelRecycler> dataModelArrayList){

        inflater = LayoutInflater.from(ctx);
        this.dataModelArrayList = dataModelArrayList;
        this.contactListFiltered = dataModelArrayList;
        this.context = ctx;
    }

    @Override
    public RetrofitAdapter1.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.listcard1, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RetrofitAdapter1.MyViewHolder holder, int position) {

     try {

         holder.name.setText(contactListFiltered.get(position).getName());
         holder.city.setText(contactListFiltered.get(position).getCity());


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


        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.testName);
            city = (TextView) itemView.findViewById(R.id.testPrice);
           // iv = (ImageView) itemView.findViewById(R.id.imageView);

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