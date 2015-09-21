package com.gidimobile.gidimocompass.data;

import java.io.Serializable;

/**
 * Created by Ocheja Patrick Ileanwa on 2015-09-16.
 */
public class UserConnectivity implements Serializable {
    private long time;
    private String speed;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}
