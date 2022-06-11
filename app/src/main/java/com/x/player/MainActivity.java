package com.x.player;

import android.app.*;
import android.media.MediaPlayer;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;

import android.widget.LinearLayout.*;
import android.graphics.*;
import android.graphics.drawable.*;

import java.io.*;

import android.content.*;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final int SEEKBAR_PADDING = 30;
    public static final int SEEKER_INTERVAL = 100;
    public static final int MID_STORAGE_MIN_A = 600;
    public static final int MID_STORAGE_MIN_B = 700;

    public static final int REQ_THUMB_DATA = 1;
    public static final int REQ_MENU = 2;
    public static final int REQ_SEEK_INPUT_A = 3;
    public static final int REQ_SEEK_INPUT_B = 4;
    public static final int REQ_CHOOSE_FILE_A = 5;
    public static final int REQ_CHOOSE_FILE_B = 6;

    public static final int MID_BOOKS = 1;
    public static final int MID_BOOK_EDITOR = 2;
    public static final int MID_LOOP = 3;
    public static final int MID_PLAYER_A = 4;
    public static final int MID_PLAYER_B = 5;
    public static final int MID_FOLDER_PLAY = 6;
    public static final int MID_FOLDER_PLAY_SEQ = 7;
    public static final int MID_FOLDER_PLAY_RND = 8;
    public static final int MID_FOLDER_PLAY_OFF = 9;

    public static final String THUMB_EXT = "srcvd";

    private TextView txtPosCurr;
    private TextView txtPosMax;
    private TextView txtBookA;
    private TextView txtBookB;
    private TextView txtBookLength, txtBookSetA, txtBookSetB;
    private TextView txtSubA, txtSubB;
    private MutedVideoView vidPlayer, vidSubA, vidSubB;
    private RelativeLayout relControls, relBooks, relBooksEditor;
    private SeekBar seeker;
    private ImageView btnContPlay, btnContRewind, btnContForward;
    private ImageView btnBookReset, btnBookSave, btnOptions, btnSound;
    private ImageView imgSelThumb = null, imgPressedThumb = null;
    private FrameLayout frameSubA, frameSubB;

    private String videoPath, videoPathA, videoPathB;
    private List<String> videoList = new ArrayList<>();
    private int videoIndex = -1;
    private int markStart = -1, markEnd = -1;
    private boolean settLoop = false;
    private boolean settShowBookEditor = true;
    private boolean settShowBooks = true;
    private boolean settSoundOn = false;
    private Boolean settFolderPlay = null;
    private BookRunnerData runnerA, runnerB;

    private int pauseVidPos = -1, pauseVidAPos = -1, pauseVidBPos = -1;
    private boolean dialogShown = false;

    private final Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
            if (seeker != null) {
                int curr = vidPlayer.getCurrentPosition();
                if (markEnd > -1 && (curr > markEnd)) {
                    vidPlayer.seekTo(markStart);
                    if (!settLoop) onClick(btnContPlay);
                }
                updateSeeker(curr);

            }
            if (vidPlayer.isPlaying()) {
                seeker.postDelayed(onEverySecond, SEEKER_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Tools.DefaultFont(this, true));
        txtPosCurr = findViewById(R.id.txtPosCurrent);
        txtPosCurr.setTypeface(Tools.DefaultFont(this, false));
        txtPosMax = findViewById(R.id.txtPosMax);
        txtPosMax.setTypeface(Tools.DefaultFont(this, false));
        txtBookA = findViewById(R.id.txtBookA);
        txtBookA.setTypeface(Tools.DefaultFont(this, false));
        txtBookA.setOnClickListener(this);
        txtBookA.setOnLongClickListener(view -> {
            int i = Integer.parseInt(view.getTag().toString());
            if (i < 0) i = 0;
            callSeeker(REQ_SEEK_INPUT_A, 0, seeker.getMax(), i);
            return true;
        });
        txtBookSetA = findViewById(R.id.txtBookSetA);
        txtBookSetA.setTypeface(Tools.DefaultFont(this, false));
        txtBookSetA.setOnClickListener(this);
        txtBookB = findViewById(R.id.txtBookB);
        txtBookB.setTypeface(Tools.DefaultFont(this, false));
        txtBookB.setOnClickListener(this);
        txtBookB.setOnLongClickListener(view -> {
            int i = Integer.parseInt(view.getTag().toString());
            if (i < 0) i = 0;
            callSeeker(REQ_SEEK_INPUT_B, 0, seeker.getMax(), i);
            return true;
        });
        txtBookSetB = findViewById(R.id.txtBookSetB);
        txtBookSetB.setTypeface(Tools.DefaultFont(this, false));
        txtBookSetB.setOnClickListener(this);
        txtBookLength = findViewById(R.id.txtBookLength);
        txtBookLength.setTypeface(Tools.DefaultFont(this, false));
        resetBookmarks();
        relControls = findViewById(R.id.relControls);
        relControls.setVisibility(View.INVISIBLE);
        relBooks = findViewById(R.id.relThumbs);
        relBooksEditor = findViewById(R.id.relBookEditor);
        btnContPlay = findViewById(R.id.btnContPlay);
        btnContPlay.setTag("pause");
        btnContPlay.setOnClickListener(this);
        btnContRewind = findViewById(R.id.btnContRewind);
        btnContRewind.setOnClickListener(this);
        btnContForward = findViewById(R.id.btnContForward);
        btnContForward.setOnClickListener(this);
        btnBookReset = findViewById(R.id.btnBookReset);
        btnBookReset.setOnClickListener(this);
        btnBookSave = findViewById(R.id.btnBookSave);
        btnBookSave.setOnClickListener(this);
        btnOptions = findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(this);
        btnSound = findViewById(R.id.btnSound);
        btnSound.setOnClickListener(this);
        frameSubA = findViewById(R.id.frameSubA);
        frameSubA.setVisibility(View.GONE);
        frameSubB = findViewById(R.id.frameSubB);
        frameSubB.setVisibility(View.GONE);
        txtSubA = findViewById(R.id.txtSubA);
        txtSubA.setVisibility(View.GONE);
        txtSubA.setTypeface(Tools.DefaultFont(this, true));
        txtSubB = findViewById(R.id.txtSubB);
        txtSubB.setVisibility(View.GONE);
        txtSubB.setTypeface(Tools.DefaultFont(this, true));
        seeker = findViewById(R.id.seeker);
        seeker.setPadding(SEEKBAR_PADDING, 0, SEEKBAR_PADDING, 0);
        seeker.requestLayout();
        seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (String.valueOf(btnContPlay.getTag()).equals("pause")) {
                    vidPlayer.start();
                    seeker.postDelayed(onEverySecond, SEEKER_INTERVAL);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                vidPlayer.pause();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    clearMark();
                    vidPlayer.seekTo(progress);
                    updateSeeker(progress);
                }
            }
        });
        vidPlayer = findViewById(R.id.vidPlayer);
        vidPlayer.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (relControls.getVisibility() == View.VISIBLE) {
                    relBooks.setVisibility(View.GONE);
                    relBooksEditor.setVisibility(View.GONE);
                    relControls.setVisibility(View.GONE);
                } else {
                    relControls.setVisibility(View.VISIBLE);
                }
                return true;
            }
            return false;
        });
        vidPlayer.setOnPreparedListener(mp -> {
            vidPlayer.SetVolume(settSoundOn ? 100 : 0);
            if (pauseVidPos != -1) {
                resumeVideo();
                return;
            }

            seeker.setMax(vidPlayer.getDuration());
            seeker.postDelayed(onEverySecond, SEEKER_INTERVAL);
            txtPosMax.setText(Tools.formatMilliseconds(vidPlayer.getDuration()));
            relControls.setVisibility(View.VISIBLE);
            loadThumbs();
        });
        vidPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Tools.Toast(MainActivity.this, "Video playback error", true, true);
                MainActivity.this.finish();
                return true;
            }
        });
        vidPlayer.setOnCompletionListener(mp -> {
            clearMark();
            vidPlayer.seekTo(0);
            if (settFolderPlay != null) {
                playNextVideo();
            } else if (settLoop) {
                vidPlayer.start();
                seeker.postDelayed(onEverySecond, SEEKER_INTERVAL);
            } else {
                vidPlayer.setTag("play");
                onClick(btnContPlay);
            }
        });

        String selVideoPath = this.getIntent().getData().getPath();
        videoList = Tools.ListVideosFromPath(selVideoPath);
        videoIndex = Tools.SortVideos(videoList, selVideoPath, settFolderPlay);
        videoIndex--; // to select the same video after incrementing
        playNextVideo();

        setupVideoAB();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_THUMB_DATA) {
            if (resultCode == ThumbDataDialog.RES_DELETE) {
                if (imgSelThumb == imgPressedThumb) clearMark();
                removeThumb(imgPressedThumb);
                saveAllThumbData();
            } else if (resultCode == ThumbDataDialog.RES_LOAD) {
                String[] marks = String.valueOf(imgPressedThumb.getTag()).split("-");
                int iStart = Integer.parseInt(marks[0]);
                int iEnd = Integer.parseInt(marks[1]);
                txtBookA.setTag(iStart);
                txtBookA.setText(Tools.formatMilliseconds(iStart));
                txtBookB.setTag(iEnd);
                txtBookB.setText(Tools.formatMilliseconds(iEnd));
                int len = iEnd - iStart;
                txtBookLength.setTag(len);
                txtBookLength.setText(Tools.formatMilliseconds(len));
            }
            imgPressedThumb = null;
        } else if (requestCode == REQ_MENU &&
                resultCode == RESULT_OK) {
            int selMenu = data.getIntExtra(MenuDialog.RETURN_MENU_ID, 0);
            if (selMenu == MID_BOOKS) {
                settShowBooks = !settShowBooks;
                relBooks.setVisibility(settShowBooks ? View.VISIBLE : View.GONE);
            } else if (selMenu == MID_BOOK_EDITOR) {
                settShowBookEditor = !settShowBookEditor;
                relBooksEditor.setVisibility(settShowBookEditor ? View.VISIBLE : View.GONE);
            } else if (selMenu == MID_LOOP) {
                settLoop = !settLoop;
            } else if (selMenu == MID_PLAYER_A) {
                if (frameSubA.getVisibility() == View.VISIBLE)
                    stopVideoA();
                else {
                    showMenuDialog("Select Root", createRootMenu(MID_STORAGE_MIN_A), MenuDialog.MODE_GENERAL);
                }
            } else if (selMenu == MID_PLAYER_B) {
                if (frameSubB.getVisibility() == View.VISIBLE)
                    stopVideoB();
                else {
                    showMenuDialog("Select Root", createRootMenu(MID_STORAGE_MIN_B), MenuDialog.MODE_GENERAL);
                }
            } else if (selMenu == MID_FOLDER_PLAY) {
                showMenuDialog("Folder Play Options", createFolderPlayOptionsMenu(), MenuDialog.MODE_GENERAL);
            } else if (selMenu == MID_FOLDER_PLAY_SEQ
                    || selMenu == MID_FOLDER_PLAY_RND) {
                settFolderPlay = selMenu == MID_FOLDER_PLAY_SEQ;
                videoIndex = Tools.SortVideos(videoList, videoPath, settFolderPlay);
                if (videoIndex == -1) {
                    videoList.clear(); // all videos are removed
                }
            } else if (selMenu == MID_FOLDER_PLAY_OFF) {
                settFolderPlay = null;
            } else if (selMenu >= MID_STORAGE_MIN_B) {
                Intent intent = new Intent(this, FileSelectorDialog.class);
                intent.putExtra(FileSelectorDialog.PARAM_TITLE, "Select Bookmark File");
                intent.putExtra(FileSelectorDialog.PARAM_ROOT_PATH,
                        data.getStringExtra(MenuDialog.RETURN_MENU_TAG));
                intent.putExtra(FileSelectorDialog.PARAM_FILTER_EXT, THUMB_EXT);
                dialogShown = true;
                startActivityForResult(intent, REQ_CHOOSE_FILE_B);
            } else if (selMenu >= MID_STORAGE_MIN_A) {
                Intent intent = new Intent(this, FileSelectorDialog.class);
                intent.putExtra(FileSelectorDialog.PARAM_TITLE, "Select Bookmark File");
                intent.putExtra(FileSelectorDialog.PARAM_ROOT_PATH,
                        data.getStringExtra(MenuDialog.RETURN_MENU_TAG));
                intent.putExtra(FileSelectorDialog.PARAM_FILTER_EXT, THUMB_EXT);
                dialogShown = true;
                startActivityForResult(intent, REQ_CHOOSE_FILE_A);
            }
        } else if (requestCode == REQ_SEEK_INPUT_A &&
                resultCode == RESULT_OK) {
            int ret = data.getIntExtra(SeekInputDialog.RES_SEEK_VALUE, 0);
            setA(ret);
        } else if (requestCode == REQ_SEEK_INPUT_B &&
                resultCode == RESULT_OK) {
            int ret = data.getIntExtra(SeekInputDialog.RES_SEEK_VALUE, 0);
            setB(ret);
        } else if (requestCode == REQ_CHOOSE_FILE_A &&
                resultCode == RESULT_OK) {
            stopVideoA();
            String sel = data.getStringExtra(FileSelectorDialog.RETURN_SEL_PATH);
            sel = sel.replace("." + THUMB_EXT, "");
            if (!(new File(sel)).exists()) {
                Tools.Toast(this, "Marked video file not found", true, true);
                return;
            }
            videoPathA = sel;
            runVideoA();
        } else if (requestCode == REQ_CHOOSE_FILE_B &&
                resultCode == RESULT_OK) {
            stopVideoB();
            String sel = data.getStringExtra(FileSelectorDialog.RETURN_SEL_PATH);
            sel = sel.replace("." + THUMB_EXT, "");
            if (!(new File(sel)).exists()) {
                Tools.Toast(this, "Marked video file not found", true, true);
                return;
            }
            videoPathB = sel;
            runVideoB();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnContPlay) {
            String tag = String.valueOf(btnContPlay.getTag());
            if (tag.equals("pause")) {
                vidPlayer.pause();
                btnContPlay.setTag("play");
                btnContPlay.setImageResource(R.mipmap.ic_play);
            } else {
                vidPlayer.start();
                seeker.postDelayed(onEverySecond, SEEKER_INTERVAL);
                btnContPlay.setTag("pause");
                btnContPlay.setImageResource(R.mipmap.ic_pause);
            }
        } else if (v == btnContForward) {
            int pos = vidPlayer.getCurrentPosition();
            pos += 5000; // 5 secs
            if (pos > vidPlayer.getDuration())
                pos = vidPlayer.getDuration();
            clearMark();
            vidPlayer.seekTo(pos);
            updateSeeker(pos);
        } else if (v == btnContRewind) {
            int pos = vidPlayer.getCurrentPosition();
            pos -= 5000; // 5 secs
            if (pos < 0) pos = 0;
            clearMark();
            vidPlayer.seekTo(pos);
            updateSeeker(pos);
        } else if (v == txtBookSetA) {
            int curr = vidPlayer.getCurrentPosition();
            setA(curr);
        } else if (v == txtBookSetB) {
            int curr = vidPlayer.getCurrentPosition();
            setB(curr);
        } else if (v == btnBookReset) {
            resetBookmarks();
        } else if (v == btnBookSave) {
            if (Integer.parseInt(txtBookLength.getTag().toString()) == -1) {
                Tools.Toast(this, "Both A and B must be marked", false, false);
                return;
            }
            int a = Integer.parseInt(txtBookA.getTag().toString());
            int b = Integer.parseInt(txtBookB.getTag().toString());
            byte[] image = Tools.GetThumbnail(this, videoPath, a);
            addThumb(BitmapFactory.decodeByteArray(image, 0, image.length), a, b, true);
            onClick(btnBookReset);
        } else if (v == btnOptions) {
            showMenuDialog("Settings", createSettingsMenu(), MenuDialog.MODE_GENERAL);
        } else if (v == btnSound) {
            settSoundOn = !settSoundOn;
            vidPlayer.SetVolume(settSoundOn ? 100 : 0);
            if (frameSubA.getVisibility() == View.VISIBLE)
                vidSubA.SetVolume(settSoundOn ? 100 : 0);
            if (frameSubB.getVisibility() == View.VISIBLE)
                vidSubB.SetVolume(settSoundOn ? 100 : 0);
            btnSound.setImageResource(
                    settSoundOn ? R.mipmap.ic_radio_button_checked : R.mipmap.ic_radio_button_unchecked);
        } else if (v == txtBookA) {
            int i = Integer.parseInt(txtBookA.getTag().toString());
            if (i > -1) {
                vidPlayer.seekTo(i);
                updateSeeker(i);
            }
        } else if (v == txtBookB) {
            int i = Integer.parseInt(txtBookB.getTag().toString());
            if (i > -1) {
                vidPlayer.seekTo(i);
                updateSeeker(i);
            }
        }
    }

    @Override
    protected void onPause() {
        pauseVidPos = vidPlayer.getCurrentPosition();
        vidPlayer.pause();

        if (frameSubA.getVisibility() == View.VISIBLE) {
            pauseVidAPos = vidSubA.getCurrentPosition();
            vidSubA.pause();
        }
        if (frameSubB.getVisibility() == View.VISIBLE) {
            pauseVidBPos = vidSubB.getCurrentPosition();
            vidSubB.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (pauseVidPos != -1) {
            if (dialogShown) {
                resumeVideo();

                if (frameSubA.getVisibility() == View.VISIBLE) {
                    vidSubA.seekTo(pauseVidAPos);
                    if (runnerA.IsRunning()) runnerA.Resume();
                    pauseVidAPos = -1;
                }
                if (frameSubB.getVisibility() == View.VISIBLE) {
                    vidSubB.seekTo(pauseVidBPos);
                    if (runnerB.IsRunning()) runnerB.Resume();
                    pauseVidBPos = -1;
                }
                dialogShown = false;
            }
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    private void playNextVideo() {
        if (videoList.size() <= 0) {
            videoIndex = -1;
            return;
        }

        videoIndex++;
        if (videoIndex >= videoList.size()) {
            videoIndex = 0;
            if (settLoop) {
                Tools.Toast(this, "Back to first video", false, false);
            } else {
                Tools.Toast(this, "End of play", false, false);
                return;
            }
        }

        videoPath = videoList.get(videoIndex);
        //Tools.Toast(MainActivity.this, videoPath, true, false);
        ((TextView) findViewById(R.id.txtTitle)).setText(videoPath);
        vidPlayer.setVideoPath(videoPath);
        vidPlayer.start();
    }

    public void resumeVideo() {
        if (String.valueOf(btnContPlay.getTag()).equals("pause"))
            vidPlayer.start();
        vidPlayer.seekTo(pauseVidPos);
        seeker.postDelayed(onEverySecond, SEEKER_INTERVAL);
        pauseVidPos = -1;
    }

    private void setA(int val) {
        txtBookA.setTag(val);
        txtBookA.setText(Tools.formatMilliseconds(val));
        int b = Integer.parseInt(txtBookB.getTag().toString());
        if (val >= b) {
            txtBookB.setTag(-1);
            txtBookB.setText("---");
            txtBookLength.setTag(-1);
            txtBookLength.setText("---");
        } else if (b >= 0) {
            int len = b - val;
            txtBookLength.setTag(len);
            txtBookLength.setText(Tools.formatMilliseconds(len));
        }
    }

    private void setB(int val) {
        txtBookB.setTag(val);
        txtBookB.setText(Tools.formatMilliseconds(val));
        int a = Integer.parseInt(txtBookA.getTag().toString());
        if (a >= val) {
            txtBookA.setTag(-1);
            txtBookA.setText("---");
            txtBookLength.setTag(-1);
            txtBookLength.setText("---");
        } else if (a >= 0) {
            int len = val - a;
            txtBookLength.setTag(len);
            txtBookLength.setText(Tools.formatMilliseconds(len));
        }
    }

    private void updateSeeker(int milli) {
        final int m = milli;
        seeker.setProgress(m);
        txtPosCurr.post(() -> txtPosCurr.setText(Tools.formatMilliseconds(m)));
    }

    private void resetBookmarks() {
        txtBookA.setTag(-1);
        txtBookA.setText("---");
        txtBookB.setTag(-1);
        txtBookB.setText("---");
        txtBookLength.setTag(-1);
        txtBookLength.setText("---");
    }

    private void addThumb(Bitmap bitmap, int start, int end, boolean save) {
        LinearLayout parent = findViewById(R.id.lnrThumbs);
        ImageView btn = new ImageView(this);
        String tag = String.format(Locale.US, "%010d-%010d", start, end);
        btn.setTag(tag);
        btn.setImageBitmap(bitmap);
        btn.setBackgroundResource(R.drawable.back_thumb);
        btn.setMinimumWidth(0);
        btn.setClickable(true);
        btn.setPadding(10, 10, 10, 10);
        btn.setScaleType(ImageView.ScaleType.FIT_XY);
        LayoutParams params = new LinearLayout.LayoutParams(
                Tools.DpsToPixel(this, 64), Tools.DpsToPixel(this, 48));
        params.setMargins(0, 0, 10, 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(v -> {
            if (v == imgSelThumb) {
                clearMark();
            } else {
                if (imgSelThumb != null) // replace
                    imgSelThumb.setBackgroundResource(R.drawable.back_thumb);
                imgSelThumb = (ImageView) v;
                imgSelThumb.setBackgroundResource(R.drawable.back_thumb_selected);
                String[] marks = String.valueOf(imgSelThumb.getTag()).split("-");
                markStart = Integer.parseInt(marks[0]);
                markEnd = Integer.parseInt(marks[1]);
                // draw marker
                int realWidth = seeker.getWidth() - (SEEKBAR_PADDING * 2);
                int left = (markStart * realWidth) / seeker.getMax();
                int right = (markEnd * realWidth) / seeker.getMax();
                int width = right - left;
                View filler = findViewById(R.id.viewFiller);
                RelativeLayout.LayoutParams params1 =
                        new RelativeLayout.LayoutParams(width, LayoutParams.MATCH_PARENT);
                params1.setMargins(left + SEEKBAR_PADDING, 0, 0, 0);
                filler.setLayoutParams(params1);
                filler.requestLayout();
                filler.setVisibility(View.VISIBLE);
                // seek
                vidPlayer.seekTo(markStart);
                updateSeeker(markStart);
            }
        });
        btn.setOnLongClickListener(view -> {
            imgPressedThumb = (ImageView) view;
            Bitmap bmp = ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            int len = stream.toByteArray().length;
            String title = "Thumb Data (" + Tools.ToDataUnitString(len) + ")";
            Intent intent = new Intent(MainActivity.this, ThumbDataDialog.class);
            intent.putExtra(ThumbDataDialog.PARAM_TITLE, title);
            String tag1 = String.valueOf(view.getTag());
            String[] mark = tag1.split("-");
            intent.putExtra(ThumbDataDialog.PARAM_START, Integer.valueOf(mark[0]));
            intent.putExtra(ThumbDataDialog.PARAM_END, Integer.valueOf(mark[1]));
            dialogShown = true;
            startActivityForResult(intent, REQ_THUMB_DATA);
            return true;
        });
        parent.addView(btn);
        if (save) appendThumbData(btn);
    }

    private boolean saveAllThumbData() {
        LinearLayout parent = findViewById(R.id.lnrThumbs);

        String fName = "." + videoPath + "." + THUMB_EXT;
        File f = new File(fName);
        if (f.exists()) {
            if (!f.delete()) {
                Tools.Toast(this, "Cannot overwrite existing thumb file!", true, true);
                return false;
            }
        }

        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(f, false);
        } catch (Exception ex) {
            Tools.Toast(this, "Cannot open thumb file!", true, true);
            return false;
        }

        for (int i = 0; i < parent.getChildCount(); ++i) {
            ImageView img = (ImageView) parent.getChildAt(i);
            String[] marks = String.valueOf(img.getTag()).split("-");
            String start = String.format(Locale.US, "%010d", Integer.valueOf(marks[0]));
            String end = String.format(Locale.US, "%010d", Integer.valueOf(marks[1]));

            Bitmap bmp = ((BitmapDrawable) img.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();
            String sLen = String.format(Locale.US, "%010d", data.length);

            try {
                writer.write(start.getBytes());
                writer.write(end.getBytes());
                writer.write(sLen.getBytes());
                writer.write(data);
                writer.flush();
            } catch (Exception ex) {
                Tools.Toast(this, "Write failed!", true, true);
                return false;
            }
        }

        try {
            writer.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean appendThumbData(ImageView img) {
        String fName = videoPath + "." + THUMB_EXT;
        File f = new File(fName);
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    Tools.Toast(this, "Cannot create thumb file!", true, true);
                    return false;
                }
            } catch (Exception ex) {
                Tools.Toast(this, "Cannot create thumb file!", true, true);
                return false;
            }
        }

        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(f, true);
        } catch (Exception ex) {
            Tools.Toast(this, "Cannot open thumb file!", true, true);
            return false;
        }

        String[] marks = String.valueOf(img.getTag()).split("-");
        String start = String.format(Locale.US, "%010d", Integer.valueOf(marks[0]));
        String end = String.format(Locale.US, "%010d", Integer.valueOf(marks[1]));

        Bitmap bmp = ((BitmapDrawable) img.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        String sLen = String.format(Locale.US, "%010d", data.length);

        try {
            writer.write(start.getBytes());
            writer.write(end.getBytes());
            writer.write(sLen.getBytes());
            writer.write(data);
            writer.flush();
        } catch (Exception ex) {
            Tools.Toast(this, "Write failed!", true, true);
            return false;
        }

        try {
            writer.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean loadThumbs() {
        LinearLayout parent = findViewById(R.id.lnrThumbs);
        parent.removeAllViews();

        String fName = videoPath + "." + THUMB_EXT;
        File f = new File(fName);
        if (!f.exists()) return false;

        byte[] data = new byte[(int) f.length()];
        try {
            FileInputStream in = new FileInputStream(f);
            BufferedInputStream reader = new BufferedInputStream(in);
            int ret = reader.read(data, 0, data.length);
            if (ret == -1) {
                throw new Exception("Reader returns -1.");
            }
            reader.close();

            int ind = 0;
            while (ind < f.length()) {
                byte[] b = Arrays.copyOfRange(data, ind, ind + 10);
                String start = new String(b);
                ind += 10;
                b = Arrays.copyOfRange(data, ind, ind + 10);
                String end = new String(b);
                ind += 10;
                b = Arrays.copyOfRange(data, ind, ind + 10);
                String sLen = new String(b);
                int len = Integer.parseInt(sLen);
                ind += 10;
                b = Arrays.copyOfRange(data, ind, ind + len);

                addThumb(BitmapFactory.decodeByteArray(b, 0, b.length),
                        Integer.parseInt(start), Integer.parseInt(end), false);
                ind += len;
            }

            return true;
        } catch (Exception ex) {
            Tools.Toast(this, "Error loading thumb data!", true, true);
            return false;
        }
    }

    private void removeThumb(View v) {
        if (saveAllThumbData()) {
            LinearLayout parent = findViewById(R.id.lnrThumbs);
            parent.removeView(v);
        }
    }

    private void clearMark() {
        if (imgSelThumb != null)
            imgSelThumb.setBackgroundResource(R.drawable.back_thumb);
        imgSelThumb = null;
        markStart = -1;
        markEnd = -1;
        View filler = findViewById(R.id.viewFiller);
        filler.setVisibility(View.INVISIBLE);
    }

    private void showMenuDialog(String title, List<MenuItem> menus, int mode) {
        Intent intent = new Intent(this, MenuDialog.class);
        intent.putExtra(MenuDialog.PARAM_TITLE, title);
        intent.putExtra(MenuDialog.PARAM_IDS, MenuItem.toListID(menus));
        intent.putExtra(MenuDialog.PARAM_TITLES, MenuItem.toListNames(menus));
        intent.putExtra(MenuDialog.PARAM_ICONS, MenuItem.toListIcons(menus));
        intent.putExtra(MenuDialog.PARAM_IS_DEVS, MenuItem.toListIsDevs(menus));
        intent.putExtra(MenuDialog.PARAM_TAGS, MenuItem.toListTags(menus));
        intent.putExtra(MenuDialog.PARAM_MODE, mode);
        dialogShown = true;
        startActivityForResult(intent, REQ_MENU);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    private List<MenuItem> createSettingsMenu() {
        int sel = R.mipmap.ic_selected;
        int nsel = R.mipmap.ic_unselected;
        List<MenuItem> ret = new ArrayList<>();
        ret.add(new MenuItem(MID_BOOKS, "Bookmarks", (settShowBooks ? sel : nsel), true));
        ret.add(new MenuItem(MID_BOOK_EDITOR, "Bookmark Setter", (settShowBookEditor ? sel : nsel), true));
        ret.add(new MenuItem(MID_LOOP, "Loop", (settLoop ? sel : nsel), false));
        int play = R.mipmap.ic_play;
        int stop = R.mipmap.ic_cancel;
        ret.add(new MenuItem(MID_PLAYER_A,
                ((frameSubA.getVisibility() == View.VISIBLE) ? "Stop Player A" : "Run Player A"),
                ((frameSubA.getVisibility() == View.VISIBLE) ? stop : play),
                false));
        ret.add(new MenuItem(MID_PLAYER_B,
                ((frameSubB.getVisibility() == View.VISIBLE) ? "Stop Player B" : "Run Player B"),
                ((frameSubB.getVisibility() == View.VISIBLE) ? stop : play),
                false));
        String mnuFolderPlayText = "Folder Play ";
        if (settFolderPlay == null) {
            mnuFolderPlayText += "(Off)";
        } else if (settFolderPlay) {
            mnuFolderPlayText += "(Sequential)";
        } else {
            mnuFolderPlayText += "(Random)";
        }
        ret.add(new MenuItem(MID_FOLDER_PLAY, mnuFolderPlayText, (settFolderPlay == null ? nsel : sel), false));
        return ret;
    }

    private List<MenuItem> createRootMenu(int minBound) {
        List<MenuItem> ret = new ArrayList<>();
        List<StorageUtils.StorageInfo> roots = StorageUtils.getStorageList();
        int count = 0;
        for (StorageUtils.StorageInfo r : roots) {
            MenuItem m = new MenuItem(
                    count + minBound,
                    r.getDisplayName(),
                    (r.getDisplayName().startsWith("Internal") ? R.mipmap.ic_smartphone : R.mipmap.ic_device_hub),
                    false);
            m.Tag = r.path;
            ret.add(m);
        }

        return ret;
    }

    private List<MenuItem> createFolderPlayOptionsMenu() {
        int sel = R.mipmap.ic_selected;
        int nsel = R.mipmap.ic_unselected;
        List<MenuItem> ret = new ArrayList<>();
        ret.add(new MenuItem(MID_FOLDER_PLAY_SEQ, "Sequential",
                ((settFolderPlay != null && settFolderPlay) ? sel : nsel), false));
        ret.add(new MenuItem(MID_FOLDER_PLAY_RND, "Random",
                ((settFolderPlay != null && !settFolderPlay) ? sel : nsel), false));
        ret.add(new MenuItem(MID_FOLDER_PLAY_OFF, "Off", (settFolderPlay == null ? sel : nsel), false));
        return ret;
    }

    private void callSeeker(int reqCode, int min, int max, int def) {
        Intent intent = new Intent(this, SeekInputDialog.class);
        intent.putExtra(SeekInputDialog.PARAM_MIN, min);
        intent.putExtra(SeekInputDialog.PARAM_MAX, max);
        intent.putExtra(SeekInputDialog.PARAM_DEFAULT, def);
        dialogShown = true;
        startActivityForResult(intent, reqCode);
    }

    private void setupVideoAB() {
        vidSubA = findViewById(R.id.vidSubA);
        vidSubA.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (runnerA.IsRunning()) runnerA.Stop();
                else runnerA.Resume();
                return true;
            }
            return false;
        });
        vidSubA.setOnPreparedListener(mp -> {
            vidSubA.SetVolume(settSoundOn ? 100 : 0);
            if (pauseVidAPos != -1) { // resume from onPause()
                vidSubA.seekTo(pauseVidAPos);
                if (runnerA.IsRunning()) runnerA.Resume();
                pauseVidAPos = -1;
                return;
            }

            if (runnerA.GetThumbCount() <= 0) {
                Tools.Toast(MainActivity.this, "No thumb data A found", false, true);
                stopVideoA();
                return;
            }

            if (!runnerA.Start()) {
                Tools.Toast(MainActivity.this, "Cannot start bookmark player A!", false, true);
                stopVideoA();
                return;
            }
        });
        vidSubA.setOnErrorListener((mp, what, extra) -> {
            Tools.Toast(MainActivity.this, "Video A playback error", true, true);
            stopVideoA();
            return true;
        });

        vidSubB = findViewById(R.id.vidSubB);
        vidSubB.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (runnerB.IsRunning()) runnerB.Stop();
                else runnerB.Resume();
                return true;
            }
            return false;
        });
        vidSubB.setOnPreparedListener(mp -> {
            vidSubB.SetVolume(settSoundOn ? 100 : 0);
            if (pauseVidBPos != -1) { // resume from onPause()
                vidSubB.seekTo(pauseVidBPos);
                if (runnerB.IsRunning()) runnerB.Resume();
                pauseVidBPos = -1;
                return;
            }

            if (runnerB.GetThumbCount() <= 0) {
                Tools.Toast(MainActivity.this, "No thumb data B found", false, true);
                stopVideoB();
                return;
            }

            if (!runnerB.Start()) {
                Tools.Toast(MainActivity.this, "Cannot start bookmark player B!", false, true);
                stopVideoB();
            }
        });
        vidSubB.setOnErrorListener((mp, what, extra) -> {
            Tools.Toast(MainActivity.this, "Video B playback error", true, true);
            stopVideoB();
            return true;
        });
    }

    private void runVideoA() {
        runnerA = new BookRunnerData(vidSubA, txtSubA, videoPathA);
        runnerA.LoadThumbs(videoPathA);
        frameSubA.setVisibility(View.VISIBLE);
        vidSubA.setVisibility(View.VISIBLE);
        txtSubA.setVisibility(View.VISIBLE);
        vidSubA.setVideoPath(videoPathA);
        vidSubA.start();
    }

    private void stopVideoA() {
        if (runnerA != null) {
            runnerA.Stop();
            runnerA = null;
        }
        vidSubA.setVisibility(View.GONE);
        frameSubA.setVisibility(View.GONE);
        txtSubA.setVisibility(View.GONE);
        if (vidSubA.isPlaying()) vidSubA.stopPlayback();
    }

    private void runVideoB() {
        runnerB = new BookRunnerData(vidSubB, txtSubB, videoPathB);
        runnerB.LoadThumbs(videoPathB);
        frameSubB.setVisibility(View.VISIBLE);
        vidSubB.setVisibility(View.VISIBLE);
        txtSubB.setVisibility(View.VISIBLE);
        vidSubB.setVideoPath(videoPathB);
        vidSubB.start();
    }

    private void stopVideoB() {
        if (runnerB != null) {
            runnerB.Stop();
            runnerB = null;
        }
        vidSubB.setVisibility(View.GONE);
        frameSubB.setVisibility(View.GONE);
        txtSubB.setVisibility(View.GONE);
        if (vidSubB.isPlaying()) vidSubB.stopPlayback();
    }
}
