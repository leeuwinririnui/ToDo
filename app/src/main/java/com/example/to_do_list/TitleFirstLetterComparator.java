package com.example.to_do_list;

import java.util.Comparator;

public class TitleFirstLetterComparator implements Comparator<Task>
{
    @Override
    public int compare(Task task1, Task task2)
    {
        // Compare the first letter of the titles
        String title1 = task1.getTitle().toLowerCase();
        String title2 = task2.getTitle().toLowerCase();

        return title1.compareTo(title2);
    }
}
