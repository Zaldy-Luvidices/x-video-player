package com.x.player;

import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import android.content.*;

import java.io.*;

public class FileSelectorDialog extends Activity
        implements View.OnClickListener {
    public static final String PARAM_TITLE = "param_title";
    public static final String PARAM_ROOT_PATH = "param_root_path";
    public static final String PARAM_FILTER_EXT = "param_filter_ext";

    public static final String RETURN_SEL_PATH = "selected_path";

    private TextView txtEmpty;
    private ListView lvwFiles;

    private List<FileItem> fileItems;
    private FileListAdapter adapter;
    private Stack<Parcelable> scrollStates;
    private String root, filter_ext = "*";
    private String currFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_file_selector);

        txtEmpty = findViewById(R.id.txtEmpty);
        txtEmpty.setTypeface(Tools.DefaultFont(this, true));
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);
        txtTitle.setText(getIntent().getStringExtra(PARAM_TITLE));
        lvwFiles = findViewById(R.id.lvwFiles);
        lvwFiles.setOnItemClickListener((parent, view, position, id) -> {
            FileItem sel = fileItems.get(position);
            File f = new File(sel.ID);
            if (!f.exists()) return;
            if (sel.Type == FileItem.TYPE_FILE) {
                Exit(RESULT_OK, sel.ID);
            } else if (sel.Type == FileItem.TYPE_FOLDER) {
                scrollStates.push(lvwFiles.onSaveInstanceState());
                loadFiles(sel.ID);
            } else if (sel.Type == FileItem.TYPE_PARENT) {
                loadFiles(sel.ID);
                if (!scrollStates.empty())
                    lvwFiles.onRestoreInstanceState(scrollStates.pop());
            }
        });

        Tools.SetDialogPosition(this, Gravity.BOTTOM);

        scrollStates = new Stack<>();
        fileItems = new ArrayList<>();
        adapter = new FileListAdapter(this, R.layout.menu_list_row);
        lvwFiles.setAdapter(adapter);
        root = getIntent().getStringExtra(PARAM_ROOT_PATH);
        filter_ext = getIntent().getStringExtra(PARAM_FILTER_EXT);
        loadFiles(root);
    }

    @Override
    public void onBackPressed() {
        if (!currFolder.equals(root)) {
            File parent = (new File(currFolder)).getParentFile();
            if (parent == null) return;
            loadFiles(parent.getAbsolutePath());
            if (!scrollStates.empty())
                lvwFiles.onRestoreInstanceState(scrollStates.pop());
        } else super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        // TODO: Implement this method
    }

    private void loadFiles(String path) {
        currFolder = path;

        File folder = new File(path);
        fileItems.clear();
        File[] files = folder.listFiles();
        if (files == null) files = new File[]{};

        Tools.SortFileList(files, Tools.SORT_TYPE_FILENAME,
                Tools.SORT_GROUPING_FOLDER_FIRST, false);

        for (File f : files) {
            int type;

            if (!filter_ext.equals("") &&
                    !filter_ext.equals("*") &&
                    f.isFile()) {
                if (!f.getAbsolutePath().endsWith("." + filter_ext)) {
                    continue;
                }
            }
            if (f.isFile()) type = FileItem.TYPE_FILE;
            else if (f.isDirectory()) type = FileItem.TYPE_FOLDER;
            else type = FileItem.TYPE_UNKNOWN;

            fileItems.add(new FileItem(f.getAbsolutePath(), f.getName(), type));
        }

        adapter.notifyDataSetChanged();
        if (fileItems.size() > 0) {
            lvwFiles.setSelection(0);
            lvwFiles.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.INVISIBLE);
        } else {
            lvwFiles.setVisibility(View.INVISIBLE);
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void Exit(int resCode, String selPath) {
        if (resCode == RESULT_OK) {
            Intent result = new Intent();
            result.putExtra(RETURN_SEL_PATH, selPath);
            setResult(resCode, result);
        }
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    public class FileListAdapter extends ArrayAdapter<FileItem> {
        private final int resourceLayout;
        private final Context mContext;

        public FileListAdapter(Context context, int resource) {
            super(context, resource, fileItems);
            this.resourceLayout = resource;
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            FileItem p = getItem(position);
            if (p != null) {
                // icon
                ImageView imgIcon = v.findViewById(R.id.imgMenuIcon);
                if (p.Type == FileItem.TYPE_FOLDER)
                    imgIcon.setImageResource(R.mipmap.ic_folder);
                else if (p.Type == FileItem.TYPE_FILE)
                    imgIcon.setImageResource(R.mipmap.ic_file);
                else if (p.Type == FileItem.TYPE_PARENT)
                    imgIcon.setImageResource(R.mipmap.ic_subdirectory_arrow_left);
                // name
                TextView txtTitle = v.findViewById(R.id.txtMenuName);
                txtTitle.setTypeface(Tools.DefaultFont(FileSelectorDialog.this, false));
                txtTitle.setText(p.Name);
            }

            return v;
        }
    }
}
