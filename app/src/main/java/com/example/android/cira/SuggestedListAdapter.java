package com.example.android.cira;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class SuggestedListAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> adapter;
    SearchFragement fragement = null;


    public SuggestedListAdapter(Context context, int layoutResourceId, ArrayList<String> objects, SearchFragement frag) {
        super(context, layoutResourceId, objects);
        mContext = context;
        this.adapter = new ArrayList<>();
        this.adapter = objects;
        this.fragement = frag;
    }

    @Override
    public int getCount() {
        return adapter.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return adapter.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.suggested_item_list, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.contact = (TextView) row.findViewById(R.id.contact);
            holder.addContact = (ImageButton) row.findViewById(R.id.add_contact);
            holder.ignoreContact = (ImageButton) row.findViewById(R.id.ignore);
            row.setTag(holder);
        }

        ViewHolder viewHolder = (ViewHolder) row.getTag();
        viewHolder.contact.setText(getItem(position));
        viewHolder.contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// TODO: 26-02-2017 go to contact details activity
                Toast.makeText(mContext, "Go to details activity", Toast.LENGTH_LONG).show();
            }
        });

        viewHolder.addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragement.updateContact(position);
                fragement.removeContact(position);

            }
        });

        viewHolder.ignoreContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragement.removeContact(position);
            }
        });



        return row;
    }

    static class ViewHolder {
        public TextView contact;
        public ImageButton addContact;
        public ImageButton ignoreContact;
    }
}
