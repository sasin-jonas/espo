package muni.fi.bl.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for reading list of stopwords from a file.
 * Could possibly be list of anything that is on separate lines in a text file
 */
@Slf4j
@Component
public class StopWordsReader {

    public static final String READING_STOPWORDS_ERROR_MESSAGE = "Error while reading stopwords";

    /**
     * Loads list of stopwords from a file represented as a reader. The file has to contain single stopword on every line
     *
     * @param reader The file containing the stopwords represented as a reader
     * @return The list of loaded stopwords
     * @throws RuntimeException When reader is null or the file couldn't be opened for some reason (e.g. doesn't exist)
     */
    public List<String> loadStopWords(Reader reader) {
        if (reader != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(reader);
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            } catch (IOException e) {
                log.error(READING_STOPWORDS_ERROR_MESSAGE, e);
                throw new RuntimeException(READING_STOPWORDS_ERROR_MESSAGE, e);
            }
        } else {
            String message = READING_STOPWORDS_ERROR_MESSAGE + ". Resource not provided";
            log.error(message, message);
            throw new RuntimeException(message);
        }
    }
}
