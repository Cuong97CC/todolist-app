package lc.btl;

import android.view.View;

import java.util.List;


/**
 * Created by Trinh Dinh Viet on 2/21/2018.
 */

public class CardPassObj {
    public View view;
    public Card item;
    public List<Card> srcList;

    public CardPassObj(View v, Card i, List<Card> s){
        view = v;
        item = i;
        srcList = s;
    }
}
