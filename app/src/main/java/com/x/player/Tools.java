package com.x.player;

import android.graphics.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.media.*;
import android.net.*;

import java.io.*;

import android.app.*;

import java.net.URLConnection;
import java.text.*;
import java.math.*;
import java.util.*;

public class Tools {
    // sort type
    public static final int SORT_TYPE_FILENAME = 0;
    public static final int SORT_TYPE_FILE_SIZE = 1;
    public static final int SORT_TYPE_FOLDER_ITEMS = 2;
    public static final int SORT_TYPE_DATE_MODIFIED = 3;
    // grouping
    public static final int SORT_GROUPING_COMBINED = 0;
    public static final int SORT_GROUPING_FOLDER_FIRST = 1;
    public static final int SORT_GROUPING_FILE_FIRST = 2;

    public static void SortFileList(File[] list,
                                    int sortType, int grouping, boolean isReversed) {
        final int rev = isReversed ? -1 : 1;
        final int gp = grouping;
        final int st = sortType;
        Arrays.sort(list, (f1, f2) -> {
            boolean isDiff = (f1.isDirectory() && f2.isFile()) ||
                    (f1.isFile() && f2.isDirectory());
            if (isDiff && gp != SORT_GROUPING_COMBINED) {
                if (gp == SORT_GROUPING_FOLDER_FIRST)
                    return ((f1.isDirectory() ? -1 : 1));
                else if (gp == SORT_GROUPING_FILE_FIRST)
                    return ((f1.isFile() ? -1 : 1));
            }

            if (st == SORT_TYPE_DATE_MODIFIED) {
                Date d1 = new Date(f1.lastModified());
                Date d2 = new Date(f2.lastModified());
                return (d1.compareTo(d2)) * rev;
            } else if (isDiff) {
                // compare by filename
                return (f1.getName().compareToIgnoreCase(f2.getName())) * rev;
            } else if (f1.isFile() && st == SORT_TYPE_FILE_SIZE) {
                return (int) (f1.length() - f2.length()) * rev;
            } else if (f1.isDirectory() && st == SORT_TYPE_FOLDER_ITEMS) {
                File[] lt = f1.listFiles();
                if (lt == null) lt = new File[]{};
                int i1 = lt.length;
                lt = f2.listFiles();
                if (lt == null) lt = new File[]{};
                int i2 = lt.length;
                return (i1 - i2) * rev;
            } else {
                return (f1.getName().compareToIgnoreCase(f2.getName())) * rev;
            }
        });
    }

    public static Typeface DefaultFont(Context context, boolean isBold) {
        if (isBold)
            return Typeface.createFromAsset(context.getAssets(), "fonts/Titillium-Bold.otf");
        else return Typeface.createFromAsset(context.getAssets(), "fonts/Titillium-Regular.otf");
    }

    public static void Toast(Context context, String msg, boolean isLong, boolean isError) {
        Toast toast = Toast.makeText(context, msg, (isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT));
        View v = toast.getView();
        v.setBackgroundResource(isError ? R.drawable.back_toast_error : R.drawable.back_toast_normal);
        TextView txt = v.findViewById(android.R.id.message);
        txt.setTextColor(Color.WHITE);
        txt.setTypeface(DefaultFont(context, false));
        toast.show();
    }

    public static String formatMilliseconds(int milli) {
        int h = milli / (60 * 60 * 1000);
        milli %= (60 * 60 * 1000);
        int m = milli / (60 * 1000);
        milli %= (60 * 1000);
        int s = milli / 1000;
        milli %= 1000;
        milli /= 100; // round to first decimal
        return String.format(Locale.US, "%d:%02d:%02d.%01d", h, m, s, milli);
    }

    public static int[] parseMilliseconds(int milli) {
        int h = milli / (60 * 60 * 1000);
        milli %= (60 * 60 * 1000);
        int m = milli / (60 * 1000);
        milli %= (60 * 1000);
        int s = milli / 1000;
        milli %= 1000;
        milli /= 100; // round to first decimal
        return new int[]{h, m, s, milli};
    }

    public static int mergeToMilliseconds(int hour, int min, int sec, int milli) {
        milli += (sec * 1000);
        milli += (min * 60 * 1000);
        milli += (hour * 60 * 60 * 1000);
        return milli;
    }

    public static String ToDataUnitString(long val) {
        long KB = 1024;
        long MB = KB * KB;
        long GB = MB * KB;

        DecimalFormat df = new DecimalFormat("0.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        if (val >= GB) {
            float ret = (float) val / (float) GB;
            return df.format(ret) + " G";
        } else if (val >= MB) {
            float ret = (float) val / (float) MB;
            return df.format(ret) + " M";
        } else if (val >= KB) {
            float ret = (float) val / (float) KB;
            return df.format(ret) + " K";
        } else return df.format(val) + " B";
    }

    public static void SetDialogPosition(Activity activity, int gravity) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.gravity = gravity;
        if (gravity == Gravity.END) lp.y = 380;
        else lp.y = 200;
        win.setAttributes(lp);
    }

    public static byte[] GetThumbnail(Context context, String vidPath, int frame) {
        // source: https://stackoverflow.com/questions/23703973/how-to-get-video-frame-bitmap-of-video-file-from-raw-folder
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(new File(vidPath)));
        Bitmap bitmap = retriever.getFrameAtTime(frame * 1000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);

        Bitmap thumb = Bitmap.createScaledBitmap(bitmap, 128, 192, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static int DpsToPixel(Context context, float dps) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static List<String> ListVideosFromPath(String path) {
        List<String> videos = new ArrayList<>();
        File folder = new File(path);
        if (!folder.exists()) {
            return videos;
        }
        if (folder.isFile()) {
            path = folder.getParent();
            folder = new File(path);
        }

        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            String mimeType = URLConnection.guessContentTypeFromName(f.getAbsolutePath());
            if (mimeType != null && mimeType.startsWith("video")) {
                videos.add(f.getAbsolutePath());
            }
        }

        return videos;
    }

    public static int SortVideos(List<String> videos, String currentPath, Boolean isSequential) {
        // verify existence of each file
        for (int i = videos.size() - 1; i >= 0; --i) {
            File f = new File(videos.get(i));
            if (!f.exists()) {
                videos.remove(i);
            }
        }
        if (videos.size() <= 0) {
            return -1;
        }

        if (isSequential == null) {
            // do nothing
        } else if (isSequential) {
            Collections.sort(videos, (s1, s2) -> s1.compareToIgnoreCase(s2));
        } else {
            Collections.shuffle(videos);
        }

        for (int i = 0; i < videos.size(); ++i) {
            if (videos.get(i).equalsIgnoreCase(currentPath)) {
                return i;
            }
        }

        return 0;
    }
}
