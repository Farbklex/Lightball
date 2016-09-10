package io.lightball.lightball.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alexander Hoffmann on 09.09.16.
 */
public class Player implements Parcelable{
    public final String id;
    public final String name;
    public final int health;

    public Player(String id, String content, int health) {
        this.id = id;
        this.name = content;
        this.health = health;
    }

    public Player(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.health = Integer.valueOf(in.readString());
    }


    @Override
        public String toString() {
            return name + " " + health;
        }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {this.id,
                this.name,
                this.health+""});
    }

    public static final Parcelable.Creator<Player> CREATOR
            = new Parcelable.Creator<Player>() {
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
