package adapters;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import android.widget.Filter;
import android.widget.Toast;
import android.content.Intent;

import com.modcom.meditest.ConfirmBooking;
import com.modcom.meditest.NewHome;
import com.modcom.meditest.Others;
import com.modcom.meditest.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import helpers.StoreDatabase;
import models.DepModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;

import static android.content.Context.MODE_PRIVATE;

public class Dep_Adapter extends RecyclerView.Adapter<Dep_Adapter.MyViewHolder> implements Filterable {

    private LayoutInflater inflater;
    private ArrayList<DepModel> dataModelArrayList;
    private ArrayList<DepModel> contactListFiltered;
    private StoreDatabase dbHelper;
    Context context;
    Activity context1;
    String x;
    public Dep_Adapter(Context ctx, ArrayList<DepModel> dataModelArrayList){

        inflater = LayoutInflater.from(ctx);
        this.dataModelArrayList = dataModelArrayList;
        this.contactListFiltered = dataModelArrayList;
        this.context = ctx;

    }

    @Override
    public Dep_Adapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.dep_list, parent, false);
        Dep_Adapter.MyViewHolder holder = new Dep_Adapter.MyViewHolder(view);
        return holder;
    }
    SharedPreferences shared;
    @Override
    public void onBindViewHolder(Dep_Adapter.MyViewHolder holder, int position) {
        holder.name.setText(contactListFiltered.get(position).getFirst_name());
        holder.relationship.setText(contactListFiltered.get(position).getRelationship());
        holder.deleting.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(context, "ID "+contactListFiltered.get(position).getId(), Toast.LENGTH_SHORT).show();
                Intent x = new Intent(context, ConfirmBooking.class);
                x.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shared = context.getSharedPreferences("mediprefs", MODE_PRIVATE);
                SharedPreferences.Editor editor  = shared.edit();
                editor.putBoolean("self", false);
                editor.putString("dependant_id", contactListFiltered.get(position).getId());
                editor.putString("dep_first_name", contactListFiltered.get(position).getFirst_name());
                editor.putString("relationship", contactListFiltered.get(position).getRelationship());
                editor.apply();
                context.startActivity(x);
            }
        });


        holder.buuton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.deleting.setVisibility(View.VISIBLE);
                holder.deleting.setText("Deleting....");
                 fetchJSON(contactListFiltered.get(position).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView name, relationship, deleting;
        Button buuton2;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tvName);
            relationship = (TextView) itemView.findViewById(R.id.tvRelationship);
            buuton2 = (Button) itemView.findViewById(R.id.button2);
            deleting = (TextView) itemView.findViewById(R.id.deleting);
            //checkbox click event handling

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

                    ArrayList<DepModel> tempFilteredList = new ArrayList<>();
                    for (DepModel user : dataModelArrayList) {

                        // search for user title
                        if (user.getFirst_name().toLowerCase().contains(searchString)) {

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
                contactListFiltered = (ArrayList<DepModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }



    public void fetchJSON(String x){
        shared = context.getSharedPreferences("mediprefs", MODE_PRIVATE);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {

                String token = shared.getString("token","");
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer "+token)
                        //  .addHeader("Authorization", "Bearer 107|4fRJslzbfcbPyu9hlhKnTeWHRgY7MHK5PVpkFsUL")
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

//        ProgressDialog dialog = new ProgressDialog(context1);
//        dialog.setTitle("Retrieving TestDescription..");
//        dialog.setMessage("Please wait..");
//        dialog.setMax(100);
//        dialog.show();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RecyclerInterface.JSONURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();

        RecyclerInterface api = retrofit.create(RecyclerInterface.class);

        Call<String> call = api.getStringRemove(Integer.parseInt(x));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
               // Log.i("Responsestring", response.body().toString());
                //Toast.makeText()
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            // dialog.dismiss();
                            // Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                            // Log.i("onSuccess", response.body().toString());
                            String jsonresponse = response.body().toString();
                            JSONObject obj = new JSONObject(jsonresponse);

                            try {

                                //for (int i = 0; i < dataArray.length(); i++) {
                                int status_code = Integer.parseInt(obj.getString("status_code"));
                                if (status_code == 200){
                                    Toast.makeText(context, "Record Deleted, Pull to refresh", Toast.LENGTH_SHORT).show();
                                        Intent x = new Intent(context, Others.class);
                                        x.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(x);
                                }

                                else{
                                    Toast.makeText(context, "Record Not deleted", Toast.LENGTH_SHORT).show();
                                    Intent x = new Intent(context, Others.class);
                                    x.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(x);

                                }
                                //  }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Error!, Try again", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error!, Try again", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        //dialog.dismiss();
                        Toast.makeText(context, "Error!, Try again", Toast.LENGTH_SHORT).show();
                       // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // dialog.dismiss();
                Toast.makeText(context, "There was a server error, try again", Toast.LENGTH_SHORT).show();


            }
        });
    }



}