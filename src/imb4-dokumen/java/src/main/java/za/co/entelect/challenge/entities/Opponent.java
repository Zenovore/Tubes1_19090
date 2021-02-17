package za.co.entelect.challenge.entities;

import com.google.gson.annotations.SerializedName;

public class Opponent {
    @SerializedName("currentWormId")
    public int currentWormId;

    @SerializedName("id")
    public int id;

    @SerializedName("score")
    public int score;

    @SerializedName("worms")
    public Worm[] worms;
}
