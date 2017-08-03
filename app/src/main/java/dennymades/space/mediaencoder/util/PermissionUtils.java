package dennymades.space.mediaencoder.util;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dennymades.space.mediaencoder.R;

/**
 * @author: sq
 * @date: 2017/7/20
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 动态权限工具类
 */
public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();
    public static final int CODE_RECORD_AUDIO = 0;
    public static final int CODE_CAMERA = 1;

    public static final int CODE_READ_EXTERNAL_STORAGE = 2;
    public static final int CODE_WRITE_EXTERNAL_STORAGE = 3;
    public static final int CODE_MULTI_PERMISSION = 100;

    public static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String[] requestPermissions = {
            PERMISSION_RECORD_AUDIO,
            PERMISSION_CAMERA,
//            PERMISSION_READ_EXTERNAL_STORAGE,
//            PERMISSION_WRITE_EXTERNAL_STORAGE
    };


    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);
    }

    /**
     * Requests permission.
     * 每次申请一个权限
     *
     * @param activity
     * @param requestCode request code, e.g. if you need request CAMERA permission,parameters is PermissionUtils.CODE_CAMERA
     * @param isFinish    if to finish a activity or not
     */
    public static void requestPermission(final Activity activity, final int requestCode, PermissionGrant permissionGrant, boolean isFinish) {
        if (activity == null) {
            return;
        }

        Log.i(TAG, "requestPermission requestCode:" + requestCode);
        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            Log.w(TAG, "requestPermission illegal requestCode:" + requestCode);
            return;
        }

        final String requestPermission = requestPermissions[requestCode];

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以判断系统版本，低于23就不申请权限，直接做你想做的。permissionGrant.onPermissionGranted(requestCode);
//        if (Build.VERSION.SDK_INT < 23) {
//            permissionGrant.onPermissionGranted(requestCode);
//            return;
//        }

        int checkSelfPermission;
        try {
            // 检测权限
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
        } catch (RuntimeException e) {   //用户关闭（禁止）了申请的权限，程序崩溃处理
            Toast.makeText(activity, "please open this permission", Toast.LENGTH_SHORT)
                    .show();
            Log.e(TAG, "RuntimeException:" + e.getMessage());
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {  //权限未授予
            Log.i(TAG, "ActivityCompat.checkSelfPermission != PackageManager.PERMISSION_GRANTED");

            //用户选择了拒绝并且不再提醒，那么shouldShowRequestPermissionRationale方法会返回false
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                Log.i(TAG, "requestPermission shouldShowRequestPermissionRationale");
                shouldShowRationale(activity, requestCode, requestPermission, isFinish);

            } else {
                Log.d(TAG, "requestCameraPermission else");
                // 权限申请
                ActivityCompat.requestPermissions(activity, new String[]{requestPermission}, requestCode);
            }

        } else {  // 权限已授予
            Log.d(TAG, "ActivityCompat.checkSelfPermission ==== PackageManager.PERMISSION_GRANTED");
            permissionGrant.onPermissionGranted(requestCode);//接口回调
        }
    }

    /**
     * 每次申请多个权限，结果回调
     *
     * @param activity
     * @param permissions
     * @param grantResults
     * @param permissionGrant
     * @param isFinish        if to finish a activity or not
     */
    private static void requestMultiResult(Activity activity, String[] permissions, int[] grantResults,
                                           PermissionGrant permissionGrant, boolean isFinish) {

        if (activity == null) {
            return;
        }

        //TODO
        Log.d(TAG, "onRequestPermissionsResult permissions length:" + permissions.length);
        Map<String, Integer> perms = new HashMap<>();

        //统计所有申请权限中，未授予的权限
        ArrayList<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            Log.d(TAG, "permissions: [i]:" + i + ", permissions[i]" + permissions[i] + ",grantResults[i]:" + grantResults[i]);
            perms.put(permissions[i], grantResults[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permissions[i]);
            }
        }

        if (notGranted.size() == 0) {  //所有权限已授予
            permissionGrant.onPermissionGranted(CODE_MULTI_PERMISSION);
        } else {
            //弹出对话框，提示用户是否打开设置界面，进行剩余权限授予
            openSettingActivity(activity, "部分权限未授予，将不能进行录制，是否进行权限授予？", isFinish);
        }

    }

    /**
     * 一次申请多个权限
     *
     * @param activity
     * @param grant
     * @param isFinish if to finish a activity or not
     */
    public static void requestMultiPermissions(final Activity activity, PermissionGrant grant, final boolean isFinish) {

        final List<String> permissionsList = getNoGrantedPermission(activity, false);
        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(activity, true);

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return;
        }
        Log.d(TAG, "requestMultiPermissions permissionsList:" + permissionsList.size() + ",shouldRationalePermissionsList:" + shouldRationalePermissionsList.size());

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
                    CODE_MULTI_PERMISSION);
            Log.d(TAG, "showMessageOKCancel requestPermissions");

        } else if (shouldRationalePermissionsList.size() > 0) {

            DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(activity, shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]),
                            CODE_MULTI_PERMISSION);
                    Log.d(TAG, "showMessageOKCancel requestPermissions");
                }
            };

            DialogInterface.OnClickListener cancleListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isFinish) {
                        activity.finish();
                    }
                }
            };

            showMessageOKCancel(activity, "部分权限未授予，不能进行录制，是否进行授予？", okListener, cancleListener);

        } else {  //所有权限都已授予
            grant.onPermissionGranted(CODE_MULTI_PERMISSION);//接口回调
        }

    }


    private static void shouldShowRationale(final Activity activity, final int requestCode, final String requestPermission, final boolean isFinish) {
        //TODO
        String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{requestPermission},
                        requestCode);
                Log.d(TAG, "showMessageOKCancel requestPermissions:" + requestPermission);
            }
        };
        DialogInterface.OnClickListener cancleListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinish) {
                    activity.finish();
                }
            }
        };

        showMessageOKCancel(activity, permissionsHint[requestCode], okListener, cancleListener);
    }

    private static void showMessageOKCancel(final Activity context, String message, DialogInterface.OnClickListener okListener,
                                            DialogInterface.OnClickListener cancleListener) {

        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancleListener)
                .setCancelable(false)
                .create()
                .show();

    }

    /**
     * @param activity
     * @param requestCode  Need consistent with requestPermission
     * @param permissions
     * @param grantResults
     * @param isFinish     if to finish a activity or not
     */
    public static void requestPermissionsResult(final Activity activity, final int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults, PermissionGrant permissionGrant, boolean isFinish) {

        if (activity == null) {
            return;
        }
        Log.d(TAG, "requestPermissionsResult requestCode:" + requestCode);

        if (requestCode == CODE_MULTI_PERMISSION) {
            requestMultiResult(activity, permissions, grantResults, permissionGrant, isFinish);
            return;
        }

        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            Log.w(TAG, "requestPermissionsResult illegal requestCode:" + requestCode);
            Toast.makeText(activity, "illegal requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "onRequestPermissionsResult requestCode:" + requestCode + ",permissions:" + permissions.toString()
                + ",grantResults:" + grantResults.toString() + ",length:" + grantResults.length);

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult PERMISSION_GRANTED");
            //TODO success, do something, can use callback
            permissionGrant.onPermissionGranted(requestCode); //接口回调

        } else {
            //TODO hint user this permission function
            Log.i(TAG, "onRequestPermissionsResult PERMISSION NOT GRANTED");
            //TODO
            String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
            openSettingActivity(activity, permissionsHint[requestCode], isFinish);
        }

    }

    /**
     * 进入设置界面，进行权限授予
     *
     * @param activity
     * @param message
     */
    private static void openSettingActivity(final Activity activity, String message, final boolean isFinish) {
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //跳转至设置界面，进行权限授予
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Log.d(TAG, "getPackageName(): " + activity.getPackageName());
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        };
        DialogInterface.OnClickListener cancleListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinish) {
                    activity.finish();
                }
            }
        };

        showMessageOKCancel(activity, message, okListener, cancleListener);
    }


    /**
     * 获取所有未授予权限
     *
     * @param activity
     * @param isShouldRationale true: return no granted and shouldShowRequestPermissionRationale permissions,
     *                          false:return no granted and !shouldShowRequestPermissionRationale
     * @return
     */
    public static ArrayList<String> getNoGrantedPermission(Activity activity, boolean isShouldRationale) {

        ArrayList<String> permissions = new ArrayList<>();

        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];

            //TODO checkSelfPermission
            int checkSelfPermission = -1;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT)
                        .show();
                Log.e(TAG, "RuntimeException:" + e.getMessage());
                return null;
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "getNoGrantedPermission ActivityCompat.checkSelfPermission != PackageManager.PERMISSION_GRANTED:" + requestPermission);

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                    Log.d(TAG, "shouldShowRequestPermissionRationale if");
                    if (isShouldRationale) {
                        permissions.add(requestPermission);
                    }

                } else {

                    if (!isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                    Log.d(TAG, "shouldShowRequestPermissionRationale else");
                }

            }
        }

        return permissions;
    }

}
