
package com.juara.schedullerservice.model.dataLokasi;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data implements Serializable, Parcelable
{

    @SerializedName("gps_tracking")
    @Expose
    private List<GpsTracking> gpsTracking = null;
    public final static Creator<Data> CREATOR = new Creator<Data>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        public Data[] newArray(int size) {
            return (new Data[size]);
        }

    }
    ;
    private final static long serialVersionUID = -3036469232101458705L;

    protected Data(Parcel in) {
        in.readList(this.gpsTracking, (com.juara.schedullerservice.model.dataLokasi.GpsTracking.class.getClassLoader()));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public Data() {
    }

    /**
     * 
     * @param gpsTracking
     */
    public Data(List<GpsTracking> gpsTracking) {
        super();
        this.gpsTracking = gpsTracking;
    }

    public List<GpsTracking> getGpsTracking() {
        return gpsTracking;
    }

    public void setGpsTracking(List<GpsTracking> gpsTracking) {
        this.gpsTracking = gpsTracking;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(gpsTracking);
    }

    public int describeContents() {
        return  0;
    }

}
