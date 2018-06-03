package lc.btl.Object;

import java.util.ArrayList;

/**
 * Created by THHNt on 2/5/2018.
 */

public class CardList {
    private int id;
    private String name;
    private ArrayList<Card> cards;

    public CardList(String name) {
        this.name = name;
    }

    public CardList(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public CardList(String name, ArrayList<Card> cards) {
        this.name = name;
        this.cards = cards;
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

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }
}
