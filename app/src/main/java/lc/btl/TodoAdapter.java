package lc.btl;

import android.app.Activity;
import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by Trinh Dinh Viet on 2/21/2018.
 */

public class TodoAdapter extends BaseAdapter{
    CardsListActivity context;
    int resource;
    private int idList;
    private List<Card> list;

    public List<Card> getList() {
        return list;
    }

    public int getIdList() {
        return idList;
    }

    public TodoAdapter(CardsListActivity context, int resource,int idList, List<Card> list) {
        this.context = context;
        this.resource = resource;
        this.idList = idList;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        View rowView = view;

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(resource, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView) rowView.findViewById(R.id.tvCardName);
            viewHolder.tvTime = (TextView) rowView.findViewById(R.id.tvCardTime);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        if (list.get(i).getName().length() > 20) {
            holder.tvName.setText(list.get(i).getName().substring(0,19) + "...");
        } else {
            holder.tvName.setText(list.get(i).getName());
        }
        if(list.get(i).getDate().equals("")) {
            holder.tvTime.setText(R.string.no_time);
        } else {
            holder.tvTime.setText(list.get(i).getDate() + " " + context.getString(R.string.at) + " " + list.get(i).getTime());
        }

        rowView.setOnDragListener(new ItemOnDragListener(list.get(i),context));

        return rowView;
    }
    static class ViewHolder {
        TextView tvName;
        TextView tvTime;
    }
}
class ItemOnDragListener implements View.OnDragListener {

    Card  me;
    CardsListActivity context;

    ItemOnDragListener(Card i, CardsListActivity context){
        me = i;
        this.context = context;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                v.setBackgroundColor(0x30000000);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                v.setBackgroundColor(v.getResources().getColor(android.R.color.background_light));
                break;
            case DragEvent.ACTION_DROP:


                CardPassObj passObj = (CardPassObj)event.getLocalState();
                View view = passObj.view;
                Card passedItem = passObj.item;
                int oldList = passedItem.getIdList();
                List<Card> srcList = passObj.srcList;
                ListView oldParent = (ListView)view.getParent();
                TodoAdapter srcAdapter = (TodoAdapter)(oldParent.getAdapter());

                ListView newParent = (ListView)v.getParent();
                TodoAdapter destAdapter = (TodoAdapter)(newParent.getAdapter());
                int newList = destAdapter.getIdList();
                List<Card> destList = destAdapter.getList();

                int removeLocation = srcList.indexOf(passedItem);
                int insertLocation = destList.indexOf(me);
				/*
				 * If drag and drop on the same list, same position,
				 * ignore
				 */
                if(srcList != destList || removeLocation != insertLocation){
                    context.moveCard(context.moveCardURL,String.valueOf(passedItem.getId()), String.valueOf(oldList), String.valueOf(newList));
                }

                v.setBackgroundColor( v.getResources().getColor(android.R.color.background_light));

                break;
            case DragEvent.ACTION_DRAG_ENDED:

                v.setBackgroundColor(v.getResources().getColor(android.R.color.background_light));
            default:
                break;
        }

        return true;
    }

}
