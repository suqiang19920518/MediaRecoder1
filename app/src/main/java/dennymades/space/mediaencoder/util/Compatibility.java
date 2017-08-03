package dennymades.space.mediaencoder.util;

import android.os.Build;

/**
 * @author: sq
 * @date: 2017/7/26
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 版本兼容检测工具
 */
public class Compatibility {
    public static boolean isCompatible(int version){
        if(Build.VERSION.SDK_INT<=version)
            return true;
        else
            return false;
    }
}
