package dennymades.space.mediaencoder.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author: sq
 * @date: 2017/7/25
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 文件管理工具（生成媒体文件、删除文件、复制文件、解压缩文件、读取asset目录下文件、
 * 读取普通文件、获取文件大小、转换文件大小）
 */
public class FileManager {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * 生成媒体文件
     *
     * @param mediaType 1——图片    2——视频
     * @param context
     * @return 媒体文件
     */
    public static File getOutputMediaFile(int mediaType, Context context) {

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraSample");

        // Create the storage directory if it does not exist
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d(TAG, "error creating directory");
//                return null;
//            }
//        }

        //generate unique filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (mediaType == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(context.getExternalCacheDir().getAbsolutePath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_" + timeStamp + ".jpg");
        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(context.getExternalCacheDir().getAbsolutePath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * 生成媒体文件
     *
     * @param mediaType 1——图片    2——视频
     * @param context
     * @return 媒体文件路径
     */
    public static String getOutputMediaPath(int mediaType, Context context) {

        String outputFile = null;

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return outputFile;
        }

        // Create the storage directory
//        String parentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
//                File.separator + "CameraSample";

        String parentDir = context.getExternalCacheDir().getAbsolutePath();//解决部分手机读取内存受限（如华为）

        //generate unique filename
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        if (mediaType == MEDIA_TYPE_IMAGE) {
            outputFile = parentDir + File.separator + "IMG_" + timeStamp + ".jpg";

        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            outputFile = parentDir + File.separator + "VID_" + timeStamp + ".mp4";

        }

        return outputFile;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的所有文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 删除文件夹以及目录下的所有空文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteEmptyDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除空子文件
                try {
                    if (getFileSize(files[i]) == 0) {
                        flag = deleteFile(files[i].getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!flag) break;
            } else {
                //删除空子目录
                try {
                    if (getFileSizes(files[i]) == 0) {
                        flag = deleteDirectory(files[i].getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!flag) break;
            }
        }
        if (!flag) return false;
        return true;

    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 复制单个文件
     *
     * @param fromFile 源文件
     * @param toFile   目标文件
     * @return
     */
    public static boolean copySingleFile(String fromFile, String toFile) {

        boolean result = false;

        int size = 1 * 1024;

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(fromFile);
            out = new FileOutputStream(toFile);
            byte[] buffer = new byte[size];
            int bytesRead = -1;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    /**
     * 复制单个文件
     *
     * @param assetFile 被复制文件的输入流
     * @param sdFile    目标文件
     * @return
     */
    public static Boolean copySingleFile(InputStream assetFile, File sdFile) {
        boolean flags = false;
        try {
            FileOutputStream fos = new FileOutputStream(sdFile);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = assetFile.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            flags = true;
            fos.close();
            assetFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flags;
    }

    public static Boolean copySingleFile(InputStream assetFile, String sdFilePath) {
        return copySingleFile(assetFile, new File(sdFilePath));
    }

    /**
     * 复制文件夹中的所有文件
     *
     * @param fromFile 源文件夹
     * @param toFile   目标文件夹
     * @return
     */
    public static boolean copyAllFiles(String fromFile, String toFile) {

        File[] currentFiles;
        File root = new File(fromFile);
        if (!root.exists()) {
            return false;
        }
        currentFiles = root.listFiles();
        File targetDir = new File(toFile);

        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                copyAllFiles(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");

            } else {
                copySingleFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
            }
        }

        return true;
    }

    /**
     * 读取asset目录下文件
     *
     * @param mContext 上下文
     * @param fileName 文件名
     * @param code     字符串编码，可以设置为"UTF-8"
     * @return
     */
    public static String readAssetFile(Context mContext, String fileName, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(fileName);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 读取文件
     *
     * @param file        需要读取的文件
     * @param charsetName 字符串编码
     * @return 如何文件为空，则返回null，否则返回文件内容
     */
    public static String readFile(File file, String charsetName) {
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return fileContent.toString();
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
        return fileContent.toString();
    }

    public static String readFile(String filePath, String charsetName) {
        return readFile(new File(filePath), charsetName);
    }

    public static String readFile(File file) {
        return readFile(file, "utf-8");
    }


    /**
     * 解压缩一个文件
     *
     * @param zipFile    压缩文件
     * @param folderPath 解压缩的目标目录
     * @throws IOException 当解压缩过程出错时抛出
     */
    public static void upZipFile(File zipFile, String folderPath) {
//        String strZipName = zipFile.getName();
//        folderPath += "/" + strZipName.substring(0, strZipName.lastIndexOf("."));
//        File desDir = new File(folderPath);
//        if (!desDir.exists())
//        {
//            desDir.mkdirs();
//        }

        ZipFile zf;
        try {
            zf = new ZipFile(zipFile);
            for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                if (entry.isDirectory()) {
                    String dirstr = entry.getName();
                    dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                    File f = new File(dirstr);
                    f.mkdir();
                    continue;
                }

                InputStream in = zf.getInputStream(entry);
                String str = folderPath + File.separator + entry.getName();
                str = new String(str.getBytes("8859_1"), "GB2312");
                File desFile = new File(str);
                if (!desFile.exists()) {
                    File fileParentDir = desFile.getParentFile();
                    if (!fileParentDir.exists()) {
                        fileParentDir.mkdirs();
                    }
                    desFile.createNewFile();
                }

                OutputStream out = new FileOutputStream(desFile);
                int BUFF_SIZE = 1024 * 1024; // 1M Byte
                byte buffer[] = new byte[BUFF_SIZE];
                int realLength;
                while ((realLength = in.read(buffer)) > 0) {
                    out.write(buffer, 0, realLength);
                }
                in.close();
                out.close();
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void upZipFile(String zipFilePath, String folderPath) {
        upZipFile(new File(zipFilePath), folderPath);
    }

    /**
     * 压缩一个文件
     *
     * @param srcFilePath 需要压缩文件的路径
     * @param zipFilePath 压缩后文件的路径
     * @throws IOException 当压缩过程出错时抛出
     */
    public static void zipFolder(String srcFilePath, String zipFilePath) throws Exception {
        // 创建Zip包
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFilePath));
        // 打开要输出的文件
        File file = new File(srcFilePath);
        // 压缩
        zipFiles(file.getParent() + File.separator, file.getName(), outZip);
        // 完成,关闭
        outZip.finish();
        outZip.close();
    }

    /**
     * 进行文件压缩
     *
     * @param folderPath 需要压缩文件的上一级目录
     * @param fileName   需要压缩文件的文件名
     * @param zipOut     Zip包
     * @throws Exception 当压缩过程出错时抛出
     */
    private static void zipFiles(String folderPath, String fileName, ZipOutputStream zipOut)
            throws Exception {
        if (zipOut == null) {
            return;
        }
        File file = new File(folderPath + fileName);
        // 判断是不是文件
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            FileInputStream inputStream = new FileInputStream(file);
            zipOut.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[100000];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
            inputStream.close();
            zipOut.closeEntry();
        } else {
            // 文件夹的方式,获取文件夹下的子文件
            String fileList[] = file.list();
            // 如果没有子文件, 则添加进去即可
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileName + File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            }
            // 如果有子文件, 遍历子文件
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderPath, fileName + File.separator + fileList[i], zipOut);
            }
        }
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
//            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    public static long getFileSize(String filePath) throws Exception {
        return getFileSize(new File(filePath));
    }

    /**
     * 获取指定文件夹大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File file) throws Exception {
        long size = 0;
        File flist[] = file.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    public static long getFileSizes(String filePath) throws Exception {
        return getFileSizes(new File(filePath));
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小
     *
     * @param length
     * @return
     */
    public static String formatFileLength(long length) {
        if (length >> 30 > 0L) {
            float sizeGb = Math.round(10.0F * (float) length / 1.073742E+009F) / 10.0F;
            return sizeGb + " GB";
        }
        if (length >> 20 > 0L) {
            float mbSize = Math.round(10.0F * (float) length / 1048576.0F) / 10.0F;
            return mbSize + " MB";
        }
        if (length >> 9 > 0L) {
            float sizekb = Math.round(10.0F * (float) length / 1024.0F) / 10.0F;
            return sizekb + " KB";
        }
        return length + " B";
    }

    /**
     * 将文件转换为字节数组
     *
     * @param filePath
     * @param seek
     * @param length
     * @return
     */
    public static byte[] readFlieToByte(String filePath, int seek, int length) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        if (length == -1) {
            length = (int) file.length();
        }

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            byte[] bs = new byte[length];
            randomAccessFile.seek(seek);
            randomAccessFile.readFully(bs);
            randomAccessFile.close();
            return bs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
