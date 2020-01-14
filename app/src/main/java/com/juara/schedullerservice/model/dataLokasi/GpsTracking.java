
package com.juara.schedullerservice.model.dataLokasi;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GpsTracking implements Serializable, Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("time")
    @Expose
    private String time;
    public final static Creator<GpsTracking> CREATOR = new Creator<GpsTracking>() {


        @SuppressWarnings({
            "unchecked"
        })
        public GpsTracking createFromParcel(Parcel in) {
            return new GpsTracking(in);
        }

        public GpsTracking[] newArray(int size) {
            return (new GpsTracking[size]);
        }

    }
    ;
    private final static long serialVersionUID = 5915154607651455569L;

    protected GpsTracking(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.username = ((String) in.readValue((String.class.getClassLoader())));
        this.latitude = ((String) in.readValue((String.class.getClassLoader())));
        this.longitude = ((String) in.readValue((String.class.getClassLoader())));
        this.time = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public GpsTracking() {
    }

    /**
     * 
     * @param latitude
     * @param id
     * @param time
     * @param username
     * @param longitude
     */
    public GpsTracking(String id, String username, String latitude, String longitude, String time) {
        super();
        this.id = id;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(username);
        dest.writeValue(latitude);
        dest.writeValue(longitude);
        dest.writeValue(time);
    }

    public int describeContents() {
        return  0;
    }

}
