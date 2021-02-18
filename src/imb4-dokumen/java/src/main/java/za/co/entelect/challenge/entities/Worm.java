package za.co.entelect.challenge.entities;
import com.google.gson.annotations.SerializedName;
import za.co.entelect.challenge.enums.Profession;

public class Worm{
    @SerializedName("roundsUntilUnfrozen")
    public int sampeMeleleh;

    @SerializedName("profession")
    public Profession prof;

    @SerializedName("id")
    public int id;

    @SerializedName("health")
    public int health;

    @SerializedName("initialHp")
    public int initHP;

    @SerializedName("position")
    public Position position;

    @SerializedName("diggingRange")
    public int diggingRange;

    @SerializedName("movementRange")
    public int movementRange;

    @SerializedName("bananaBombs")
    public BananaBombs banana;

    @SerializedName("snowballs")
    public SnowBalls snow;
}
