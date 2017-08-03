package dennymades.space.mediaencoder.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aqi00.lib.dialog.FileSelectFragment;

import java.io.File;
import java.util.Map;

import dennymades.space.mediaencoder.R;
import dennymades.space.mediaencoder.util.TrimVideoUtils;

/**
 * @author: sq
 * @date: 2017/7/27
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 视频裁剪演示界面
 */
public class TrimVideoActivity extends AppCompatActivity implements View.OnClickListener, FileSelectFragment.FileSelectCallbacks {

    private static final String TAG = TrimVideoActivity.class.getSimpleName();
    private Button mBtnStartTrim;

    // 需要裁剪的视频路径
    String videoPath;
    // 保存的路径
    String savePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_video);
        initView();
        initEvent();
    }

    private void initView() {
        mBtnStartTrim = ((Button) findViewById(R.id.btn_start_trim));
    }

    private void initEvent() {
        mBtnStartTrim.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_trim:
                FileSelectFragment.show(this, new String[]{"mp4"}, null);
                break;
        }
    }

    public void cutVideo() {
        TrimVideoUtils trimVideoUtils = TrimVideoUtils.getInstance();
        // 设置回调
        trimVideoUtils.setTrimCallBack(new TrimVideoUtils.TrimFileCallBack() {
            @Override // 裁剪失败回调
            public void trimError(int eType) {
                switch (eType) {
                    case TrimVideoUtils.FILE_NOT_EXISTS: // 文件不存在
                        System.out.println("视频文件不存在");
                        break;
                    case TrimVideoUtils.TRIM_STOP: // 手动停止裁剪
                        System.out.println("停止裁剪");
                        break;
                    case TrimVideoUtils.TRIM_FAIL:
                    default: // 裁剪失败
                        System.out.println("裁剪失败");
                        break;
                }
            }

            @Override // 裁剪成功回调
            public void trimCallback(boolean isNew, int startS, int endS, int vTotal, File file, File trimFile) {
                /**
                 * 裁剪回调
                 * @param isNew 是否新剪辑
                 * @param starts 开始时间(秒)
                 * @param ends 结束时间(秒)
                 * @param vTime 视频长度
                 * @param file 需要裁剪的文件路径
                 * @param trimFile 裁剪后保存的文件路径
                 */
                // ===========
                System.out.println("isNew : " + isNew);
                System.out.println("startS : " + startS);
                System.out.println("endS : " + endS);
                System.out.println("vTotal : " + vTotal);
                System.out.println("file : " + file.getAbsolutePath());
                System.out.println("trimFile : " + trimFile.getAbsolutePath());
            }
        });

        final File file = new File(videoPath); // 视频文件地址
        final File trimFile = new File(savePath);// 裁剪文件保存地址
        final int startS = 0; // 开始时间(秒数 - 非毫秒)
        final int endS = 5; // 结束时间(秒数 - 非毫秒)
        // 进行裁剪
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { // 开始裁剪
                    TrimVideoUtils.getInstance().startTrim(true, startS, endS, file, trimFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 设置回调为null
                    TrimVideoUtils.getInstance().setTrimCallBack(null);
                }
            }
        }).start();

        // 停止裁剪
//         TrimVideoUtils.getInstance().stopTrim();
    }

    @Override
    public void onConfirmSelect(String absolutePath, String fileName, Map<String, Object> map_param) {
        String path = String.format("%s/%s", absolutePath, fileName);
        Log.e(TAG, "path=" + path);
        videoPath = path;
        savePath = videoPath.substring(0, videoPath.lastIndexOf("/") + 1) + System.currentTimeMillis() + "_cut.mp4";
        cutVideo();
    }

    @Override
    public boolean isFileValid(String absolutePath, String fileName, Map<String, Object> map_param) {
        return true;
    }
}
