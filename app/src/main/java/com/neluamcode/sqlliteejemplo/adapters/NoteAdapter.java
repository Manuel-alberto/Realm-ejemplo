package com.neluamcode.sqlliteejemplo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.neluamcode.sqlliteejemplo.R;
import com.neluamcode.sqlliteejemplo.models.Note;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class NoteAdapter extends BaseAdapter {

    private Context context;
    private List<Note> list;
    private int layout;

    public NoteAdapter(Context context, List<Note> list, int layout){
        this.context=context;
        this.list=list;
        this.layout=layout;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Note getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null){
            convertView= LayoutInflater.from(context).inflate(layout, null);
            vh = new ViewHolder();
            vh.descripcion = (TextView) convertView.findViewById(R.id.textViewNoteDescription);
            vh.createdAt = (TextView) convertView.findViewById(R.id.textViewNoteCreatedAt);
            convertView.setTag(vh);
        }else{
            vh= (ViewHolder) convertView.getTag();
        }

        Note note = list.get(position);
        vh.descripcion.setText(note.getDescripcion());
        DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
        String date=df.format(note.getCreatedAt());
        vh.createdAt.setText(date);

        return convertView;
    }

    public class ViewHolder{
        TextView descripcion;
        TextView createdAt;

    }
}
