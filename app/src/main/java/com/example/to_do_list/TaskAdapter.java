package com.example.to_do_list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.android.material.datepicker.MaterialDatePicker;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>
        implements ItemTouchHelperAdapter
{

    private List<Task> taskList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private Context context; // Add context variable

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(taskList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void setCompletedTaskCount(int completedTaskCount)
    {

    }

    public interface OnCheckBoxClickListener {
        void onCheckBoxClick(int position);
    }

    private OnCheckBoxClickListener onCheckBoxClickListener;

    public void setOnCheckBoxClickListener(OnCheckBoxClickListener listener) {
        this.onCheckBoxClickListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // New method to get the count of completed tasks
    public int getCompletedTaskCount()
    {
        int count = 0;
        for (Task task : taskList)
        {
            if (task.isChecked())
            {
                count++;
            }
        }
        return count;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        // Other views
        private TextView priorityLabel;
        TextView taskTitle;
        TextView taskDescription;
        CheckBox checkBox;
        TextView dueDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize other views
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            checkBox = itemView.findViewById(R.id.checkBox);
            dueDate = itemView.findViewById(R.id.dueDate);
            priorityLabel = itemView.findViewById(R.id.priorityLabel);
            priorityLabel.setOnClickListener(v -> showPriorityDialog(getAdapterPosition()));

            taskTitle.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    // When focus is lost, save the edited title to the task
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        String newTitle = taskTitle.getText().toString();
                        taskList.get(adapterPosition).setTitle(newTitle);
                    }
                }
            });

            taskDescription.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    // When focus is lost, save the edited description to the task
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        String newDescription = taskDescription.getText().toString();
                        taskList.get(adapterPosition).setDescription(newDescription);
                    }
                }
            });

            dueDate.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showMaterialDatePicker(position);
                }
            });

            // Set a listener for CheckBox click events
            checkBox.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onCheckBoxClickListener != null) {
                    onCheckBoxClickListener.onCheckBoxClick(position);
                    taskList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }

        public void bind(Task task) {
            setPriorityLabel(task.getPriority());
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());
            checkBox.setChecked(task.isChecked());

            if (task.hasDueDate()) {
                dueDate.setVisibility(View.VISIBLE);
                dueDate.setText("Due: " + dateFormat.format(task.getDueDate()));
            } else {
                dueDate.setVisibility(View.GONE);
            }
            TextView overdueMessage = itemView.findViewById(R.id.overdueMessage);
            if (isTaskOverdue(task.getDueDate())) {
                overdueMessage.setVisibility(View.VISIBLE);
            } else {
                overdueMessage.setVisibility(View.GONE);
            }
        }

        private void setPriorityLabel(Task.Priority priority) {
            switch (priority) {
                case HIGH:
                    priorityLabel.setText("High Priority");
                    break;
                case MEDIUM:
                    priorityLabel.setText("Medium Priority");
                    break;
                case LOW:
                    priorityLabel.setText("Low Priority");
                    break;
            }
        }

        private void showPriorityDialog(final int position) {
            // Create and show a dialog or dropdown menu to select the new priority
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select Priority");
            final String[] priorityOptions = {"High Priority", "Medium Priority", "Low Priority"};
            builder.setItems(priorityOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Task task = taskList.get(position);
                    task.setPriority(Task.Priority.values()[which]);
                    notifyItemChanged(position);
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void addTask(Task task) {
        task.setChecked(false);
        taskList.add(task);
        notifyItemInserted(taskList.size() - 1);
        Task.incrementTotalTasks();
    }

    private void showMaterialDatePicker(int position) {
        Task task = taskList.get(position);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(task.getDueDate().getTime())
                .setTitleText("Select Due Date")
                .build();
        datePicker.addOnPositiveButtonClickListener(
                selection -> updateDueDate(position, new Date(selection))
        );
        datePicker.show(((MainActivity) context).getSupportFragmentManager(), datePicker.toString());
    }

    private void updateDueDate(int position, Date newDueDate) {
        Task task = taskList.get(position);
        task.setDueDate(newDueDate);
        notifyItemChanged(position);
    }

    private boolean isTaskOverdue(Date dueDate) {
        if (dueDate == null) {
            return false;
        }
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return dueDate.before(calendar.getTime());
    }
    public void sortByTitle() {
        Collections.sort(taskList, new TitleFirstLetterComparator());
        notifyDataSetChanged();
    }

    public void sortByPriority() {
        Collections.sort(taskList, (task1, task2) -> task1.getPriority().compareTo(task2.getPriority()));
        notifyDataSetChanged();
    }

    public void sortByDueDate() {
        Collections.sort(taskList, (task1, task2) -> task1.getDueDate().compareTo(task2.getDueDate()));
        notifyDataSetChanged();
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged(); // Notify the adapter that the dataset has changed
    }

    public List<Task> getTasks() {
        return taskList;
    }

}
