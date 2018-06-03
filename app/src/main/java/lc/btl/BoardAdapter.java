package lc.btl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;

import lc.btl.Object.Board;

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

        if (board.getName().length() > 10) {
            viewHolder.btBoardName.setText(board.getName().substring(0,9) + "...");
        } else {
            viewHolder.btBoardName.setText(board.getName());
        }

        viewHolder.btEditBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(board.getIs_owner() == 0) {
                    Toast.makeText(context, R.string.not_owner, Toast.LENGTH_LONG).show();
                } else {
                    context.editBoardDialog(board);
                }
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
