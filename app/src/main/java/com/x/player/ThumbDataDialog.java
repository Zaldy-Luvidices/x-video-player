package com.x.player;

import android.widget.*;
import android.app.*;
import android.view.*;
import android.os.*;

public class ThumbDataDialog extends Activity implements View.OnClickListener {
    public static final String PARAM_TITLE = "param_title";
    public static final String PARAM_START = "param_start";
    public static final String PARAM_END = "param_end";

    public static final int RES_DELETE = 19;
    public static final int RES_LOAD = 20;

    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_thumb_data);
        ((TextView) findViewById(R.id.lblStart))
                .setTypeface(Tools.DefaultFont(this, true));
        ((TextView) findViewById(R.id.lblEnd))
                .setTypeface(Tools.DefaultFont(this, true));
        ((TextView) findViewById(R.id.lblLength))
                .setTypeface(Tools.DefaultFont(this, true));
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);
        txtTitle.setText(getIntent().getStringExtra(PARAM_TITLE));
        int start = getIntent().getIntExtra(PARAM_START, 0);
        int end = getIntent().getIntExtra(PARAM_END, 0);
        int len = end - start;
        TextView txt = findViewById(R.id.txtStart);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setText(Tools.formatMilliseconds(start));
        txt = findViewById(R.id.txtEnd);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setText(Tools.formatMilliseconds(end));
        txt = findViewById(R.id.txtLength);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setText(Tools.formatMilliseconds(len));
        txt = findViewById(R.id.txtDelete);
        txt.setOnClickListener(this);
        txt = findViewById(R.id.txtLoad);
        txt.setOnClickListener(this);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);
    }

    @Override
    public void onClick(View v) {
        if (v == txtTitle) {
            Exit(RESULT_CANCELED);
        } else if (v.getId() == R.id.txtDelete) {
            Exit(RES_DELETE);
        } else if (v.getId() == R.id.txtLoad) {
            Exit(RES_LOAD);
        }
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED); // make sure animation will run
    }

    private void Exit(int resCode) {
        setResult(resCode);
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
