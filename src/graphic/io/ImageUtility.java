/*
 *
 */

package graphic.io;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageUtility {

    private ImageUtility() {}

    public static BufferedImage convertBytesToImage(byte[] imageBytes) throws IOException {
        try ( ByteArrayInputStream in = new ByteArrayInputStream( imageBytes )) {
            return ImageIO.read(in);
        }
    }

    public static Image scaleImage(Image image, int targetWidth, int targetHeight) {
        return image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_REPLICATE);
    }

    public static BufferedImage scaleImage(BufferedImage image, int targetWidth, int targetHeight) {
        if (image == null) {
            return null;
        }

        int originalWidth  = image.getWidth();
        int originalHeight = image.getHeight();

        double widthRatio  = (double) targetWidth  / originalWidth;
        double heightRatio = (double) targetHeight / originalHeight;

        // Das kleinere Verhältnis verwenden, um das Seitenverhältnis beizubehalten
        double ratio = Math.min(widthRatio, heightRatio);

        // Neue Breite und Höhe berechnen
        int newWidth  = (int) (originalWidth  * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Bild skalieren
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Das skalierte Bild in ein BufferedImage konvertieren
        BufferedImage scaledBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = scaledBufferedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return scaledBufferedImage;
    }

    /**
     * Skaliert das Bild auf ein Quadrat der angegebenen Seitelänge.
     * @param image Bild
     * @param size  Seitenlänge des Quadrates
     * @return      skaliertes Bild
     */
    public static BufferedImage scale(BufferedImage image, int size) {
        if (image == null) {
            return null;
        }
        if ( image.getHeight() > size || image.getWidth() > size ) {
            return ImageUtility.scaleImage(image, size, size);
        }
        return image;
    }

    /**
     * Spiegelt ein BufferedImage horizontal oder vertikal.
     * @param inputImage Das Originalbild
     * @param horizontal true für horizontal spiegeln, false für vertikal
     * @return Ein neues, gespiegeltes BufferedImage
     */
    public static BufferedImage flipImage(BufferedImage inputImage, boolean horizontal) {
        int width  = inputImage.getWidth();
        int height = inputImage.getHeight();

        // Erstelle ein neues Bild mit denselben Dimensionen und Typ
        BufferedImage flippedImage = new BufferedImage( width, height, inputImage.getType() );
        Graphics2D g2d = flippedImage.createGraphics();
        AffineTransform transformer = new AffineTransform();

        if (horizontal) {
            // Verschiebe das Bild nach rechts und skaliere x mit -1
            transformer.translate(width, 0);
            transformer.scale(-1, 1);
        } else {
            // Verschiebe das Bild nach unten und skaliere y mit -1
            transformer.translate(0, height);
            transformer.scale(1, -1);
        }

        // Zeichne das Originalbild mit der Transformation in das neue Bild
        g2d.drawImage(inputImage, transformer, null);
        g2d.dispose();

        return flippedImage;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // sicherstellen, dass das Bild vollständig geladen ist. ImageIcon nutzt intern einen MediaTracker und regelt das.
        img = new ImageIcon(img).getImage();

        // neues BufferedImage erstellen
        BufferedImage bimg = new BufferedImage(
            img.getWidth(null),
            img.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        );

        // Inhalt von Image in das BufferedImage zeichnen
        Graphics2D g2d = bimg.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return bimg;
    }

}
