package offers;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by belal on 19/4/17.
 */

public interface APIService {

    @GET("heroes.php")
    Call<Heroes> getHeroes();
}