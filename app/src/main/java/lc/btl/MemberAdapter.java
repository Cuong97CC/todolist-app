package lc.btl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by THHNt on 2/20/2018.
 */

public class MemberAdapter extends BaseAdapter {

    private CardDetailsActivity context;
    private int layout;
    private List<User> userList;

    public MemberAdapter (CardDetailsActivity context, int layout, List<User> userList) {
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
        CheckBox cbAssign;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MemberAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);
            viewHolder = new MemberAdapter.ViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvEmail = (TextView) convertView.findViewById(R.id.tvEmail);
            viewHolder.cbAssign = (CheckBox) convertView.findViewById(R.id.cbAssign);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MemberAdapter.ViewHolder) convertView.getTag();
        }

        final User user = userList.get(position);

        viewHolder.tvName.setText(user.getName());
        viewHolder.tvEmail.setText(user.getEmail());

        return convertView;
    }
}
