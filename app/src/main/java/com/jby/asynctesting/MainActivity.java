package com.jby.asynctesting;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    ArrayList<User> userArrayList;
    ListViewAdapter listViewAdapter;
    EditText editTextusername;
    private static final String TAG = "MainActivity";
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectInitialize();
        objectSetting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(DbContact.UI_UPDATE_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void objectInitialize() {
        lv = (ListView)findViewById(R.id.listview);
        editTextusername = (EditText)findViewById(R.id.username);
        userArrayList = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(this, userArrayList);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getAllContact();
            }
        };
    }

    private void objectSetting() {
       lv.setAdapter(listViewAdapter);
       getAllContact();
    }

    public void getAllContact(){
        userArrayList.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.fetchAll(database);
        while (cursor.moveToNext()){
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String status = cursor.getString(cursor.getColumnIndex("status"));
            userArrayList.add(new User(
                    username,
                    status));
        }
        listViewAdapter.notifyDataSetChanged();
        cursor.close();
        dbHelper.close();
    }

    public void insert(View view){
        final String username = editTextusername.getText().toString().trim();
        if(checkNetworkConnection())
        {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if(status.equals("1")){
                            sendToLocalServer(DbContact.USER_STATUS_SUCCESSFUL);
                            getAllContact();
                        }
                        else{
                            sendToLocalServer(DbContact.USER_STATUS_FAILED);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Failed To Send!", Toast.LENGTH_SHORT).show();
                    sendToLocalServer(DbContact.USER_STATUS_FAILED);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String , String> params = new HashMap<>();
                    params.put("username", username);
                    return params;
                }
            };
            MySingleton.getmInstance(this).addToRequestQueue(stringRequest);
        }
        else{
            sendToLocalServer(DbContact.USER_STATUS_FAILED);
            scheduleJob();
        }
    }

    public void sendToLocalServer(String status){
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String username = editTextusername.getText().toString().trim();
        dbHelper.saveContact(username, status, database);
        dbHelper.close();
        database.close();
        getAllContact();
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo !=  null && networkInfo.isConnected());
    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, NetworkMonitor.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    public void cancelJob(View v) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "Job cancelled");
    }

}
