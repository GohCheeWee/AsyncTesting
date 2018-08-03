package com.jby.asynctesting;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class NetworkMonitor extends JobService{

    private static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        final DbHelper dbHelper = new DbHelper(this);
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.getContact(database);

        while (cursor.moveToNext()) {
            String status = cursor.getString(cursor.getColumnIndex("status"));
            if (status.equals(DbContact.USER_STATUS_FAILED)) {
                final String name = cursor.getString(cursor.getColumnIndex("username"));

                StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContact.URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("1")) {
                                dbHelper.updateLocalDatabase(name, DbContact.USER_STATUS_SUCCESSFUL, database);
                                sendBroadcast(new Intent(DbContact.UI_UPDATE_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", name);
                        return params;
                    }

                };
                MySingleton.getmInstance(this).addToRequestQueue(stringRequest);
            }
            jobFinished(params, false);
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}
