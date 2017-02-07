package eu.europeana.normalization.language.nlp;

import java.util.Enumeration;

import com.ibm.icu.text.Transliterator;

/**
 * The choice for Greek alphabet transliteration to Latin. Follows the standard UNGEGN, which is
 * based in the older standard ELOT 743, in use in Greek passports, for example. This transliterator
 * also removes accents and transforms to lowercase (after the transliteration)
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 21 de Jun de 2012
 */
public class CyrillicTransliterator {
    Transliterator translit;

    /**
     * Creates a new instance of this class.
     */
    public CyrillicTransliterator() {
        translit = Transliterator.getInstance("Cyrillic-Latin; nfd; [:nonspacing mark:] remove; nfc; Lower");
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
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception {
        String source = "Топ-10 туристических объектов Латвии.";
        System.out.println(source);

        Transliterator translit = Transliterator.getInstance("Cyrillic-Latin");
        String res = translit.transliterate(source);
        System.out.println(res);

        translit = Transliterator.getInstance("Cyrillic-Latin; nfd; [:nonspacing mark:] remove; nfc; Lower");
        res = translit.transliterate(source);
        System.out.println(res);

        Enumeration availableIDs = Transliterator.getAvailableIDs();
        while (availableIDs.hasMoreElements()) {
            System.out.println(availableIDs.nextElement());
        }

// translit = Transliterator.getInstance("Greek-Latin");
// res = translit.transliterate(source);
//
// System.out.println(res);
// translit = Transliterator.getInstance("Greek-Latin/BGN");
// res = translit.transliterate(source);
//
// System.out.println(res);
// translit = Transliterator.getInstance("Greek-Latin; nfd; [:nonspacing mark:] remove; nfc");
// res = translit.transliterate(source);
// System.out.println(res);
//
// translit =
// Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc");
// res = translit.transliterate(source);
// System.out.println(res);
//
// // translit = Transliterator.getInstance("Greek-Latin; nfd; [:nonspacing mark:] remove; nfc");
//
// // Choice. UNGEGN is based in ELOT 743
// translit =
// Transliterator.getInstance("Greek-Latin/UNGEGN; nfd; [:nonspacing mark:] remove; nfc");
// res = translit.transliterate(source);
// // translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// // res = translit.transliterate(res);
// System.out.println(res);
//
// System.out.println("Greek-Latin/UNGEGN; nfc");
// translit = Transliterator.getInstance("Greek-Latin/UNGEGN; nfd");
// res = translit.transliterate(source);
// // translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// // res = translit.transliterate(res);
// System.out.println(res);
//
// res = translit.transliterate("Is this removin the latin 1 accents? á é ã ê");
// // translit = Transliterator.getInstance("[:Latin:]; nfd; Lower");
// // res = translit.transliterate(res);
// System.out.println(res);
    }
}
