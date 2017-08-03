package dennymades.space.mediaencoder.util;

import android.content.Context;
import android.os.PowerManager;

/**
 * @author: sq
 * @date: 2017/7/26
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 屏幕唤醒工具
 */
public class WakeLockUtil {
	
	private static WakeLockUtil instance;
	PowerManager.WakeLock mWakeLock;
	
	private WakeLockUtil(){
		
	}
	
	public static WakeLockUtil getInstance(){
		if(null == instance){
			instance = new WakeLockUtil();
		}
		return instance;
	}

	@SuppressWarnings("deprecation")
	public void keepCreenWake(Context cx){
		if(mWakeLock == null){
			PowerManager pm = (PowerManager) cx.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
	                "XYTEST");
			mWakeLock.acquire();	
		}	  
	}
	
	public void cancleCreenWake(){
		if(mWakeLock != null){
		   mWakeLock.release();
		   mWakeLock = null;
		}
	}

}
