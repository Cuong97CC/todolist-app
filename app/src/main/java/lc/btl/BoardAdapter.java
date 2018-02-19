package lc.btl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import java.util.List;

/**
 * Created by THHNt on 2/5/2018.
 */

public class BoardAdapter extends BaseAdapter {

    private BoardsListActivity context;
    private int layout;
    private List<Board> boardList;

    public BoardAdapter(BoardsListActivity context, int layout, List<Board> boardList) {
        this.context = context;
        this.layout = layout;
        this.boardList = boardList;
    }

    @Override
    public int getCount() {
        return boardList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
        Button btBoardName;
        ImageButton btDeleteBoard, btEditBoard;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            viewHolder = new ViewHolder();
            viewHolder.btBoardName = (Button) convertView.findViewById(R.id.btBoardName);
            viewHolder.btDeleteBoard = (ImageButton) convertView.findViewById(R.id.btDeleteBoard);
            viewHolder.btEditBoard = (ImageButton) convertView.findViewById(R.id.btEditBoard);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Board board = boardList.get(position);

        if(board.getIs_owner() == 0) {
            viewHolder.btEditBoard.setEnabled(false);
            viewHolder.btEditBoard.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        } else {
            viewHolder.btEditBoard.setEnabled(true);
            viewHolder.btEditBoard.setColorFilter(null);
        }

        /*if (board.isUnsynced()) {
            viewHolder.btBoardName.setTextColor(Color.parseColor("#bebebe"));
        } else {
            viewHolder.btBoardName.setTextColor(Color.parseColor("#000000"));
        }*/

        viewHolder.btBoardName.setText(board.getName());

        viewHolder.btEditBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.editBoardDialog(board);
            }
        });

        viewHolder.btDeleteBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.deleteBoardDialog(board);
            }
        });

        viewHolder.btBoardName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.showCardsList(board);
            }
        });

        return convertView;
    }
}
