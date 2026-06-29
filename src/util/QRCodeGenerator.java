/*
 *
 */

package util;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    private QRCodeGenerator() {}

    /**
     * Generiert einen QR-Code als BufferedImage.
     * @param text   Der Inhalt des QR-Codes (z. B. die Server-URL mit ID)
     * @param width  Breite des QR-Codes in Pixeln
     * @param height Höhe des QR-Codes in Pixeln
     * @return BufferedImage des QR-Codes
     * @throws WriterException Falls bei der Generierung etwas schiefgeht
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        // Optionale Einstellungen für eine bessere Erkennung und UTF-8 Support
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // Randdicke (1 ist recht schmal und platzsparend)

        // Hohe Fehlerkorrektur (L), falls die Kamera des Tablets nicht die Beste ist oder Reflexionen auf dem Bildschirm auftreten
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        // BitMatrix erzeugen
        MultiFormatWriter barcodeWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        // Die ZXing-JavaSE-Erweiterung wandelt die Matrix direkt in ein BufferedImage um
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

}
