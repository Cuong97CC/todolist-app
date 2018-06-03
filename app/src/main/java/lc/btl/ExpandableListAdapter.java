package lc.btl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import lc.btl.Object.Card;
import lc.btl.Object.CardList;

/**
 * Created by THHNt on 2/6/2018.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter{

    CardsListActivity context;
    ArrayList<CardList> listList;
    HashMap<CardList, ArrayList<Card>> listCard;

    public ExpandableListAdapter(CardsListActivity context, ArrayList<CardList> listList, HashMap<CardList, ArrayList<Card>> listCard) {
        this.context = context;
        this.listList = listList;
        this.listCard = listCard;
    }

    @Override
    public int getGroupCount() {
        return listList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listCard.get(listList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listCard.get(listList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final CardList list = (CardList) getGroup(groupPosition);
        String listName = list.getName();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_list, null);
        TextView tvList = (TextView) convertView.findViewById(R.id.tvList);
        final ImageButton btListOption = (ImageButton) convertView.findViewById(R.id.btListOption);
        btListOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.showListOptions(btListOption, list);
            }
        });
        tvList.setText(listName);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Card card = (Card) getChild(groupPosition, childPosition);
        String cardName = card.getName();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.item_card, null);
        TextView tvCard = (TextView) convertView.findViewById(R.id.tvCard);
        tvCard.setText(cardName);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
