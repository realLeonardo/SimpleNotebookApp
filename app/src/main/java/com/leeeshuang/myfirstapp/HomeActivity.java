package com.leeeshuang.myfirstapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

class Note {
    public String title;
    public String content;

    Note(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

class Task {
    public boolean status;
    public String content;

    Task(boolean status, String content) {
        this.status = status;
        this.content = content;
    }
}

class NoteListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Note> data;
    private static LayoutInflater inflater = null;

    public NoteListAdapter(Context context, ArrayList<Note> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.note_item_view, null);
        TextView titleText = (TextView) vi.findViewById(R.id.title);
        TextView contentText = (TextView) vi.findViewById(R.id.content);
        titleText.setText(data.get(position).title);
        contentText.setText(data.get(position).content);
        return vi;
    }
}

class TaskListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Task> data;
    private static LayoutInflater inflater = null;

    public TaskListAdapter(Context context, ArrayList<Task> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.task_item_view, null);
        CheckBox checkBox = (CheckBox) vi.findViewById(R.id.checkBox);
        checkBox.setText(data.get(position).content);
        checkBox.setActivated(data.get(position).status);
        return vi;
    }
}

public class HomeActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "com.example.myfirstapp.ID";
    public static final String EXTRA_TITLE = "com.example.myfirstapp.TITLE";
    public static final String EXTRA_CONTENT = "com.example.myfirstapp.CONTENT";

    private final int ANIMATE_DURATION = 300;
    private int shownStatus;

    private ListView notesList;
    private ListView tasksList;

    private final ArrayList<Note> notesDataArr = new ArrayList<>();
    private final ArrayList<Task> tasksDataArr = new ArrayList<>();

    private NoteListAdapter notesAdapter;
    private TaskListAdapter tasksAdapter;

    private TextView notesButton;
    private TextView tasksButton;
    private EditText taskInput;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notesList = findViewById(R.id.notesList);
        tasksList = findViewById(R.id.tasksList);

        this.mockFakeData();

        notesAdapter = new NoteListAdapter(this, notesDataArr);
        notesList.setAdapter(notesAdapter);

        tasksAdapter = new TaskListAdapter(this, tasksDataArr);
        tasksList.setAdapter(tasksAdapter);

        notesButton = findViewById(R.id.notesBtn);
        tasksButton = findViewById(R.id.tasksBtn);

        taskInput = findViewById(R.id.taskInput);
        taskInput.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null) {
            return;
        }

        String title = data.getStringExtra(EXTRA_TITLE);
        String content = data.getStringExtra(EXTRA_CONTENT);
        int targetID = data.getIntExtra(EXTRA_ID, -1);

        // Create item
        if (targetID < 0) {
            notesDataArr.add(0, new Note(title, content));
            notesAdapter.notifyDataSetChanged();
        } else {
            Note n = notesDataArr.get(targetID);

            if(n != null){
                n.title = title;
                n.content = content;
                notesAdapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    public void showNotesListContainer(View view) {
        if (shownStatus == 0) {
            return;
        }

        shownStatus = 0;
        notesButton.setTextColor(getColor(R.color.black));
        tasksButton.setTextColor(getColor(R.color.primary));

        tasksList.setVisibility(View.GONE);
        notesList.setAlpha(0f);
        notesList.setVisibility(View.VISIBLE);
        notesList.animate()
                .alpha(1f)
                .setDuration(ANIMATE_DURATION)
                .setListener(null);
    }

    @SuppressLint("ResourceAsColor")
    public void showTasksListContainer(View view) {
        if(shownStatus == 1) {
            return;
        }

        shownStatus = 1;
        tasksButton.setTextColor(getColor(R.color.black));
        notesButton.setTextColor(getColor(R.color.primary));

        notesList.setVisibility(View.GONE);
        tasksList.setAlpha(0f);
        tasksList.setVisibility(View.VISIBLE);
        tasksList.animate()
                .alpha(1f)
                .setDuration(ANIMATE_DURATION)
                .setListener(null);
    }

    public void createBtnClickHandler(View view) {
        // Create Task Button Clicked
        if (shownStatus == 1) {
            if (taskInput.getVisibility() == View.VISIBLE) {
                String content = taskInput.getText().toString();

                // NOTE: clear input
                taskInput.setText("");

                // NOTE: hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                taskInput.setVisibility(View.GONE);

                if (content.isEmpty()) {
                    return;
                }

                tasksDataArr.add(0, new Task(false, content));
                tasksAdapter.notifyDataSetChanged();
            } else {
                // NOTE: animated toggle
                taskInput.setAlpha(0f);
                taskInput.setVisibility(View.VISIBLE);
                taskInput.animate()
                        .alpha(1f)
                        .setDuration(ANIMATE_DURATION)
                        .setListener(null);

                // NOTE: open keyboard
                taskInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(taskInput, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            // note item clicked
            Intent intent = new Intent(this, WriteNoteActivity.class);
            if (view instanceof LinearLayout) {
                View titleView = ((LinearLayout) view).getChildAt(0);
                View contentView = ((LinearLayout) view).getChildAt(1);

                String title = ((TextView) titleView).getText().toString();
                String content = ((TextView) contentView).getText().toString();
                int index = notesList.getPositionForView(view);

                intent.putExtra(EXTRA_ID, index);
                intent.putExtra(EXTRA_TITLE, title);
                intent.putExtra(EXTRA_CONTENT, content);
            }

            startActivityForResult(intent, 1);
        }
    }

    // NOTE: JUST FOR TEST
    private void mockFakeData() {
        // NOTE: Mock fake data
        Note n1 = new Note("形式化作业！！！", "后天就要截止啦");
        Note n2 = new Note("Idea in 10/21", "Colorful output stream in cpp.");
        Note n3 = new Note("Money-=18", "学校食堂午饭");

        notesDataArr.add(n1);
        notesDataArr.add(n2);
        notesDataArr.add(n3);

        Task t1 = new Task(false, "Learn Android Activity");
        Task t2 = new Task(false, "Play basketball 🏀~");
        Task t3 = new Task(false, "天冷了🥶，买几件衣服吧");
        Task t4 = new Task(false, "二刺猿和 linux 很搭！！！");

        tasksDataArr.add(t1);
        tasksDataArr.add(t2);
        tasksDataArr.add(t3);
        tasksDataArr.add(t4);
    }

}