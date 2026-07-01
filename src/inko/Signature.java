/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import java.awt.image.BufferedImage;
import java.time.LocalDate;

public class Signature {
    // record in Java8 noch nicht verfügbar

    private final SignableDocument documentType;

    private BufferedImage sign;
    private LocalDate date;

    public Signature(SignableDocument documentType, BufferedImage sign) {
        this( documentType, sign, LocalDate.now() );
    }

    public Signature(SignableDocument documentType, BufferedImage sign, LocalDate date) {
        this.documentType = documentType;
        this.sign = sign;
        this.date = date == null ? LocalDate.now() : date;
    }

    public SignableDocument getDocumentType() {
        return documentType;
    }

    public BufferedImage getSign() {
        return sign;
    }

    public void setSign(BufferedImage sign) {
        this.sign = sign;
    }

    public LocalDate getDate() {
        return date == null ? LocalDate.now() : date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

}
