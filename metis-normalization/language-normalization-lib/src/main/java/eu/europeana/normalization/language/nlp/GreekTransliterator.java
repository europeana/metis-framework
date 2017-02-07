package eu.europeana.normalization.language.nlp;

import com.ibm.icu.text.Transliterator;

/**
 * The choice for Greek alphabet transliteration to Latin. Follows the standard UNGEGN, which is
 * based in the older standard ELOT 743, in use in Greek passports, for example. This transliterator
 * also removes accents and transforms to lowercase (after the transliteration)
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 21 de Jun de 2012
 */
public class GreekTransliterator {
    Transliterator translit;

    /**
     * Creates a new instance of this class.
     */
    public GreekTransliterator() {
        translit = Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc; Lower");
    }

    /**
     * @param sourceText
     * @return transliterated text
     */
    public String transliterate(String sourceText) {
        String res = translit.transliterate(sourceText);
        return res;
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String source = "Βρίσκεστε στην αρχική σελίδα του: Δημόσιου Καταλόγου της Εθνικής Βιβλιοθήκης της Ελλάδος. Ο κατάλογός μας βασίζεται στο πληροφοριακό σύστημα Horizon της Dynix.";
        System.out.println(source);

        Transliterator translit = Transliterator.getInstance("Greek-Latin/UNGEGN");

        String res = translit.transliterate(source);

        System.out.println(res);

        translit = Transliterator.getInstance("Greek-Latin");
        res = translit.transliterate(source);

        System.out.println(res);
        translit = Transliterator.getInstance("Greek-Latin/BGN");
        res = translit.transliterate(source);

        System.out.println(res);
        translit = Transliterator.getInstance("Greek-Latin; nfd; [:nonspacing mark:] remove; nfc");
        res = translit.transliterate(source);
        System.out.println(res);

        translit = Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc");
        res = translit.transliterate(source);
        System.out.println(res);

// translit = Transliterator.getInstance("Greek-Latin; nfd; [:nonspacing mark:] remove; nfc");

        // Choice. UNGEGN is based in ELOT 743
        translit = Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc");
        res = translit.transliterate(source);
// translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// res = translit.transliterate(res);
        System.out.println(res);

        System.out.println("Greek-Latin/UNGEGN; nfc");
        translit = Transliterator.getInstance("Greek-Latin/UNGEGN; nfd");
        res = translit.transliterate(source);
// translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// res = translit.transliterate(res);
        System.out.println(res);

        res = translit.transliterate("Is this removin the latin 1 accents? á é ã ê");
// translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// res = translit.transliterate(res);
        System.out.println(res);
    }
}
