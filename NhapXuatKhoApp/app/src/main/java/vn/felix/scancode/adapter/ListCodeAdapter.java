package vn.felix.scancode.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import vn.felix.scancode.R;
import vn.felix.scancode.object.InfoCode;

/**
 * Created by VietRuyn on 16/08/2016.
 */
public class ListCodeAdapter extends ArrayAdapter<InfoCode> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<InfoCode> data;

    public ListCodeAdapter(Context context, int layoutResourceId, ArrayList<InfoCode> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CodeHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new CodeHolder();
            holder.tvStampCode = (TextView) row.findViewById(R.id.tvStampCode);
            holder.tvSerialCode = (TextView) row.findViewById(R.id.tvSerialCode);
            holder.imgEdit = (ImageView) row.findViewById(R.id.imgEdit);

            row.setTag(holder);
        } else {
            holder = (CodeHolder) row.getTag();
        }
        InfoCode infoCode = data.get(position);
        holder.tvStampCode.setText(infoCode.getStampCode());
        holder.tvSerialCode.setText(infoCode.getSerialCode());

        return row;
    }

    static class CodeHolder {
        TextView tvStampCode;
        TextView tvSerialCode;
        ImageView imgEdit;
    }

    public void addRangeToTop(ArrayList<InfoCode> code)
    {
        for (InfoCode infoCode : code)
        {
            this.insert(infoCode, 0);
        }
    }
}