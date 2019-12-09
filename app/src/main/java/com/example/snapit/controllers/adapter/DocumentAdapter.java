package com.example.snapit.controllers.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapit.views.DisplayDocumentActivity;
import com.example.snapit.R;
import com.example.snapit.models.Bean_Document;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private Context context;
    private List<Bean_Document> documentList;
    private DocumentInterfaceCallback callback;

    public DocumentAdapter(Context context, List<Bean_Document> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.document_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.documentText.setText(documentList.get(position).getName());

        Picasso.get().load(documentList.get(position).getFileUrl()).into(holder.documentImg);

        holder.docCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayDocumentActivity.class);
                intent.putExtra("docType", documentList.get(position).getType());
                intent.putExtra("docName", documentList.get(position).getName());
                intent.putExtra("docUrl", documentList.get(position).getFileUrl());
                intent.putExtra("subjectName", documentList.get(position).getDocSubjectName());
                context.startActivity(intent);

            }
        });

        holder.docCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("Hello", "onLongClick: "+ documentList.get(position).getImageId() );
                callback.showOptionMenu(position, v);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public void updateName(String name, int position) {
        documentList.get(position).setName(name);
        notifyItemChanged(position);
        notifyDataSetChanged();
    }

    public void setCallback(DocumentInterfaceCallback callback) {
        this.callback = callback;
    }

    public interface DocumentInterfaceCallback{
        void showOptionMenu(int position, View view);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView documentImg;
        TextView documentText;
        CardView docCardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            documentImg = itemView.findViewById(R.id.document_img);
            documentText = itemView.findViewById(R.id.tv_document_name);
            docCardView = itemView.findViewById(R.id.doc_card_view);

        }
    }
}
