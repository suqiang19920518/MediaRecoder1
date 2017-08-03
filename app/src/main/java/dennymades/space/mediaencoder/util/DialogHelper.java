package dennymades.space.mediaencoder.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;


/**
 * @author: sq
 * @date: 2017/7/26
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: dialog工具类
 */
public class DialogHelper {

    private static ProgressDialog waitDialog = null;

    /**
     * 获取一个dialog
     *
     * @param context 上下文
     * @return
     */
    public static AlertDialog.Builder getDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder;
    }


    /**
     * 显示一个耗时等待对话框
     *
     * @param context 上下文
     * @param message 提示信息
     */
    public static void showWaitDialog(final Context context, String title, String message) {
        if(waitDialog != null) {
            waitDialog.cancel();
            waitDialog.dismiss();
        }
        waitDialog = new ProgressDialog(context);
        waitDialog.setCancelable(false);
        waitDialog.setIndeterminate(true);//进度条采用不明确显示进度的‘模糊模式’

        waitDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }
                return false;
            }
        });

        if(!TextUtils.isEmpty(title)) {
            waitDialog.setTitle(title);
        }

        if (!TextUtils.isEmpty(message)) {
            waitDialog.setMessage(message);
        }
        waitDialog.show();

    }

    /**
     * 隐藏一个耗时等待对话框
     */
    public void hiddenWaitDialog() {
        if(waitDialog != null && waitDialog.isShowing()) {
            waitDialog.dismiss();
        }

        waitDialog = null;
    }

    /**
     * 显示一个信息对话框
     *
     * @param context         上下文
     * @param title           标题
     * @param message         提示信息
     * @param onClickListener 确定按钮的监听器
     */
    public static void showMessageDialog(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", onClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }


    /**
     * 显示信息对话框
     *
     * @param context               上下文
     * @param title                 标题
     * @param message               提示信息
     * @param onOkClickListener     确定按钮的监听器
     * @param onCancleClickListener 取消按钮的监听器
     */
    public static void showConfirmDialog(Context context, String title, String message, DialogInterface.OnClickListener onOkClickListener, DialogInterface.OnClickListener onCancleClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", onOkClickListener);
        builder.setNegativeButton("取消", onCancleClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }

    /**
     * 显示列表对话框
     *
     * @param context             上下文
     * @param title               标题
     * @param arrays              所需要显示的列表内容
     * @param onItemClickListener item选中的监听器
     */
    public static void showListDialog(Context context, String title, String[] arrays, DialogInterface.OnClickListener onItemClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setItems(arrays, onItemClickListener);
        builder.setPositiveButton("确定", null);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }


    /**
     * 显示单选对话框
     *
     * @param context             上下文
     * @param title               标题
     * @param arrays              所需要显示的列表内容
     * @param selectIndex         默认选中item的位置，从0开始。如果设置为-1，则无选中项
     * @param onItemClickListener item选中的监听器
     * @param onOkClickListener   确定按钮的监听器
     */
    public static void showSingleChoiceDialog(Context context, String title, String[] arrays, int selectIndex, DialogInterface.OnClickListener onItemClickListener, DialogInterface.OnClickListener onOkClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setSingleChoiceItems(arrays, selectIndex, onItemClickListener);
        builder.setPositiveButton("确定", onOkClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }

    /**
     * 显示单选对话框
     *
     * @param context               上下文
     * @param title                 标题
     * @param arrays                所需要显示的列表内容
     * @param selectIndex           默认选中item的位置，从0开始。如果设置为-1，则无选中项
     * @param onItemClickListener   item选中的监听器
     * @param onOkClickListener     确定按钮的监听器
     * @param onCancleClickListener 取消按钮的监听器
     */
    public static void showSingleChoiceDialog(Context context, String title, String[] arrays, int selectIndex, DialogInterface.OnClickListener onItemClickListener, DialogInterface.OnClickListener onOkClickListener, DialogInterface.OnClickListener onCancleClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setSingleChoiceItems(arrays, selectIndex, onItemClickListener);
        builder.setPositiveButton("确定", onOkClickListener);
        builder.setNegativeButton("取消", onCancleClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }

    /**
     * 显示多选对话框
     *
     * @param context                    上下文
     * @param title                      标题
     * @param arrays                     所需要显示的列表内容
     * @param selectFlag                 长度跟arrays字符串长度一致，保存有哪些item处于选中状态，true表示选中，false表示未选中，可以传入null表示都未选中
     * @param onMultiChoiceClickListener item选中的监听器
     * @param onOkClickListener          确定按钮的监听器
     */
    public static void showMultiChoiceDialog(Context context, String title, String[] arrays, boolean[] selectFlag, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener, DialogInterface.OnClickListener onOkClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setMultiChoiceItems(arrays, selectFlag, onMultiChoiceClickListener);
        builder.setPositiveButton("确定", onOkClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }

    /**
     * 显示多选对话框
     *
     * @param context                    上下文
     * @param title                      标题
     * @param arrays                     所需要显示的列表内容
     * @param selectFlag                 长度跟arrays字符串长度一致，保存有哪些item处于选中状态，true表示选中，false表示未选中，可以传入null表示都未选中
     * @param onMultiChoiceClickListener item选中的监听器
     * @param onOkClickListener          确定按钮的监听器
     * @param onCancleClickListener      取消按钮的监听器
     */
    public static void showMultiChoiceDialog(Context context, String title, String[] arrays, boolean[] selectFlag, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener, DialogInterface.OnClickListener onOkClickListener, DialogInterface.OnClickListener onCancleClickListener) {
        AlertDialog.Builder builder = getDialog(context);
        builder.setMultiChoiceItems(arrays, selectFlag, onMultiChoiceClickListener);
        builder.setPositiveButton("确定", onOkClickListener);
        builder.setNegativeButton("取消", onCancleClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.show();
    }

}
