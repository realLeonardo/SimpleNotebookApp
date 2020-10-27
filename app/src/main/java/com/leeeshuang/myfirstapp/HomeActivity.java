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
        TextView titleText = vi.findViewById(R.id.title);
        TextView contentText = vi.findViewById(R.id.content);
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
        CheckBox checkBox = vi.findViewById(R.id.checkBox);
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

        // NOTE: hide task input
        taskInput.setVisibility(View.GONE);

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
        Note n4 = new Note("经典的吹泡泡项目源码", "cjson网址：https://sourceforge.net/projects/cjson/\n" +
                "MyTinySTL网址：https://github.com/Alinshans/MyTinySTL\n" +
                "oatpp网址：https://github.com/oatpp/oatpp\n" +
                "Tinyhttpd网址：https://github.com/EZLippi/Tinyhttpd/blob/master/httpd.c\n" +
                "nginx网址：http://nginx.org/\n" +
                "Redis网址：https://redis.io/download");
        Note n5 = new Note("安卓课堂笔记 10/20", "Pager 是 SQLite 的核心模块之一，充当了多种重要角色。作为一个事务管理器，它通过并发控制和故障恢复实现事务的 ACID 特性，负责事务的原子提交和回滚；作为一个页管理器，它处理从文件中读写数据页，并执行文件空间管理工作；作为日志管理器，它负责写日志记录到日志文件；作为锁管理器，它确保事务在访问数据页之前，一定先对数据文件上锁，实现并发控制。本质上来说，Pager 模块实现了存储的持久性和事务的原子性。总结来说，Pager 模块主要由4个子模块组成：事务管理模块、锁管理模块、日志模块和缓存模块，事务模块的实现依赖于其它3个子模块。因此 Pager 模块最核心的功能实质是由缓存模块、日志管理器和锁管理器完成。Pager 模块利用 Pager 对象来跟踪文件锁相关的信息、日志状态、数据库状态等。对于同一个文件、一个进程可能有多个 Pager 对象，这些对象之间都是相互独立的。对于共享缓存模式，每个数据文件只有一个 Pager 对象，所有连接共享这个 Pager 对象。");

        notesDataArr.add(n1);
        notesDataArr.add(n4);
        notesDataArr.add(n2);
        notesDataArr.add(n3);
        notesDataArr.add(n5);

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