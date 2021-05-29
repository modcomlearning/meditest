package retrofit_api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;



public interface IRetrofit {

   // static String BASE_URL = "https://everydayapps.org/api/v1/";
    static String BASE_URL = "http://meditestdiagnostic.org/index.php/api/v1/";

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("user/register")
    Call<JsonObject> postRawJSON(@Body JsonObject jsonObject);

    @POST("user/login")
    Call<JsonObject> postRawLogin(@Body JsonObject jsonObject);

    @POST("user/add-dependant")
    Call<JsonObject> postRawAddDependants(@Body JsonObject jsonObject);


    @POST("user/dependants/{user_id}")
    Call<String> getStringYourDep(@Path("user_id") int id);

    @POST("user/book")
    Call<JsonObject> postRawBook(@Body JsonObject jsonObject);

    @POST("user/payment")
    Call<String> postRawPayment(@Body JsonObject jsonObject);

    @POST("user/forgot-password")
    Call<JsonObject> postRawRequestOTP(@Body JsonObject jsonObject);

    @POST("user/otp")
    Call<JsonObject> postRawSubmitOTP(@Body JsonObject jsonObject);

    @POST("user/new-password")
    Call<JsonObject> postRawChangePassword(@Body JsonObject jsonObject);

    @POST("user/logout")
    Call<JsonObject> postRawLogout();
}