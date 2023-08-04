package muni.fi.bl.component;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static muni.fi.bl.component.StopWordsReader.READING_STOPWORDS_ERROR_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StopWordsReaderTest {

    private final StopWordsReader stopWordsReader = new StopWordsReader();

    @Test
    void loadStopWords() throws IOException {
        // prepare
        Resource resource = new ClassPathResource("testStopwords.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());

        // tested method
        List<String> stopWords = stopWordsReader.loadStopWords(inputStreamReader);

        // verify
        assertThat(stopWords.size(), equalTo(3));
        assertThat(stopWords.get(0), equalTo("test"));
        assertThat(stopWords.get(1), equalTo("stop"));
        assertThat(stopWords.get(2), equalTo("words"));
    }

    @Test
    void loadStopWordsNullResource() {
        // tested method
        Throwable exception = assertThrows(RuntimeException.class, () -> stopWordsReader.loadStopWords(null));

        // verify
        assertThat(exception.getMessage(), equalTo(READING_STOPWORDS_ERROR_MESSAGE + ". Resource not provided"));
    }

    @Test
    void loadStopWordsNonExistingResource() throws IOException {
        // prepare
        InputStreamReader readerMock = mock(InputStreamReader.class);
        when(readerMock.read(any(), anyInt(), anyInt())).thenThrow(new IOException());

        // tested method
        Throwable exception = assertThrows(RuntimeException.class, () -> stopWordsReader.loadStopWords(readerMock));

        // verify
        assertThat(exception.getMessage(), equalTo(READING_STOPWORDS_ERROR_MESSAGE));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }
}