package offers;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by belal on 19/4/17.
 */

public class Heroes {

    @SerializedName("offers")
    private ArrayList<Hero> heros;

    public Heroes(){

    }

    public ArrayList<Hero> getHeros(){
        return heros;
    }
}