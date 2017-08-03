package dennymades.space.mediaencoder.activity;

import android.content.DialogInterface;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

import dennymades.space.mediaencoder.R;
import dennymades.space.mediaencoder.encoder.AudioRecorderHandlerThread;
import dennymades.space.mediaencoder.encoder.VideoEncoder;
import dennymades.space.mediaencoder.util.Compatibility;
import dennymades.space.mediaencoder.util.DialogHelper;
import dennymades.space.mediaencoder.util.FileManager;
import dennymades.space.mediaencoder.util.HomeWatcher;
import dennymades.space.mediaencoder.util.Messages;
import dennymades.space.mediaencoder.util.PermissionUtils;
import dennymades.space.mediaencoder.util.WakeLockUtil;
import dennymades.space.mediaencoder.util.YUVRotateUtil;

/**
 * @author: sq
 * @date: 2017/7/26
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 多媒体录制界面——SurfaceView
 */
public class MediaRecordActivity2 extends AppCompatActivity implements Camera.PreviewCallback, View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = MediaRecordActivity2.class.getSimpleName();  // TAG = "MediaRecordActivity1"

    private AudioRecorderHandlerThread audioRecorderHandlerThread;

    private Camera mCamera;
    // 手机相机前置摄像头时候设置为1；后置摄像头的时候设置为0
    private int cameraId = 1;
    String bs = Build.MODEL;    //获取设备信息
    private SurfaceView mSurfaceView;
    private boolean mHasSurface = false;
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    private boolean isStart;

    private int previewWidth = 640;//此处宽、高置反了
    private int previewHeight = 480;//此处宽、高置反了
    private VideoEncoder encoder;
    private Button mBtnStart;
    private Button mBtnStop;

    private String outputFile;
    private String parentDirectory;
    private HomeWatcher mHomeWatcher;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Messages.MSG_CHANGE_BUTTON_ENABLED:
                    mBtnStop.setEnabled(true);
                    break;
                case Messages.MSG_RECORDING_START_CALLBACK:
                    Log.e(TAG, "message recording started callback in the UI thread");
                    break;
                case Messages.MSG_RECORDING_STOP_CALLBACK:
                    Log.e(TAG, "message recording stopped callback in the UI thread");
                    break;
            }
        }
    };

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_MULTI_PERMISSION:
                    openCamera();//初始化摄像头
                    doPreview();
                    break;
                default:
                    break;
            }
        }
    };
    private SurfaceHolder mSurfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_media_record);
        initView();
        initEvent();

        Point mScreenSize = new Point(0, 0);
        getWindowManager().getDefaultDisplay().getSize(mScreenSize);
        mScreenWidth = mScreenSize.x;
        mScreenHeight = mScreenSize.y;

    }

    @Override
    protected void onResume() {
        super.onResume();
        WakeLockUtil.getInstance().keepCreenWake(this);//保持屏幕一直唤醒
        outputFile = FileManager.getOutputMediaPath(FileManager.MEDIA_TYPE_VIDEO, this);
        parentDirectory = outputFile.substring(0, outputFile.lastIndexOf("/"));//上一级父目录
        audioRecorderHandlerThread = new AudioRecorderHandlerThread("Audio Recorder Thread", Process.THREAD_PRIORITY_URGENT_AUDIO, outputFile);
        audioRecorderHandlerThread.setCallback(mHandler);
        audioRecorderHandlerThread.start();
        encoder = audioRecorderHandlerThread.getVideoEncoder();

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                Log.e(TAG, "onHomePressed");
                finish();
            }

            @Override
            public void onHomeLongPressed() {
                Log.e(TAG, "onHomeLongPressed");
            }
        });
        mHomeWatcher.startWatch();//开始监听Home键
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseResource();
        WakeLockUtil.getInstance().cancleCreenWake();//取消屏幕唤醒
        FileManager.deleteEmptyDirectory(parentDirectory);//删除空文件
        audioRecorderHandlerThread.setCallback(null);
        if (Compatibility.isCompatible(17)) {
            //把MessageQueue消息池中所有的消息全部清空，无论是延迟消息,还是非延迟消息
            audioRecorderHandlerThread.quit();
        } else {
            //只会清空MessageQueue消息池中所有的延迟消息，并将消息池中所有的非延迟消息派发出去让Handler去处理
            audioRecorderHandlerThread.quitSafely();
        }
        mHomeWatcher.stopWatch();// 在onPause中停止监听，不然会报错
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResource();
        WakeLockUtil.getInstance().cancleCreenWake();//取消屏幕唤醒
        FileManager.deleteEmptyDirectory(parentDirectory);//删除空文件
        audioRecorderHandlerThread.setCallback(null);
        if (Compatibility.isCompatible(17)) {
            audioRecorderHandlerThread.quit();
        } else {
            audioRecorderHandlerThread.quitSafely();
        }

    }

    /**
     * 监听返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (isStart) {
                DialogHelper.showConfirmDialog(this, "提示", "您确定要退出，停止录制吗？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }, null);
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        mBtnStart = ((Button) findViewById(R.id.btn_start));
        mBtnStop = ((Button) findViewById(R.id.btn_stop));
        mSurfaceView = (SurfaceView) findViewById(R.id.main_surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    private void initEvent() {
        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
    }

    private void setCamera(Camera camera) {
        Camera.Parameters params = camera.getParameters();

        // FIXME 640 480
        params.setPreviewSize(640, 480);

        camera.setDisplayOrientation(90);

        if (bs.equals("N9180") || bs.equals("U9180")) {
            // 针对中兴N9180
            camera.setDisplayOrientation(270);
        } else {
            camera.setDisplayOrientation(90);
        }
        camera.setParameters(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                Log.e(TAG, "record start clicked");
                audioRecorderHandlerThread.startRecording();
                mHandler.sendEmptyMessageDelayed(Messages.MSG_CHANGE_BUTTON_ENABLED, 3000);//确保录制最短时间为3秒
                mBtnStart.setEnabled(false);
                isStart = true;
                break;
            case R.id.btn_stop:
                Log.e(TAG, "record stop clicked");
                releaseResource();//资源释放
                mBtnStop.setEnabled(false);
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHasSurface = true;
        //申请权限——相机、录音、内部存储
        PermissionUtils.requestMultiPermissions(this, mPermissionGrant, true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHasSurface = false;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Log.e("aa", "======onPreviewFrame========");
        //视频旋转后存储
        final byte[] tempData = YUVRotateUtil.rotateYUV420Degree270(bytes, previewWidth, previewHeight);

        try {
            encoder.encode(tempData, tempData.length, encoder.getPTSUs());
        } catch (Exception e) {
            Log.e("LivenessCaptureActivity", "waiting muxer start....");
        }

    }

    private void doPreview() {
        if (mCamera == null || !mHasSurface)
            return;
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();

            relayout();

        } catch (IOException e) {
            Log.e("error", Log.getStackTraceString(e));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogHelper.showMessageDialog(MediaRecordActivity2.this, "提示", "请授予相机和录音权限,否则不能进行录制", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MediaRecordActivity2.this.finish();
                        }
                    });

                }
            });
        }
    }

    private void relayout() {

        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();

        float scale = Math.min(mScreenWidth * 1.0f / previewSize.height, mScreenHeight * 1.0f / previewSize.width);
        int layout_width = (int) (scale * previewSize.height);
        int layout_height = (int) (scale * previewSize.width);
        System.out.println("layout_width:---" + layout_width + " layout_height----:" + layout_height);

        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(layout_width, layout_height);
        mSurfaceView.setLayoutParams(layout_params);

    }

    /**
     * 初始化摄像头
     */
    public void openCamera() {
        try {
            mCamera = Camera.open(cameraId);
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            setCamera(mCamera);
            relayout();
            mCamera.setPreviewCallback(this);
        } catch (Exception e) {
            Toast.makeText(this, "无法连接相机服务，请检查相机权限", Toast.LENGTH_LONG).show();
            FileManager.deleteFile(outputFile);//删除空媒体文件
            finish();
        }

    }

    /**
     * 停止录制，资源释放
     */
    public void releaseResource() {
        try {
            if (isStart) {
                audioRecorderHandlerThread.stopRecording(); //停止录制
                isStart = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCamera != null) {
                mCamera.lock();
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview(); //停止摄像头预览
                mCamera.release();
                mCamera = null;
                mSurfaceHolder.removeCallback(this);
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant, true);

    }

}
