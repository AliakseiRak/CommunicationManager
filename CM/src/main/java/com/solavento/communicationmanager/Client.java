package com.solavento.communicationmanager;

import java.net.InetAddress;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class Client {
    private InetAddress inetAddress;
    private int port;
    private boolean isAlive = true;
    private Date startingTime;
    private Date lastCommunicationTime;
    private String description;
    private DateFormat dateFormat = DateFormat.getTimeInstance();

    public Client(InetAddress inetAddress, int port, String description){
        this.inetAddress = inetAddress;
        this.port = port;
        startingTime = Calendar.getInstance().getTime();
        lastCommunicationTime = startingTime;
        this.description = description;
    }

    public String getAddress(){
        return inetAddress.getHostAddress() + ":" + port;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public String getTime(){
        return "Started: " + dateFormat.format(startingTime) + " LastCom: " + dateFormat.format(lastCommunicationTime);
    }

    public boolean isAlive(){
        return isAlive;
    }

    public void setAlive(boolean isAlive){
        this.isAlive = isAlive;
    }

    public void updateLastCommunicationTime(){
        lastCommunicationTime = Calendar.getInstance().getTime();
    }

    public Date getLastCommunicationTime(){
        return lastCommunicationTime;
    }

    @Override
    public boolean  equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Client))return false;
        return ((Client) other).inetAddress.equals(this.inetAddress);
    }
}
