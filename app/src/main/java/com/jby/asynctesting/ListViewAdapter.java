package com.jby.asynctesting;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<User> userArrayList;

    ListViewAdapter(Context context, ArrayList<User> userArrayList)
    {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @Override
    public int getCount() {
        return userArrayList.size();
    }

    @Override
    public User getItem(int i) {
        return userArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.list_view_layout, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        User object = getItem(i);
        viewHolder.username.setText(object.getUsername());
        String status = object.getStatus();
        if(status.equals(DbContact.USER_STATUS_FAILED))
            viewHolder.status.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.rotate));
        else
            viewHolder.status.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.checked));

        return view;
    }

    private static class ViewHolder{
        private TextView username;
        private ImageView status;

        ViewHolder (View view){
            username = (TextView)view.findViewById(R.id.username);
            status = (ImageView) view.findViewById(R.id.status);

        }
    }
}
