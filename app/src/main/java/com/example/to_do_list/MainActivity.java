
package com.example.to_do_list;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements TaskAdapter.OnCheckBoxClickListener
{

    private TaskAdapter taskAdapter;

    private static Boolean[] hasBadge = new Boolean[]{false, false, false, false, false, false};


    @Override
    protected void onPause()
    {
        super.onPause();

        // Save tasks to SharedPreferences when the app is closed or when tasks change
        saveTasks();
        saveBadges();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.sort_by_title)
        {
            // Handle sorting by title
            taskAdapter.sortByTitle();
            return true;
        }
        else if (id == R.id.sort_by_priority)
        {
            // Handle sorting by priority
            taskAdapter.sortByPriority();
            return true;
        }
        else if (id == R.id.sort_by_date)
        {
            // Handle sorting by due date
            taskAdapter.sortByDueDate();
            return true;
        }

        else if (id == R.id.display_badges) {
            // Show the badge pop-up when the "Display Badges" menu item is selected
            showBadgePopup();
            return true;
        }
        else if (id == R.id.action_reset_counts) {
            // Handle the reset counts option
            Task.resetTotalTasks();
            Task.resetTotalPoints();
            resetBadges();
            updateTaskCount();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the taskAdapter
        taskAdapter = new TaskAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(taskAdapter); // Set the adapter here

        // load badges
        loadBadges();

        // Load tasks from SharedPreferences when the app is opened
        loadTasks();

        // Continue with the rest of your code
        taskAdapter.setOnCheckBoxClickListener(this);

        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);
        if (taskAdapter.getItemCount() == 0) // initial task
        {
            addNewTask();
        }
        fabAddTask.setOnClickListener(view -> addNewTask());

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(taskAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private List<Task> getTasksForDate()
    {
        return new ArrayList<>();
    }




    private void addNewTask()
    {
        Task newTask = new Task("New Task", "Description", Task.Priority.MEDIUM, new Date());
        taskAdapter.addTask(newTask);
        updateTaskCount();  // Update task count after adding a new task
    }

    @SuppressLint("SetTextI18n")
    private void updateTaskCount()
    {

        // Assuming you have a TextView with ID textTaskCount to display the counts
        // Assuming you have a TextView with ID textTaskCount to display the count
        TextView textTaskCount = findViewById(R.id.textTaskCount);
        textTaskCount.setText("Points Earned: " + Task.getTotalPoints());
        awardBadge();
    }

    @Override
    public void onCheckBoxClick(int position)
    {

        Task task = taskAdapter.getTasks().get(position);
        // Handle CheckBox click events here
        // You can update points, task count, or any other action you want
        Task.incrementPoints(task.getPointsWorth());
        updateTaskCount();
    }


    public void onAddTaskClick(View view)
    {
        addNewTask();
    }

    // Load tasks when the app is opened
    private void loadTasks()
    {
        SharedPreferences preferences = getSharedPreferences("tasks", Context.MODE_PRIVATE);
        String tasksJson = preferences.getString("tasks_data", "");

        if (!tasksJson.isEmpty())
        {
            try
            {
                JSONArray jsonArray = new JSONArray(tasksJson);
                List<Task> savedTasks = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject taskJson = jsonArray.getJSONObject(i);
                    Task task = new Task(
                            taskJson.getString("title"),
                            taskJson.getString("description"),
                            Task.Priority.valueOf(taskJson.getString("priority")),
                            taskJson.isNull("dueDate") ? null : new Date(taskJson.getLong("dueDate"))
                    );
                    savedTasks.add(task);
                }
                taskAdapter.setTasks(savedTasks);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            // set the completed task count
            Task.setTotalPoints(preferences.getInt("completed_task_count", 0));

            // set the total task count
            Task.setTotalTasks(preferences.getInt("total_tasks", 0));
            updateTaskCount();
        }
    }


    private void saveTasks()
    {
        SharedPreferences preferences = getSharedPreferences("tasks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Convert the taskList to a JSON string
        JSONArray jsonArray = new JSONArray();
        for (Task task : taskAdapter.getTasks())
        {
            JSONObject taskJson = new JSONObject();
            try
            {
                taskJson.put("title", task.getTitle());
                taskJson.put("description", task.getDescription());
                taskJson.put("priority", task.getPriority().toString());
                taskJson.put("dueDate", task.getDueDate() != null ? task.getDueDate().getTime() : JSONObject.NULL);
                jsonArray.put(taskJson);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        editor.putString("tasks_data", jsonArray.toString());
        // Save the completed task count
        editor.putInt("completed_task_count", Task.getTotalPoints());
        editor.putInt("total_tasks", Task.getTotalTasks());

        editor.apply();

    }

    private void loadBadges()
    {
        SharedPreferences preferences = getSharedPreferences("badges", Context.MODE_PRIVATE);
        hasBadge[0] = preferences.getBoolean("badge_1", false);
        hasBadge[1] = preferences.getBoolean("badge_2", false);
        hasBadge[2] = preferences.getBoolean("badge_3", false);
        hasBadge[3] = preferences.getBoolean("badge_4", false);
        hasBadge[4] = preferences.getBoolean("badge_5", false);
        hasBadge[5] = preferences.getBoolean("badge_6", false);

    }


    private void saveBadges() {
        SharedPreferences preferences = getSharedPreferences("badges", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("badge_1", hasBadge[0]);
        editor.putBoolean("badge_2", hasBadge[1]);
        editor.putBoolean("badge_3", hasBadge[2]);
        editor.putBoolean("badge_4", hasBadge[3]);
        editor.putBoolean("badge_5", hasBadge[4]);
        editor.putBoolean("badge_6", hasBadge[5]);
        // Repeat this pattern for all the badges in your array

        editor.apply();
    }


    public void awardBadge()
    {
        int totalPoints = Task.getTotalPoints(); // Get the user's total points from your data

        if (totalPoints >= 10 && !hasBadge[0])
        {
            showBadgeMessage("Congratulations! You've earned a bronze badge", 1);
            hasBadge[0] = true; // Mark the badge as awarded
        }

        if (totalPoints >= 25 && !hasBadge[1])
        {
            showBadgeMessage("Congratulations! You've earned a silver badge", 2);
            hasBadge[1] = true; // Mark the badge as awarded
        }

        if (totalPoints >= 45&& !hasBadge[2])
        {
            showBadgeMessage("Congratulations! You've earned a gold badge", 3);
            hasBadge[2] = true; // Mark the badge as awarded
        }

        if (totalPoints >= 65 && !hasBadge[3])
        {
            showBadgeMessage("Congratulations! You've earned a platinum trophy", 4);
            hasBadge[3] = true; // Mark the badge as awarded
        }

        if (totalPoints >= 90 && !hasBadge[4])
        {
            showBadgeMessage("Congratulations! You've earned diamond shield", 5);
            hasBadge[4] = true; // Mark the badge as awarded
        }
        if (totalPoints >= 120 && !hasBadge[5])
        {
            showBadgeMessage("Congratulations! You've earned a crown trophy", 6);
            hasBadge[5] = true; // Mark the badge as awarded
        }
    }


    private void showBadgeMessage(String message, int badgeNumber)
    {
        // Inflate the custom layout
        View layout = getLayoutInflater().inflate(R.layout.custom_toast_layout, null);

        // Find the ImageView and set the badge image
        ImageView badgeImageView = layout.findViewById(R.id.badgeImageView);
        if (badgeNumber == 1)
        {
            badgeImageView.setImageResource(R.drawable.bronze);
        }
        else if (badgeNumber == 2)
        {
            badgeImageView.setImageResource(R.drawable.silver);
        }
        else if (badgeNumber == 3)
        {
            badgeImageView.setImageResource(R.drawable.gold);
        }
        else if (badgeNumber == 4)
        {
            badgeImageView.setImageResource(R.drawable.platinum);
        }
        else if (badgeNumber == 5)
        {
            badgeImageView.setImageResource(R.drawable.diamond);
        }
        else if (badgeNumber == 6)
        {
            badgeImageView.setImageResource(R.drawable.crown);
        }


        // Find the TextView and set the message
        TextView toastTextView = layout.findViewById(R.id.toastTextView);
        toastTextView.setText(message);

        // Create and display the custom Toast
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showBadgePopup() {
        // Create a custom layout for the badge popup
        View badgePopupView = getLayoutInflater().inflate(R.layout.badge_popup, null);

        // Find the ImageViews for badges and set their visibility based on whether they are earned
        ImageView badge1 = badgePopupView.findViewById(R.id.badge1);
        ImageView badge2 = badgePopupView.findViewById(R.id.badge2);
        ImageView badge3 = badgePopupView.findViewById(R.id.badge3);
        ImageView badge4 = badgePopupView.findViewById(R.id.badge4);
        ImageView badge5 = badgePopupView.findViewById(R.id.badge5);
        ImageView badge6 = badgePopupView.findViewById(R.id.badge6);

        // Check if each badge is earned and set visibility accordingly
        if (hasBadge[0]) {
            badge1.setVisibility(View.VISIBLE);
        }
        if (hasBadge[1]) {
            badge2.setVisibility(View.VISIBLE);
        }
        if (hasBadge[2]) {
            badge3.setVisibility(View.VISIBLE);
        }
        if (hasBadge[3]) {
            badge4.setVisibility(View.VISIBLE);
        }
        if (hasBadge[4]) {
            badge5.setVisibility(View.VISIBLE);
        }
        if (hasBadge[5]) {
            badge6.setVisibility(View.VISIBLE);
        }

        // Create and display a Toast with the badgePopupView as its layout
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(badgePopupView);
        toast.show();
    }

    public static void resetBadges()
    {
        Arrays.fill(hasBadge, false);
    }

}
