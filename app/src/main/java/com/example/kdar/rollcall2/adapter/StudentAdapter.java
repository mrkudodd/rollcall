package com.example.kdar.rollcall2.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kdar.rollcall2.R;
import com.example.kdar.rollcall2.model.Student;

import java.util.ArrayList;

public class StudentAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Student> students;

    public StudentAdapter(Context context, ArrayList<Student> students) {
        this.context = context;
        this.students = students;
    }

    @Override
    public int getCount() {
        return students != null ? students.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return students.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row, null);
            viewHolder.tv_number = convertView.findViewById(R.id.tv_number);
            viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
            viewHolder.iv_roll = convertView.findViewById(R.id.iv_roll);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Student student = students.get(position);
        viewHolder.tv_number.setText(student.getNumber());
        viewHolder.tv_name.setText(student.getName());
        viewHolder.iv_roll.setImageResource(student.isRolled() ? R.drawable.checked : R.drawable.uncheck);

        return convertView;
    }

    public class ViewHolder {
        private TextView tv_number;
        private TextView tv_name;
        private ImageView iv_roll;
    }
}