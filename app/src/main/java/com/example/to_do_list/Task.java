// Task.java
package com.example.to_do_list;

import java.util.Date;

public class Task {
    private long id;
    private String title;
    private String description;
    private Priority priority;
    private Date dueDate;
    private static int totalPoints = 0;
    private static int totalTasks = 0;
    private boolean isChecked;

    private int pointsWorth;

    // Constructor without ID
    public Task(String title, String description, Priority priority, Date dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;

        setPointsWorth();
    }

    // Constructor with ID
    public Task(long id, String title, String description, Priority priority, Date dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public void setPointsWorth() {
        if (priority.equals(Priority.HIGH))
        {
            pointsWorth = 10;
        }
        else if (priority.equals(Priority.MEDIUM))
        {
            pointsWorth = 5;
        }
        else if (priority.equals(Priority.LOW))
        {
            pointsWorth = 2;
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public static int getTotalTasks() {
        return totalTasks;
    }

    public static void setTotalTasks(int totalTasks) {
        Task.totalTasks = totalTasks;
    }

    public static void resetTotalTasks()
    {
        Task.totalTasks = 0;

    }

    public static void incrementTotalTasks() {
        totalTasks += 1;
    }

    public static int getTotalPoints()
    {
        return totalPoints;
    }

    public static void setTotalPoints(int totalPoints) {
        Task.totalPoints = totalPoints;
    }

    public static void resetTotalPoints()
    {
        Task.totalPoints = 0;

    }

    public static void incrementPoints(int pointsWorth)
    {
        totalPoints += pointsWorth;
    }

    // Getter for ID
    public long getId() {
        return id;
    }

    // Setter for ID
    public void setId(long id) {
        this.id = id;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Setter for description
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter for priority
    public Priority getPriority() {
        return priority;
    }

    // Setter for priority
    public void setPriority(Priority priority) {
        this.priority = priority;
        setPointsWorth();
    }

    public int getPointsWorth() {
        return pointsWorth;
    }

    // Getter for due date
    public Date getDueDate() {
        return dueDate;
    }

    // Setter for due date
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean hasDueDate()
    {
        return dueDate != null;
    }

    // Priority enum (you can customize this based on your needs)
    public enum Priority {
        HIGH, MEDIUM, LOW
    }
}
