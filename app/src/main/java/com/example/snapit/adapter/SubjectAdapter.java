package com.example.snapit.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapit.DocumentListActivity;
import com.example.snapit.R;
import com.example.snapit.beans.Bean_Subject;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    private Context context;
    private List<Bean_Subject> subjectList;
    private SubjectInterfaceCallback callback;

    public SubjectAdapter(Context context, List<Bean_Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.subject_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.subjectName.setText(subjectList.get(position).getName());
        holder.subjectName.setSelected(true);

        holder.subjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DocumentListActivity.class);
                intent.putExtra("subjectName", subjectList.get(position).getName());
                intent.putExtra("subjectId", subjectList.get(position).getSubjectId());
                context.startActivity(intent);
            }
        });

        holder.subjectName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                callback.showOption(position, v);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }


    public void updateName(String name, int position) {
        subjectList.get(position).setName(name);
        notifyItemChanged(position);
        notifyDataSetChanged();
    }

    public void setCallback(SubjectInterfaceCallback callback) {
        this.callback = callback;
    }

    public interface SubjectInterfaceCallback{
        void showOption(int position, View view);
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView subjectName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectName = itemView.findViewById(R.id.tv_subject_name);
        }
    }
}
