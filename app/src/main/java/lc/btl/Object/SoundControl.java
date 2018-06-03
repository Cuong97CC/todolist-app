package lc.btl.Object;

import android.content.Context;
import android.media.MediaPlayer;

import lc.btl.R;

/**
 * Created by THHNt on 2/9/2018.
 */

public class SoundControl {
    private static SoundControl sInstance;
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    public SoundControl(Context context) {
        this.mContext = context;
    }

    public static SoundControl getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SoundControl(context);
        }
        return sInstance;
    }

    public void playMusic() {
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.alarm);
        mMediaPlayer.start();
    }

    public void stopMusic() {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }
}
