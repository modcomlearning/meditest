package retrofit_api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecyclerInterface {

    //String JSONURL = "https://everydayapps.org/api/v1/";
    String JSONURL = "http://meditestdiagnostic.org/index.php/api/v1/";
    @GET("user/services")
    Call<String> getString();

    @GET("user/services/{id}")
    Call<String> getStringSingle(@Path("id") int id);

    @POST("user/dependants/{id}")
    Call<String> getStringYourDep(@Path("id") int id);

    @POST("user/remove-dependant/{id}")
    Call<String> getStringRemove(@Path("id") int id);

    @GET("user/bookings/{id}")
    Call<String> getStringMyBookings(@Path("id") int id);

    @GET("user/booking/{id}")
    Call<String> getStringSingleBooking(@Path("id") int id);


    @GET("user/phlebo-assigned-details/{id}")
    Call<String> getStringSinglePhlebo(@Path("id") int id);

    @GET("user/phlebo-get-current/{id}")
    Call<String> getStringSinglePhleboLocation(@Path("id") int id);
}
