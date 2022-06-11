package com.x.player;

import java.util.*;
import java.io.*;

import android.widget.*;

public class BookRunnerData {
    public static final int LOOP_INTERVAL = 100;

    private final List<Integer> bStart = new ArrayList<>();
    private final List<Integer> bEnd = new ArrayList<>();
    private final List<Integer> indexer = new ArrayList<>();
    private MutedVideoView vidPlayer = null;
    private final TextView txtStatus;

    private int curEnd = -1;
    private int curIndex = -1;
    private boolean isRunning = false;
    private final String videoPath;

    private final Runnable onLoop = new Runnable() {
        @Override
        public void run() {
            if (!vidPlayer.isPlaying()) return;
            if (!isRunning) return;
            int i = vidPlayer.getCurrentPosition();
            if (i >= curEnd) {
                curIndex++;
                if (curIndex >= indexer.size()) curIndex = 0;
                runLoop(curIndex);
            } else {
                vidPlayer.postDelayed(onLoop, LOOP_INTERVAL);
            }
        }
    };

    public BookRunnerData(MutedVideoView video, TextView txt, String vidPath) {
        vidPlayer = video;
        txtStatus = txt;
        videoPath = vidPath;
    }

    public int GetThumbCount() {
        return indexer.size();
    }

    public boolean LoadThumbs(String videoPath) {
        String fName = videoPath + ".srcvd";
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
            indexer.clear();
            bStart.clear();
            bEnd.clear();
            while (ind < f.length()) {
                indexer.add(bStart.size());
                byte[] b = Arrays.copyOfRange(data, ind, ind + 10);
                bStart.add(Integer.valueOf(new String(b)));
                ind += 10;
                b = Arrays.copyOfRange(data, ind, ind + 10);
                bEnd.add(Integer.valueOf(new String(b)));
                ind += 10;
                b = Arrays.copyOfRange(data, ind, ind + 10);
                int len = Integer.parseInt(new String(b));
                ind += 10;
                ind += len;
            }

            Collections.shuffle(indexer);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean Start() {
        if (GetThumbCount() == 0) return false;
        isRunning = true;
        return runLoop(0);
    }

    public void Stop() {
        if (vidPlayer.isPlaying())
            vidPlayer.pause();
        isRunning = false;
    }

    public void Resume() {
        vidPlayer.start();
        isRunning = true;
        vidPlayer.postDelayed(onLoop, LOOP_INTERVAL);
    }

    public boolean IsRunning() {
        return isRunning;
    }

    private boolean runLoop(int index) {
        if (vidPlayer == null) {
            isRunning = false;
            return false;
        }

        curIndex = index;
        int ind = indexer.get(curIndex);
        int curStart = bStart.get(ind);
        curEnd = bEnd.get(ind);
        txtStatus.setText("Mark: " + (curIndex + 1) + " of " + GetThumbCount() + " (" + videoPath + ")");

        vidPlayer.seekTo(curStart);
        vidPlayer.postDelayed(onLoop, LOOP_INTERVAL);
        return true;
    }
}
