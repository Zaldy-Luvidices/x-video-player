package com.x.player;

import android.widget.*;
import android.app.*;
import android.view.*;
import android.os.*;
import android.content.*;
import android.text.*;

public class SeekInputDialog extends Activity implements
        View.OnClickListener,
        TextWatcher,
        View.OnFocusChangeListener {
    public static final String PARAM_MIN = "param_min";
    public static final String PARAM_MAX = "param_max";
    public static final String PARAM_DEFAULT = "param_default";

    public static final String RES_SEEK_VALUE = "res_seek_value";

    private TextView txtTitle;
    private EditText editHour, editMinute, editSecond, editMilli;
    private int min;
    private int max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_seek_input);
        ((TextView) findViewById(R.id.lblMin))
                .setTypeface(Tools.DefaultFont(this, true));
        ((TextView) findViewById(R.id.lblMax))
                .setTypeface(Tools.DefaultFont(this, true));
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtTitle.setOnClickListener(this);

        min = getIntent().getIntExtra(PARAM_MIN, 0);
        max = getIntent().getIntExtra(PARAM_MAX, 0);
        int def = getIntent().getIntExtra(PARAM_DEFAULT, 0);
        TextView txt = findViewById(R.id.txtMin);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setText(Tools.formatMilliseconds(min));
        txt = findViewById(R.id.txtMax);
        txt.setTypeface(Tools.DefaultFont(this, false));
        txt.setText(Tools.formatMilliseconds(max));

        int[] val = Tools.parseMilliseconds(def);
        editHour = findViewById(R.id.editHour);
        editHour.setTypeface(Tools.DefaultFont(this, false));
        editHour.setText(String.valueOf(val[0]));
        editHour.addTextChangedListener(this);
        editHour.setOnFocusChangeListener(this);
        editMinute = findViewById(R.id.editMinute);
        editMinute.setTypeface(Tools.DefaultFont(this, false));
        editMinute.setText(String.valueOf(val[1]));
        editMinute.addTextChangedListener(this);
        editHour.setOnFocusChangeListener(this);
        editSecond = findViewById(R.id.editSecond);
        editSecond.setTypeface(Tools.DefaultFont(this, false));
        editSecond.setText(String.valueOf(val[2]));
        editSecond.addTextChangedListener(this);
        editHour.setOnFocusChangeListener(this);
        editMilli = findViewById(R.id.editMilli);
        editMilli.setTypeface(Tools.DefaultFont(this, false));
        editMilli.setText(String.valueOf(val[3]));
        editMilli.addTextChangedListener(this);
        editHour.setOnFocusChangeListener(this);

        txt = findViewById(R.id.txtSeek);
        txt.setOnClickListener(this);
        findViewById(R.id.imgHourUp).setOnClickListener(this);
        findViewById(R.id.imgHourDown).setOnClickListener(this);
        findViewById(R.id.imgMinuteUp).setOnClickListener(this);
        findViewById(R.id.imgMinuteDown).setOnClickListener(this);
        findViewById(R.id.imgSecondUp).setOnClickListener(this);
        findViewById(R.id.imgSecondDown).setOnClickListener(this);
        findViewById(R.id.imgMilliUp).setOnClickListener(this);
        findViewById(R.id.imgMilliDown).setOnClickListener(this);

        Tools.SetDialogPosition(this, Gravity.BOTTOM);
    }

    @Override
    public void onClick(View v) {
        if (v == txtTitle) {
            Exit(RESULT_CANCELED, new Intent());
        } else if (v.getId() == R.id.txtSeek) {
            if (getCurrentInputValue() < min) {
                Tools.Toast(this, "Value is smaller than MIN", true, true);
                return;
            } else if (getCurrentInputValue() > max) {
                Tools.Toast(this, "Value is greater than MAX", true, true);
                return;
            }

            Intent data = new Intent();
            data.putExtra(RES_SEEK_VALUE, getCurrentInputValue());
            Exit(RESULT_OK, data);
        } else if (v.getId() == R.id.imgHourUp) {
            String s = editHour.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i < 99) i++;
            editHour.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgHourDown) {
            String s = editHour.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i > 0) i--;
            editHour.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgMinuteUp) {
            String s = editMinute.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i < 59) i++;
            editMinute.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgMinuteDown) {
            String s = editMinute.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i > 0) i--;
            editMinute.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgSecondUp) {
            String s = editSecond.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i < 59) i++;
            editSecond.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgSecondDown) {
            String s = editSecond.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i > 0) i--;
            editSecond.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgMilliUp) {
            String s = editMilli.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i < 9) i++;
            editMilli.setText(String.valueOf(i));
        } else if (v.getId() == R.id.imgMilliDown) {
            String s = editMilli.getText().toString();
            int i = 0;
            if (!s.equals("")) i = Integer.parseInt(s);
            if (i > 0) i--;
            editMilli.setText(String.valueOf(i));
        }
    }

    @Override
    public void onBackPressed() {
        Exit(RESULT_CANCELED, new Intent()); // make sure animation will run
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        EditText edit = null;
        if (editHour.hasFocus()) edit = editHour;
        else if (editMinute.hasFocus()) edit = editMinute;
        else if (editSecond.hasFocus()) edit = editSecond;
        else if (editMilli.hasFocus()) edit = editMilli;
        if (edit == null) return;

        if (s.toString().equals("")) return;
        int i = Integer.parseInt(s.toString());
        if ((editMinute.hasFocus() || editSecond.hasFocus())
                && (i >= 60)) {
            s.clear();
            s.append("59");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == editHour ||
                view == editMinute ||
                view == editSecond ||
                view == editMilli &&
                        !hasFocus) {
            EditText edit = (EditText) view;
            if (edit.getText().toString().equals(""))
                edit.setText("0");
        }
    }

    private void Exit(int resCode, Intent data) {
        setResult(resCode, data);
        finish();
        overridePendingTransition(0, R.anim.slide_down);
    }

    private int getCurrentInputValue() {
        int h = Integer.parseInt(editHour.getText().toString());
        int m = Integer.parseInt(editMinute.getText().toString());
        int s = Integer.parseInt(editSecond.getText().toString());
        int ml = Integer.parseInt(editMilli.getText().toString());
        return Tools.mergeToMilliseconds(h, m, s, ml * 100);
    }
}
