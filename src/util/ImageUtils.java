package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {

    // byte[] → BufferedImage
    public static BufferedImage byteArrayToImage(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return ImageIO.read(bais);
    }

    // BufferedImage → byte[]
    public static byte[] imageToByteArray(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
}
