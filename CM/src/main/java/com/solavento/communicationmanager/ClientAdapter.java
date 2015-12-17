package com.solavento.communicationmanager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;

public class ClientAdapter extends ArrayAdapter<Client> {
    static final long UPDATE_LIST_TIME = 60*1000;//10*60*1000;
    static final long CHECK_TIME = 30*1000;//2*60*60*1000;
    private int layoutResourceId;
    private Context context;
    private Handler handler = new Handler();

    public ClientAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;

        handler.postDelayed(runnable, UPDATE_LIST_TIME);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateList();
            handler.postDelayed(this, UPDATE_LIST_TIME);
        }
    };

    private void updateList(){
        Client client;
        for(int i=0 ; i<getCount() ; i++){
            client = getItem(i);
            if (Calendar.getInstance().getTime().getTime() - client.getLastCommunicationTime(). getTime()  > CHECK_TIME){
                remove(client);
            }
        }
    }

    public void insertClient(InetAddress inetAddress, String message) {
        Client client = new Client(inetAddress, 1234, "description: " + message);
        int position = this.getPosition(client);
        if (position == -1) {
            add(client);
        } else {
            client = this.getItem(position);
            client.updateLastCommunicationTime();
            client.setDescription("description: " + message);
            if (message.contains("Started") || message.contains("ImAlive")){
                client.setAlive(true);
            } else if (message.contains("Stopped")){
                client.setAlive(false);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        ClientViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new ClientViewHolder();
            holder.indicator = view.findViewById(R.id.indicator);
            holder.address = (TextView) view.findViewById(R.id.address);
            holder.description = (TextView) view.findViewById(R.id.description);
            holder.time = (TextView) view.findViewById(R.id.time);

            view.setTag(holder);
        } else {
            holder = (ClientViewHolder) view.getTag();
        }

        Client client = this.getItem(position);
        holder.indicator.setSelected(client.isAlive());
        holder.address.setText(client.getAddress());
        holder.description.setText(client.getDescription());
        holder.time.setText(client.getTime());

        return view;
    }

    static class ClientViewHolder {
        View indicator;
        TextView address;
        TextView description;
        TextView time;
    }
}
