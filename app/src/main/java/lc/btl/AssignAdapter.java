package lc.btl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by THHNt on 2/20/2018.
 */

public class AssignAdapter extends BaseAdapter {

    private CardDetailsActivity context;
    private int layout;
    private List<User> userList;

    public AssignAdapter (CardDetailsActivity context, int layout, List<User> userList) {
        this.context = context;
        this.layout = layout;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
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
        TextView tvName, tvEmail;
        ImageButton btRemoveMember;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AssignAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            viewHolder = new AssignAdapter.ViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvEmail = (TextView) convertView.findViewById(R.id.tvEmail);
            viewHolder.btRemoveMember = (ImageButton) convertView.findViewById(R.id.btRemoveMember);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AssignAdapter.ViewHolder) convertView.getTag();
        }

        final User user = userList.get(position);

        viewHolder.tvName.setText(user.getName());
        viewHolder.tvEmail.setText(user.getEmail());

        viewHolder.btRemoveMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.removeCardMemberDialog(user.getEmail(),user.getName());
            }
        });

        return convertView;
    }
}
