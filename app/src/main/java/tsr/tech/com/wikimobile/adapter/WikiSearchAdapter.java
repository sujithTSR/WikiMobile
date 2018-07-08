package tsr.tech.com.wikimobile.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tsr.tech.com.wikimobile.R;
import tsr.tech.com.wikimobile.models.Result;


public class WikiSearchAdapter extends ArrayAdapter<Result> {

    private ArrayList<Result> results_data;
    private Context mContext;

    public WikiSearchAdapter(@NonNull Context context, int resource, ArrayList<Result> results_data) {
        super(context, 0, results_data);
        this.results_data = results_data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View search_row = inflater.inflate(R.layout.search_row, parent, false);
        TextView sgst_title = search_row.findViewById(R.id.suggestion_title);
        ImageView sgst_img = search_row.findViewById(R.id.suggestion_image);
        TextView sgst_desc = search_row.findViewById(R.id.desc_text);
        Result sgst_result =  results_data.get(position);
        String sgst_url = sgst_result.getThumbnail();
        sgst_title.setText(sgst_result.getTitle());
        String desc = sgst_result.getDesc();
        if (desc==null) desc = "";
        else{
           if(desc.compareToIgnoreCase("")!=0 && desc.length()>2){
               desc = desc.substring(2,desc.length()-2);
           }
        }
        sgst_desc.setText(desc);
        if(sgst_url == null || sgst_url.compareToIgnoreCase("")==0 ){
            sgst_img.setImageResource(R.drawable.load_err);
            sgst_img.setPadding(2,2,2,2);
        }
        else{
            Picasso.with(mContext).load(sgst_url).into(sgst_img);
        }
        return search_row;
    }
}
