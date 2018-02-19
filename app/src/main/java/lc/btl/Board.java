package lc.btl;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by THHNt on 2/5/2018.
 */

public class Board implements Serializable {
    private int id;
    private String name;
    private ArrayList<CardList> cardLists;
    private int is_owner;
    private boolean unsynced;

    public Board(String name) {
        this.name = name;
    }

    public Board(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Board(int id, String name, int is_owner) {
        this.id = id;
        this.name = name;
        this.is_owner = is_owner;
    }

    public Board(int id, String name, int is_owner, boolean unsynced) {
        this.id = id;
        this.name = name;
        this.is_owner = is_owner;
        this.unsynced = unsynced;
    }

    public boolean isUnsynced() {
        return unsynced;
    }

    public void setUnsynced(boolean unsynced) {
        this.unsynced = unsynced;
    }

    public int getIs_owner() {
        return is_owner;
    }

    public void setIs_owner(int is_owner) {
        this.is_owner = is_owner;
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

    public ArrayList<CardList> getCardLists() {
        return cardLists;
    }

    public void setCardLists(ArrayList<CardList> cardLists) {
        this.cardLists = cardLists;
    }
}
