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

import static inko.SignatureField.*;

public enum SignableDocument {

    BERATUNG(SIGN_BERATUNG, DATE_BERATUNG),
    BINDUNG(SIGN_BINDUNG, DATE_BINDUNG),
    MEHRKOSTEN(SIGN_MEHRKOSTEN, DATE_MEHRKOSTEN);

    private final SignatureField fieldImg;
    private final SignatureField fieldDate;

    SignableDocument(SignatureField fieldImg, SignatureField fieldDate) {
        this.fieldImg  = fieldImg;
        this.fieldDate = fieldDate;
    }

    public SignatureField getImgField() {
        return fieldImg;
    }

    public SignatureField getDateField() {
        return fieldDate;
    }

}
