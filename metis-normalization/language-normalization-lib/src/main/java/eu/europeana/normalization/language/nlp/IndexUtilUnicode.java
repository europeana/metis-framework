package eu.europeana.normalization.language.nlp;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

/**
 * Utility class implementing methods for simplification of unicode strings. The resulting strings
 * are typically used for comparison, indexing, etc. Transforms to lower case and removes marks,
 * superflous spaces, accented characters, etc.
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since Aug 30, 2011
 */
public class IndexUtilUnicode {
    private static Pattern        cleanSupplusSpaces = Pattern.compile("\\s\\s+");
    private static Pattern        cleanAllSpaces     = Pattern.compile("\\s+");

    private static NormalizerPool normalizerPool     = new NormalizerPool();

    private IndexUtilUnicode() {
        // static methods only - hide constructor
    }

    /**
     * simplifies a string, without removing all spaces
     * 
     * @param s
     *            the string to simplify
     * @return the simplified string
     */
    public static String encode(String s) {
        return encode(s, false);
    }

    /**
     * simplifies a string, without removing all spaces
     * 
     * @param stringToEncode
     *            the string to simplify
     * @param removeAllSpaces
     *            should all spaces be removed
     * @return the simplified string
     */
    public static String encode(String stringToEncode, boolean removeAllSpaces) {
        String ret = normalizerPool.normalize(stringToEncode);
        if (removeAllSpaces) {
            ret = cleanAllSpaces.matcher(ret).replaceAll("");
        } else {
            ret = cleanSupplusSpaces.matcher(ret).replaceAll(" ");
        }
        return ret.trim();
    }

    private static class NormalizerPool {
        private static final int                                AVAILABLE_INSTANCES = 5;
        ArrayBlockingQueue<CyrillicGreekCombinedTransliterator> queue;

        /**
         * Creates a new instance of this class.
         */
        public NormalizerPool() {
            queue = new ArrayBlockingQueue<CyrillicGreekCombinedTransliterator>(AVAILABLE_INSTANCES);
            for (int i = 0; i < AVAILABLE_INSTANCES; i++)
                queue.add(new CyrillicGreekCombinedTransliterator());
        }

        /**
         * @param stringToEncode
         * @return normalized string
         */
        public String normalize(String stringToEncode) {
            try {
                CyrillicGreekCombinedTransliterator trl = queue.take();
                try {
                    return trl.transliterate(stringToEncode);
                } finally {
                    queue.add(trl);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
