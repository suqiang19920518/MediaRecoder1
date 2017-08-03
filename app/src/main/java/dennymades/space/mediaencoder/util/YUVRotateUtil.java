package dennymades.space.mediaencoder.util;

/**
 * @author: sq
 * @date: 2017/6/28
 * @corporation: 深圳市思迪信息科技有限公司
 * @description: 图像旋转工具类
 */
public class YUVRotateUtil {

    /**
     * 旋转90度
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {

        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }

        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    /**
     * 旋转180度
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;

        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }

        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    /**
     * 旋转270度
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (imageWidth != nWidth || imageHeight != nHeight) {
            nWidth = imageWidth;
            nHeight = imageHeight;
            wh = imageWidth * imageHeight;
            uvHeight = imageHeight >> 1;//uvHeight = height / 2
        }

        //旋转Y
        int k = 0;
        for (int i = 0; i < imageWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < imageHeight; j++) {
                yuv[k] = data[nPos + i];
                k++;
                nPos += imageWidth;
            }
        }

        for (int i = 0; i < imageWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                yuv[k] = data[nPos + i];
                yuv[k + 1] = data[nPos + i + 1];
                k += 2;
                nPos += imageWidth;
            }
        }
        //这一部分可以直接旋转270度，但是图像颜色不对
//	    // Rotate the Y luma
//	    int i = 0;
//	    for(int x = imageWidth-1;x >= 0;x--)
//	    {
//	        for(int y = 0;y < imageHeight;y++)
//	        {
//	            yuv[i] = data[y*imageWidth+x];
//	            i++;
//	        }
//
//	    }
//	    // Rotate the U and V color components
//		i = imageWidth*imageHeight;
//	    for(int x = imageWidth-1;x > 0;x=x-2)
//	    {
//	        for(int y = 0;y < imageHeight/2;y++)
//	        {
//	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
//	            i++;
//	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
//	            i++;
//	        }
//	    }

        return rotateYUV420Degree180(yuv, imageWidth, imageHeight);
    }


    public static byte[] cropYUV420(byte[] data, int imageW, int imageH, int newImageH) {
        int cropH;
        int i, j, count, tmp;
        byte[] yuv = new byte[imageW * newImageH * 3 / 2];

        cropH = (imageH - newImageH) / 2;

        count = 0;
        for (j = cropH; j < cropH + newImageH; j++) {
            for (i = 0; i < imageW; i++) {
                yuv[count++] = data[j * imageW + i];
            }
        }

        //Cr Cb
        tmp = imageH + cropH / 2;
        for (j = tmp; j < tmp + newImageH / 2; j++) {
            for (i = 0; i < imageW; i++) {
                yuv[count++] = data[j * imageW + i];
            }
        }

        return yuv;
    }

}
