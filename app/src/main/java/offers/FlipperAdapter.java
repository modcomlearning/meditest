package offers;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.modcom.meditest.R;

import java.util.ArrayList;

/**
 * Created by belal on 19/4/17.
 */

public class FlipperAdapter extends BaseAdapter {
    private Context mCtx;
    private ArrayList<Hero> heros;

    public FlipperAdapter(Context mCtx, ArrayList<Hero> heros){
        this.mCtx = mCtx;
        this.heros = heros;
    }
    @Override
    public int getCount() {
        return heros.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Hero hero = heros.get(position);

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.flipper_items, null);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        textView.setText(hero.getName());

        Glide.with(mCtx).load(hero.getUrl()).into(imageView);
        return view;
    }
}