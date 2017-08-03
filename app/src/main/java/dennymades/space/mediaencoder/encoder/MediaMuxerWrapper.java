package dennymades.space.mediaencoder.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: sq
 * @date: 2017/7/26
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 混合器，将音、视频进行混合，生成完整mp4文件
 */
public class MediaMuxerWrapper {
    private static final String TAG = MediaMuxerWrapper.class.getSimpleName();
    private MediaMuxer muxer;
    private boolean isMuxing;
    private MediaFormat audioFormat;
    private MediaFormat videoFormat;
    private int audioTrackIndex;
    private int videoTrackIndex;

    private int mNumTracksAdded = 0;
    private int TOTAL_NUM_TRACKS = 2;
    private boolean mMuxerStarted;

    public MediaMuxerWrapper(String outputFile){

        try {
            muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            Log.d(TAG, "exception creating new media muxer ", e);
        }
    }

    public void addAudioEncoder(AudioEncoder encoder){
        audioFormat = encoder.getEncoder().getOutputFormat();
        audioTrackIndex = muxer.addTrack(audioFormat);
        Log.d(TAG, "added audio track");
        Log.e("eee", "added audio track");
    }

    public void addVideoEncoder(VideoEncoder encoder){
        videoFormat = encoder.getEncoder().getOutputFormat();
        videoTrackIndex = muxer.addTrack(videoFormat);
        Log.d(TAG, "added video track");
        Log.e("eee", "added video track");
    }

    public void startMuxing(){
        mNumTracksAdded++;
        if (mNumTracksAdded == TOTAL_NUM_TRACKS) {
            isMuxing = true;
            muxer.start();
            mMuxerStarted = true;
            Log.e("aaa",mMuxerStarted + "");
        }
        Log.e("www",mNumTracksAdded+"==========");
        Log.e("bbb",mMuxerStarted + "");
//        isMuxing = true;
//        muxer.start();
//        mMuxerStarted = true;
    }

    public void stopMuxing(){
        if (isMuxing) {
            isMuxing = false;
            muxer.stop();
            muxer.release();
        }
    }

    public void muxAudio(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo){
        try{
            if (mMuxerStarted) {
                muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo);
                Log.d(TAG, "muxed sample of length "+buffer.remaining());
                Log.e("MediaMuxerWrapper", "muxed audio sample of length "+buffer.remaining());
            }

        }catch(IllegalArgumentException e){
            Log.d(TAG, "argument to writeSampleData incorrect : ",e);
        }catch(IllegalStateException e){
            Log.d(TAG, "muxer in illegal state : ",e);
        }
    }

    public void muxVideo(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo){
        try{
            if (mMuxerStarted) {
                muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo);
            }
            Log.d(TAG, "muxed sample of length "+buffer.remaining());
            Log.e("MediaMuxerWrapper", "muxed video sample of length "+buffer.remaining());
        }catch(IllegalArgumentException e){
            Log.d(TAG, "argument to writeSampleData incorrect : ",e);
        }catch(IllegalStateException e){
            Log.d(TAG, "muxer in illegal state : ",e);
        }
    }
}