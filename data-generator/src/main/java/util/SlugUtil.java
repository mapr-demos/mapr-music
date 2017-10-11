package util;

import com.ibm.icu.text.Transliterator;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    /**
     * Separates accents from their base characters, removes the accents, and then puts the remaining text into an
     * unaccented form.
     */
    private static final String ICU4J_TRANSLITERATOR_ID = "Any-Latin; NFD; [:Nonspacing Mark:] Remove";
    private static final Transliterator transliterator = Transliterator.getInstance(ICU4J_TRANSLITERATOR_ID);

    /**
     * Converts specified string to it's slug representation, which can be used to generate readable and SEO-friendly
     * URLs.
     *
     * @param input string, which will be converted.
     * @return slug representation of string, which can be used to generate readable and SEO-friendly
     * URLs.
     */
    public static String toSlug(String input) {

        String transliterated = transliterator.transform(input);
        String noWhitespace = WHITESPACE.matcher(transliterated).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");

        return slug.toLowerCase(Locale.ENGLISH);
    }

}
