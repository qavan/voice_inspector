package com.sac.speechdemo;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.user.speechrecognizationasservice.R;
import com.sac.speechdemo.util.Util;

import java.util.List;


public class TaskAdapter extends RecyclerView.Adapter {
    private static List<Task> mTasksDataset;
    private static LayoutInflater mInflater;
    private static Activity mActivity;

    public TaskAdapter(List<Task> tasks, LayoutInflater inflater, Activity activity) {
        mTasksDataset = tasks;
        mInflater = inflater;
        mActivity = activity;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public static TextView status;
        public static TextView title;
        public static TextView address;
        public static TextView date;
        public static TextView client;

        TaskViewHolder(View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.idListItemStatus);
            title = itemView.findViewById(R.id.idListItemTitle);
            address = itemView.findViewById(R.id.idListItemAddress);
            date = itemView.findViewById(R.id.idListItemExpireDate);
            client = itemView.findViewById(R.id.idListItemClient);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Task task = mTasksDataset.get(getLayoutPosition());
            Intent intent = new Intent(RouteActivity.getContext(), CardActivity.class);
            intent.putExtra("id", task.getTaskId());
            mActivity.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Task task = mTasksDataset.get(position);
        Boolean taskStatus = task.getB_done();
        if (taskStatus) {
            TaskViewHolder.status.setText(R.string.ROUT_CARD_STATUS_DONE);
            TaskViewHolder.status.setTextColor(((RouteActivity) mActivity).getContext().getResources().getColor(R.color.addGreen));
        } else {
            TaskViewHolder.status.setText(R.string.ROUT_CARD_STATUS_PROCESS);
            TaskViewHolder.status.setTextColor(((RouteActivity) mActivity).getContext().getResources().getColor(R.color.addOrange));
        }

        TaskViewHolder.title.setText("Приём показаний");
        TaskViewHolder.address.setText(task.getC_address());
        TaskViewHolder.date.setText(String.format("Предыдущее от %s", Util.dateCombine(task.getD_prev_date())));
        TaskViewHolder.client.setText(task.getC_client());
    }

    @Override
    public int getItemCount() {
        return mTasksDataset.size();
    }

    public void setTasks(List<Task> tasks) {
        mTasksDataset = tasks;
        notifyDataSetChanged();
    }
}


