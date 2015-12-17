package com.solavento.communicationmanager;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private TextView ipTextView;
    private ServerSocket serverSocket;
    private ClientAdapter adapter;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipTextView = (TextView) findViewById(R.id.ipTextView);

        ListView dynamicList = (ListView) findViewById(R.id.dynamicList);
        adapter = new ClientAdapter(this, R.layout.list_item_client);
        dynamicList.setAdapter(adapter);

        new Thread(new UDPTransmitter()).start();

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIp() {
        return Formatter.formatIpAddress(((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress());
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private class SocketServerThread extends Thread {
        static final int SocketServerPORT = 9000;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        ipTextView.setText(getIp() + " : " + serverSocket.getLocalPort());
                    }
                });


                while (true) {
                    final Socket socket = serverSocket.accept();

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    InputStream inputStream = socket.getInputStream();
                    message = "";
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                        message += byteArrayOutputStream.toString("UTF-8");
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.insertClient(socket.getInetAddress(), message);
                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class UDPTransmitter extends Thread {
        static final int UDP_PORT = 12000;
        static final long PERIOD = 2000;

        @Override
        public void run() {
            String data = getIp() + ":" + serverSocket.getLocalPort();
            while (true) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                            getBroadcastAddress(), UDP_PORT);
                    socket.send(packet);
                    sleep(PERIOD);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}