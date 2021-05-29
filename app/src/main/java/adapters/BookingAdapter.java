package adapters;


import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.modcom.meditest.R;
import com.modcom.meditest.SingleBooking;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;
import android.content.Intent;
import android.widget.CompoundButton;

import java.util.Date;
import java.util.List;

import models.BookingModel;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.MyViewHolder> implements Filterable {
    private LayoutInflater inflater;
    private ArrayList<BookingModel> dataModelArrayList;
    private ArrayList<BookingModel> contactListFiltered;

    Context context;
    SharedPreferences shared;
    public BookingAdapter(Context ctx, ArrayList<BookingModel> dataModelArrayList){
        inflater = LayoutInflater.from(ctx);
        this.dataModelArrayList = dataModelArrayList;
        this.contactListFiltered = dataModelArrayList;
        this.context = ctx;
    }

    @Override
    public BookingAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_mybookings, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BookingAdapter.MyViewHolder holder, int position) {
//        Typeface typeface = Typeface.createFromAsset(
//                context.getAssets(),
//                "MarsMission-Zxax.ttf");
//        holder.txt_time.setTypeface(typeface);

         holder.txt_time.setText(contactListFiltered.get(position).getTime());
         String formatted_date = formatDate("yyyy-MM-dd", "EEEE, dd MMMM yyyy",contactListFiltered.get(position).getDate());
         holder.txt_date.setText(formatted_date);

        if (contactListFiltered.get(position).getPaid().equalsIgnoreCase("1")){
            holder.txt_paid.setText("Paid");
            holder.txt_paid.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.txt_total_amount.setText("View Details");

        }

        else if(contactListFiltered.get(position).getPaid().equalsIgnoreCase("0")) {
            holder.txt_paid.setText("Cash on Collection");
            holder.txt_total_amount.setText("Total. Ksh "+contactListFiltered.get(position).getTotal_amount());
            holder.txt_paid.setBackgroundColor(Color.parseColor("#FF5722"));
        }

        //holder.txt_total_amount.setText("Total. Ksh "+contactListFiltered.get(position).getTotal_amount());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shared = context.getSharedPreferences("mediprefs", Context.MODE_PRIVATE);
                Intent x = new Intent(context, SingleBooking.class);
             //   Bundle b = new Bundle();
                SharedPreferences.Editor editor = shared.edit();
                editor.putString("booking_id",contactListFiltered.get(position).getBooking_id());
                editor.putString("status",contactListFiltered.get(position).getStatus());
                editor.putString("paid",contactListFiltered.get(position).getPaid());
                editor.apply();
                context.startActivity(x);
            }
        });

    }
    public static String formatDate(String fromFormat, String toFormat, String dateToFormat) {
        SimpleDateFormat inFormat = new SimpleDateFormat(fromFormat);
        Date date = null;
        try {
            date = inFormat.parse(dateToFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat outFormat = new SimpleDateFormat(toFormat);

        return outFormat.format(date);
    }
    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView  txt_date,  txt_time, txt_paid, txt_total_amount;

        public MyViewHolder(View itemView) {
            super(itemView);
            txt_paid  = (TextView) itemView.findViewById(R.id.txt_paid);
            txt_total_amount = (TextView) itemView.findViewById(R.id.txt_book_amount);
            txt_date =  (TextView) itemView.findViewById(R.id.txt_date);
            txt_time =  (TextView) itemView.findViewById(R.id.txt_time);


        }

    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString = charSequence.toString();

                if (searchString.isEmpty()) {
                    contactListFiltered = dataModelArrayList;

                } else {

                    ArrayList<BookingModel> tempFilteredList = new ArrayList<>();
                    for (BookingModel user : dataModelArrayList) {

                        // search for user title
                        if (user.getBooking_id().toLowerCase().contains(searchString)) {

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
                contactListFiltered = (ArrayList<BookingModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}