package dennymades.space.mediaencoder.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: sq
 * @date: 2017/7/26
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 视频编、解码类
 */
public class VideoEncoder {
//    private static final String TAG = VideoEncoder.class.getSimpleName();
    private static final String TAG = "VideoEncoder";
    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 20;
    private static final int COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
    private int bitRate;

    private static final int TIMEOUT_US = 10000;
    private MediaCodec videoEncoder;
    private MediaFormat videoFormat;
    private static VideoEncoder instance;
    private int mWidth;
    private int mHeight;

    private boolean isEncoding;

    private MediaCodec.BufferInfo bufferInfo;

    private MediaMuxerWrapper muxer;

    private long prevOutputPTSUs = 0;

    public VideoEncoder(MediaMuxerWrapper mux, int width, int height){
        mWidth = width;
        mHeight = height;
        instance = this;
        muxer = mux;
        bufferInfo = new MediaCodec.BufferInfo();

        try {
            videoEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            Log.e(TAG, "exception creating encoder of "+MIME_TYPE+" type", e);
        }


        bitRate = width * height * 3 / 2;
        videoFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, COLOR_FORMAT);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

//        audioFormat = MediaFormat.createAudioFormat(MIME_TYPE,
//                SAMPLE_RATE,
//                1);
//        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
//        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE);
//        //optional stuff
//        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
//        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE,BIT_RATE);
//
//        audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void start(){
        Log.d(TAG, "encoder started");
        Log.e("VideoEncoder", "encoder started");
        videoEncoder.start();
        isEncoding = true;
    }

    public void stop(){
        Log.d(TAG, "encoder stopped");
        Log.e("VideoEncoder","encoder stopped");
        videoEncoder.stop();
        muxer.stopMuxing();
        isEncoding = false;
    }

    public void encode(byte[] input, int length, long presentationTimeUs){
        Log.d(TAG, "presentation time"+presentationTimeUs);
        Log.e("VideoEncoder","presentation time"+presentationTimeUs);
        //get input buffer
        if(isEncoding){

            byte[] yuv420sp = new byte[mWidth * mHeight * 3 / 2];
            NV21ToNV12(input, yuv420sp,mWidth,mHeight);
            input = yuv420sp;

            final ByteBuffer[] inputBuffers = videoEncoder.getInputBuffers();

            //dequeue input buffer
            final int inputBufferIndex = videoEncoder.dequeueInputBuffer(TIMEOUT_US);
            Log.d(TAG, "inputBufferIndex "+inputBufferIndex);
            Log.e("VideoEncoder","inputBufferIndex "+inputBufferIndex);
            if(inputBufferIndex>=0){
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();

                if(input != null){
                    //copy ByteBuffer to input buffer
                    inputBuffer.put(input);
                }
                if(length<=0){
                    ////enqueue bytebuffer with EOS
                    Log.d(TAG, "encoding media of length : "+  length);
                    Log.e("VideoEncoder","encoding media of length : "+  length);
                    videoEncoder.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }else{
                    ////enqueue bytebuffer
                    Log.d(TAG, "encoding media of length : "+  length);
                    Log.e("VideoEncoder","encoding media of length : "+  length);
                    videoEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, presentationTimeUs, 0);
                }
            }else{
                Log.d(TAG, "input buffer index less than zero");
                Log.e("VideoEncoder", "input buffer index less than zero");
            }
        }

        sendToMediaMuxer();
        //get outputByteBuffer
        //take data from outputByteBuffer
        //send to mediamuxer
    }

    public void sendToMediaMuxer(){
        if(videoEncoder==null) return;

        final ByteBuffer[] outputBuffers = videoEncoder.getOutputBuffers();

        final int outputBufferIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
        if(outputBufferIndex==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            Log.d(TAG, "output format changed");
            Log.e("VideoFormat", "output format changed");
            muxer.addVideoEncoder(this);
            muxer.startMuxing();

            Log.d(TAG, "muxer started");
            Log.e("VideoEncoder", "muxer started");
        }
        if(outputBufferIndex>=0){
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                // You shoud set output format to muxer here when you target Android4.3 or less
                // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                // therefor we should expand and prepare output format from buffer data.
                // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                Log.d(TAG, "in BUFFER_FLAG_CODEC_CONFIG");
                Log.e("VideoEncoder", "in BUFFER_FLAG_CODEC_CONFIG");
                bufferInfo.size = 0;
            }
            final ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            muxer.muxVideo(outputBuffer, bufferInfo);
            Log.d(TAG, "outputBufferIndex"+ outputBufferIndex);
            Log.e("VideoEncoder", "outputBufferIndex"+ outputBufferIndex);
            videoEncoder.releaseOutputBuffer(outputBufferIndex, false);
        }else{
            Log.d(TAG, "output buffer index less than zero");
            Log.e("VideoEncoder", "output buffer index less than zero");
        }

    }

    public MediaCodec getEncoder(){
        return videoEncoder;
    }

    public static VideoEncoder getInstance(){
        return instance;
    }

    public long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < prevOutputPTSUs)
            result = (prevOutputPTSUs - result) + result;
        prevOutputPTSUs = result;
        return result;
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    // async style mediacodec >21 and polling based for <21
}
