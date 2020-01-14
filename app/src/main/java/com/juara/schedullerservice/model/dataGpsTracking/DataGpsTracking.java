
package com.juara.schedullerservice.model.dataGpsTracking;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataGpsTracking implements Serializable, Parcelable
{

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("message")
    @Expose
    private String message;
    public final static Creator<DataGpsTracking> CREATOR = new Creator<DataGpsTracking>() {


        @SuppressWarnings({
            "unchecked"
        })
        public DataGpsTracking createFromParcel(Parcel in) {
            return new DataGpsTracking(in);
        }

        public DataGpsTracking[] newArray(int size) {
            return (new DataGpsTracking[size]);
        }

    }
    ;
    private final static long serialVersionUID = -1819283378628599735L;

    protected DataGpsTracking(Parcel in) {
        this.status = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public DataGpsTracking() {
    }

    /**
     * 
     * @param message
     * @param status
     */
    public DataGpsTracking(Boolean status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(status);
        dest.writeValue(message);
    }

    public int describeContents() {
        return  0;
    }

}
