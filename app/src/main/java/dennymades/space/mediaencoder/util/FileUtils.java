package dennymades.space.mediaencoder.util;


import java.io.File;

/**
 * @author: sq
 * @date: 2017/8/1
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 文件管理工具（获取文件的扩展名、检测文件是否可用、判断文件类型、拼接路径）
 */
public class FileUtils {

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     * 获取文件的扩展名
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * 检测文件是否可用
     *
     * @param f
     * @return
     */
    public static boolean checkFile(File f) {
        if (f != null && f.exists() && f.canRead()
                && (f.isDirectory() || (f.isFile() && f.length() > 0))) {
            return true;
        }
        return false;
    }

    public static boolean checkFile(String filePath) {
        return checkFile(new File(filePath));
    }

    /**
     * 拼接路径
     *
     * @param paths concatPath("/mnt/sdcard", "/DCIM/Camera") => /mnt/sdcard/DCIM/Camera
     *              concatPath("/mnt/sdcard", "DCIM/Camera") => /mnt/sdcard/DCIM/Camera
     *              concatPath("/mnt/sdcard/", "/DCIM/Camera") => /mnt/sdcard/DCIM/Camera
     * @return
     */
    public static String concatPath(String... paths) {
        StringBuilder result = new StringBuilder();
        if (paths != null) {
            for (String path : paths) {
                if (path != null && path.length() > 0) {
                    int len = result.length();
                    boolean suffixSeparator = len > 0
                            && result.charAt(len - 1) == File.separatorChar;// 后缀是否是'/'
                    boolean prefixSeparator = path.charAt(0) == File.separatorChar;// 前缀是否是'/'
                    if (suffixSeparator && prefixSeparator) {
                        result.append(path.substring(1));
                    } else if (!suffixSeparator && !prefixSeparator) {// 补前缀
                        result.append(File.separatorChar);
                        result.append(path);
                    } else {
                        result.append(path);
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * 判断文件类型
     *
     * @param file 指定文件
     * @param type 文件类型，如.txt/.jpg/.mp4等等
     * @return
     */
    public static boolean isFileType(File file, String type) {
        String fileName = file.getName();
        if (!"".equals(fileName) && fileName != null) {
            String lowerCase = fileName.toLowerCase();
            String fileEnd = lowerCase.substring(lowerCase.lastIndexOf("."));
            if (type.equals(fileEnd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否图片
     *
     * @param fileName
     * @return
     */
    public static boolean isPic(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".bmp") || lowerCase.endsWith(".png")
                || lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")
                || lowerCase.endsWith(".gif");
    }

    /**
     * 是否压缩文件
     *
     * @param fileName
     * @return
     */
    public static boolean isCompresseFile(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".rar") || lowerCase.endsWith(".zip")
                || lowerCase.endsWith(".7z") || lowerCase.endsWith("tar")
                || lowerCase.endsWith(".iso");
    }

    /**
     * 是否音频
     *
     * @param fileName
     * @return
     */
    public static boolean isAudio(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".mp3") || lowerCase.endsWith(".wma")
                || lowerCase.endsWith(".mp4") || lowerCase.endsWith(".rm");
    }

    /**
     * 是否文档
     *
     * @param fileName
     * @return
     */
    public static boolean isDocument(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".doc") || lowerCase.endsWith(".docx")
                || lowerCase.endsWith("wps");
    }

    /**
     * 是否Pdf
     *
     * @param fileName
     * @return
     */
    public static boolean isPdf(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    /**
     * 是否Excel
     *
     * @param fileName
     * @return
     */
    public static boolean isXls(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".xls") || lowerCase.endsWith(".xlsx");
    }

    /**
     * 是否文本文档
     *
     * @param fileName
     * @return
     */
    public static boolean isTextFile(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".txt") || lowerCase.endsWith(".rtf");
    }

    /**
     * 是否ppt
     *
     * @param fileName
     * @return
     */
    public static boolean isPPt(String fileName) {
        if (null == fileName) return false;
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".ppt") || lowerCase.endsWith(".pptx");
    }
}
