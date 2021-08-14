package io.cruii.bilibili.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author cruii
 * Created on 2021/08/13
 */
public class QrCodeGenerator {

    /**
     * 生成二维码，写到本地
     *
     * @param text     二维码需要包含的信息
     * @param width    二维码宽度
     * @param height   二维码高度
     * @param filePath 图片保存路径
     * @throws WriterException
     * @throws IOException
     */
    public static void generateQrCode(String text, int width, int height, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = FileSystems.getDefault().getPath(filePath);

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

    }

    public static byte[] generateQrCode(String text, int size) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public static byte[] generateQrCode(String text, int size, int margin) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        //编码、容错率、二维码边框宽度，这里文档说设置0-4
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 0);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        updateBit(bitMatrix, margin);

        //因为二维码生成时，白边无法控制，去掉原有的白边，再添加自定义白边后，二维码大小与size大小就存在差异了，
        //为了让新生成的二维码大小还是size大小，根据size重新生成图片
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(bitMatrix);

        //根据size放大、缩小生成的二维码
        bi = zoomInImage(bi, size, size);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public static byte[] generateQrCode(String text, int size, boolean removeBorder) throws WriterException, IOException {
        if (removeBorder) {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 0);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
            bitMatrix = deleteWhite(bitMatrix);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", pngOutputStream);
            return pngOutputStream.toByteArray();
        } else {
            return generateQrCode(text, size);
        }
    }
    /**
     * 因为二维码边框设置那里不起作用，不管设置多少，都会生成白边，所以根据网上
     * 的例子进行修改，自定义控制白边宽度，该方法生成自定义白边框后的bitMatrix；
     */
    private static BitMatrix updateBit(BitMatrix matrix, int margin) {
        int tempM = margin * 2;
        //获取二维码图案的属性
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + tempM;
        int resHeight = rec[3] + tempM;
        // 按照自定义边框生成新的BitMatrix
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        //循环，将二维码图案绘制到新的bitMatrix中
        for (int i = margin; i < resWidth - margin; i++) {
            for (int j = margin; j < resHeight - margin; j++) {
                if (matrix.get(i - margin + rec[0], j - margin + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }
        return resMatrix;
    }

    /**
     * 图片放大缩小
     */
    public static BufferedImage zoomInImage(BufferedImage originalImage, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, originalImage.getType());
        Graphics g = newImage.getGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return newImage;
    }


    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }
}