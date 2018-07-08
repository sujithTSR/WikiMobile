package tsr.tech.com.wikimobile.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONObject;

import tsr.tech.com.wikimobile.R;

public class SuggestionsService extends Service {

    private String mSearch_Tag = "";
    private Context mContext;
    private static String TAG = "SuggestionsService";
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            mSearch_Tag = intent.getStringExtra("search_tag");
            if(mSearch_Tag!=null && mSearch_Tag.compareToIgnoreCase("")!=0){
                FetchDataAsync data = new FetchDataAsync();
                data.execute();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class FetchDataAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            String url = getString(R.string.base_url) + "&formatversion=2&generator=prefixsearch&gpslimit=10&prop=pageimages%7Cpageterms&piprop=thumbnail&pithumbsize=70&pilimit=10&redirects=&wbptterms=description&format=json&gpssearch=" + mSearch_Tag;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG,response.toString());
                    sendBroadcast(new Intent("FetchSuggestions").putExtra("response",response.toString()));
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext, "Unable to Fetch Suggestions", Toast.LENGTH_SHORT).show();
                }
            });
            MyApplication.getInstance().addToRequestQueue(jsonObjectRequest);
            return null;
        }

    }

}
