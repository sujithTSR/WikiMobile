package tsr.tech.com.wikimobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tsr.tech.com.wikimobile.adapter.WikiSearchAdapter;
import tsr.tech.com.wikimobile.models.Result;
import tsr.tech.com.wikimobile.remote.MyApplication;
import tsr.tech.com.wikimobile.remote.SuggestionsService;

public class MainActivity extends AppCompatActivity {

    public static final String SUGGESTIONS = "FetchSuggestions";
    private static final String TAG = "MainActivity" ;
    private ArrayList<Result> mNewData, mOldData;
    ListView sgst_list;
    EditText editText;
    WikiSearchAdapter searchAdapter;
    LinearLayout search_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.edit_text);
        registerMyReceiver();
        search_view = findViewById(R.id.search_layout);
        mNewData = new ArrayList<>();
        mOldData = new ArrayList<>();
        sgst_list = findViewById(R.id.sgst_list);
        searchAdapter = new WikiSearchAdapter(this, R.id.sgst_list, mNewData);
        sgst_list.setAdapter(searchAdapter);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                mNewData.clear();
                searchAdapter.clear();
                String search_tag = editText.getText().toString();
                if(search_tag == null  || search_tag.equals("")){
                    mOldData.clear();
                    mNewData.clear();
                    searchAdapter.notifyDataSetChanged();
                }
                else{
                    startServiceForSuggestion(search_tag);
                }
            }
        });

        sgst_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Result result = getItem(position);
                if(result!=null){
                    fetchFull_URL(result.getPageId());
                }
            }
        });
    }


    private Result getItem(int position) {
        if(mNewData.size()>=position){
           return mNewData.get(position);
        }
        else{
            return null;
        }
    }

    private void startServiceForSuggestion(String search_tag) {
        if (!isNetworkAvailable()){
            Toast.makeText(this, "No Network Available",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            Intent intent1 = new Intent(this, SuggestionsService.class);
            intent1.putExtra("search_tag", search_tag);
            startService(intent1);
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    private void registerMyReceiver() {
        try
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SUGGESTIONS);
            registerReceiver(mServiceReciever, intentFilter);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private BroadcastReceiver mServiceReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            getResultArray(response);
            mNewData.clear();
            mNewData.addAll(mOldData);
            searchAdapter.notifyDataSetChanged();
        }
    };

    private ArrayList<Result> getResultArray(String response) {
        mOldData.clear();
        try {
            Log.v("Response",response);
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONObject("query").getJSONArray("pages");
            for (int i=0;i< jsonArray.length(); i++){
                Result result = new Result();
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                String page_id = jsonObject1.getString("pageid");
                String thumbnail = "";
                String title = jsonObject1.getString("title");
                try {
                    JSONObject thumbnail_obj = jsonObject1.getJSONObject("thumbnail");
                    thumbnail = thumbnail_obj.getString("source");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                String desc = "";
                try{
                    desc = jsonObject1.getJSONObject("terms").getString("description");
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                result.setPageId(page_id);
                result.setTitle(title);
                result.setThumbnail(thumbnail);
                result.setDesc(desc);
                mOldData.add(result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mOldData;
    }

    private void fetchFull_URL(final String pageId) {
        String url = getString(R.string.page_url) + "&prop=info&inprop=url&format=json&pageids=" + pageId;
        Log.v(TAG, url);
        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String page_url = response.getJSONObject("query").getJSONObject("pages").getJSONObject(pageId).getString("fullurl");
                    Intent browser_intent = new Intent(MainActivity.this, WebPage.class);
                    browser_intent.putExtra("page_url",page_url);
                    startActivity(browser_intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Unable to Find Page", Toast.LENGTH_SHORT).show();
            }
        });
        MyApplication.getInstance().addToRequestQueue(jsonObjectRequest2);

    }
}
