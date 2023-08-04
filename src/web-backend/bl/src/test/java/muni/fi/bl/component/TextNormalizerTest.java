package muni.fi.bl.component;

import muni.fi.bl.exceptions.AppException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.openMocks;

class TextNormalizerTest {

    @Mock
    private StandardAnalyzer analyzerMock;

    private TextNormalizer textNormalizer;

    @BeforeEach
    void setUp() throws IOException {
        openMocks(this);

        Resource resource = new ClassPathResource("testStopwords.txt");
        Reader inputStreamReader = new InputStreamReader(resource.getInputStream());
        Analyzer analyzer = new StandardAnalyzer(inputStreamReader);

        textNormalizer = new TextNormalizer(analyzer);
    }

    @Test
    void normalize() {
        // prepare
        String text = """
                rude ,     test, new
                                
                input
                """;

        // tested method
        String normalized = textNormalizer.normalize(text);

        // verify
        assertThat(normalized, equalTo("rude new input"));
    }

    @Test
    void normalizeException() {
        // prepare
        String text = "text";
        given(analyzerMock.tokenStream(anyString(), anyString())).willAnswer(invocation -> {
            throw new IOException();
        });
        textNormalizer = new TextNormalizer(analyzerMock);

        // tested method
        Throwable exception = assertThrows(AppException.class, () -> textNormalizer.normalize(text));

        // verify
        assertThat(exception.getMessage(), equalTo("Couldn't process project annotation text"));
        assertThat(exception.getCause(), instanceOf(IOException.class));
    }
}