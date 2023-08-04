package muni.fi.bl.component;

import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.service.impl.ProjectLoadResult;
import muni.fi.dal.entity.Project;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class JsonProjectParserTest {

    @Mock
    TextNormalizer textNormalizerMock;
    @Mock
    InputStream streamMock;

    // json file resource
    Resource jsonFile;
    Resource badDateJsonFile;
    Resource missingPropertyJsonFile;

    // tested class
    ProjectParser jsonProjectParser;

    @BeforeEach
    void setUp() {
        openMocks(this);

        jsonProjectParser = new JsonProjectParser(textNormalizerMock);
        when(textNormalizerMock.normalize(any())).thenReturn("this is normalized text");

        jsonFile = new ClassPathResource("testJsonProjectFile.json");
        badDateJsonFile = new ClassPathResource("badDateJsonProjectFile.json");
        missingPropertyJsonFile = new ClassPathResource("missingPropertyJsonProjectFile.json");
    }

    @Test
    void parseProjects() throws IOException {
        // prepare
        InputStream stream = jsonFile.getInputStream();

        // tested method
        ProjectLoadResult result = jsonProjectParser.parseProjects(stream);

        // verify
        assertThat(result.successful(), equalTo(2));
        assertThat(result.total(), equalTo(2));
        assertThat(result.failed(), equalTo(0));
        assertThat(result.projects().size(), equalTo(2));
        // check a few projects parameters
        Project project1 = result.projects().get(0);
        Project project2 = result.projects().get(1);
        assertThat(project1.getTitle(), equalTo("Project title 1"));
        assertThat(project2.getTitle(), equalTo("Project title 2"));
        assertThat(project1.getProcessedAnnotation(), equalTo("this is normalized text"));
        assertThat(project2.getProcessedAnnotation(), equalTo("this is normalized text"));
    }

    @Test
    void getSample() throws IOException {
        // tested method
        String sample = jsonProjectParser.getSample();

        // verify
        String referenceSample = IOUtils.toString(jsonFile.getInputStream(), StandardCharsets.UTF_8);
        assertThat(sample, equalTo(referenceSample));
    }

    @Test
    void parseInvalidDateFormatTest() throws IOException {
        // prepare
        InputStream stream = badDateJsonFile.getInputStream();

        // tested method
        ProjectLoadResult result = jsonProjectParser.parseProjects(stream);

        // verify
        assertThat(result.total(), equalTo(2));
        assertThat(result.successful(), equalTo(1));
        assertThat(result.failed(), equalTo(1));
        assertThat(result.projects().size(), equalTo(1));
        Project project = result.projects().get(0);
        assertThat(project.getTitle(), equalTo("Project title 2"));
        assertThat(project.getProcessedAnnotation(), equalTo("this is normalized text"));
    }

    @Test
    void missingPropertyParseTest() throws IOException {
        // prepare
        InputStream stream = missingPropertyJsonFile.getInputStream();

        // tested method
        ProjectLoadResult result = jsonProjectParser.parseProjects(stream);

        // verify
        assertThat(result.total(), equalTo(2));
        assertThat(result.successful(), equalTo(1));
        assertThat(result.failed(), equalTo(1));
        assertThat(result.projects().size(), equalTo(1));
        Project project = result.projects().get(0);
        assertThat(project.getTitle(), equalTo("Project title 2"));
        assertThat(project.getProcessedAnnotation(), equalTo("this is normalized text"));
    }

    @Test
    void errorReadingStreamTest() throws IOException {
        // prepare
        when(streamMock.readAllBytes()).thenThrow(new IOException());

        // tested method
        Throwable exception = assertThrows(AppException.class, () -> jsonProjectParser.parseProjects(streamMock));

        // verify
        assertThat(exception.getCause(), instanceOf(IOException.class));
        assertThat(exception.getMessage(), equalTo("Error reading JSON file while uploading projects"));

    }
}