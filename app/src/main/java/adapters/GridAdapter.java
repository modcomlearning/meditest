package adapters;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.modcom.meditest.R;

public class GridAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] maintitle;
    private final Integer[] imgid;

    public GridAdapter(Activity context, String[] maintitle, Integer[] imgid) {
        super(context, R.layout.gridview_custom_layout, maintitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.maintitle=maintitle;
        this.imgid=imgid;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.gridview_custom_layout, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.gridview_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.gridview_image);


        titleText.setText(maintitle[position]);
        imageView.setImageResource(imgid[position]);
    ;

        return rowView;

    };
}
