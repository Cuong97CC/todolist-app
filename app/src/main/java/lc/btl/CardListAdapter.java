package lc.btl;
import android.content.ClipData;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Juned on 3/27/2017.
 */

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.MyView> {

    private List<CardList> list;
    CardsListActivity context;
    int resumeColor;
    public class MyView extends RecyclerView.ViewHolder {
        public LinearLayoutListView layout;
        public TextView textView;
        public ListView lvTest;
        public ImageButton mImageButton;
        public LinearLayout areaLv;
        public TextView luEmpty;
        public MyView(View view) {
            super(view);

            textView = (TextView) view.findViewById(R.id.textview1);
            layout = (LinearLayoutListView) view.findViewById(R.id.pane1);
            lvTest = (ListView)view.findViewById(R.id.lvTest);
            layout.setListView(lvTest);
            mImageButton = (ImageButton) view.findViewById(R.id.imageButton);
            areaLv = (LinearLayout) view.findViewById(R.id.areaLv);

            luEmpty = (TextView) view.findViewById(R.id.empty);
        }
    }


    public CardListAdapter(CardsListActivity context, List<CardList> horizontalList) {
        this.context = context;
        this.list = horizontalList;
        resumeColor  = context.getResources().getColor(android.R.color.background_light);
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_f, parent, false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {
        holder.layout.setOnDragListener(myOnDragListener);
        holder.textView.setText(list.get(position).getName());
        TodoAdapter adapter = new TodoAdapter(context,R.layout.item_card_f,list.get(position).getId(),list.get(position).getCards());

        holder.lvTest.setAdapter(adapter);
        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.showListOptions(holder.mImageButton,list.get(position));
            }
        });
        holder.lvTest.setEmptyView(holder.luEmpty);
        holder.lvTest.setOnItemClickListener(new ListViewOnClick(list.get(position).getName()));
        holder.lvTest.setOnItemLongClickListener(myOnItemLongClickListener);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    ///----------------------------
    AdapterView.OnItemLongClickListener myOnItemLongClickListener = new AdapterView.OnItemLongClickListener(){

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            Card selectedItem = (Card)(parent.getItemAtPosition(position));

            TodoAdapter associatedAdapter = (TodoAdapter)(parent.getAdapter());
            List<Card> associatedList = associatedAdapter.getList();

            CardPassObj passObj = new CardPassObj(view, selectedItem, associatedList);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, passObj, 0);

            return true;
        }

    };

    View.OnDragListener myOnDragListener = new View.OnDragListener() {

        @Override
        public boolean onDrag(View v, DragEvent event) {


            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:

                    CardPassObj passObj = (CardPassObj)event.getLocalState();
                    View view = passObj.view;
                    Card passedItem = passObj.item;
                    List<Card> srcList = passObj.srcList;
                    int oldList = passedItem.getIdList();
                    ListView oldParent = (ListView)view.getParent();
                    TodoAdapter srcAdapter = (TodoAdapter)(oldParent.getAdapter());

                    LinearLayoutListView newParent = (LinearLayoutListView)v;
                    TodoAdapter destAdapter = (TodoAdapter)(newParent.listView.getAdapter());
                    List<Card> destList = destAdapter.getList();
                    int newList = destAdapter.getIdList();

                    context.moveCard(context.moveCardURL,String.valueOf(passedItem.getId()), String.valueOf(oldList), String.valueOf(newList));

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                default:
                    break;
            }

            return true;
        }

    };

    class ListViewOnClick implements AdapterView.OnItemClickListener{
        String listName;
        ListViewOnClick(String listName){
            this.listName = listName;
        }
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            context.showCardDetails((Card)(adapterView.getItemAtPosition(i)),
                    context.currentBoard.getName(),
                    context.currentBoard.getIs_owner());
        }
    }
}
