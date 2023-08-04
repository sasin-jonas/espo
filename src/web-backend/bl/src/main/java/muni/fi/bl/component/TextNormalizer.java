package muni.fi.bl.component;

import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.AppException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for text normalization and stopwords removal
 */
@Slf4j
@Component
public class TextNormalizer {

    public static final String FIELD_NAME = "annotation";

    private final Analyzer analyzer;

    public TextNormalizer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    /**
     * Normalizes text, removes non-alfa-numerical characters and removes stopwords using lucene StandardAnalyzer
     *
     * @param annotation The text to normalize
     * @return Normalized text in form of space-separated words
     */
    public String normalize(String annotation) {
        return String.join(" ", analyze(
                annotation.replaceAll("\\s+", " ")));
    }

    private List<String> analyze(String text) {
        List<String> result = new ArrayList<>();
        try (TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text)) {
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result.add(attr.toString());
            }
        } catch (IOException e) {
            String message = "Couldn't process project annotation text";
            log.error(message, e);
            throw new AppException(message, e);
        }
        return result;
    }

}
