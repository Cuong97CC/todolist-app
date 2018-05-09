package lc.btl;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by THHNt on 2/5/2018.
 */

public class Card implements Serializable{
    private int id, idList;
    private String name, description, date, time, location, lat, lng;
    private int notice;

    public Card() {
        this.id = 0;
        this.name = "";
        this.description = "";
        this.date = "";
        this.time = "";
        this.location = "";
        this.lat = "";
        this.lng = "";
        this.notice = 0;
    }

    public Card(int id, int notice, String name, String date, String time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.notice = notice;
    }

    public Card(int id, String name, String date, String time, int idList) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.description = "";
        this.location = "";
        this.lat = "";
        this.lng = "";
        this.notice = 0;
        this.idList = idList;
    }

    public int getIdList() {
        return idList;
    }

    public void setIdList(int idList) {
        this.idList = idList;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getNotice() {
        return notice;
    }

    public void setNotice(int notice) {
        this.notice = notice;
    }
}
