package muni.fi.bl.component;

import muni.fi.bl.ProjectLoadResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CsvProjectParserTest {

    @Mock
    TextNormalizer textNormalizerMock;

    // csv file resource
    Resource csvFile;
    Resource badDateCsvFile;
    Resource missingColCsvFile;

    // tested class
    ProjectParser csvProjectParser;

    @BeforeEach
    void setUp() {
        openMocks(this);

        csvProjectParser = new CsvProjectParser(textNormalizerMock);
        when(textNormalizerMock.normalize(any())).thenReturn("this is normalized text");

        csvFile = new ClassPathResource("testCsvProjectFile.csv");
        badDateCsvFile = new ClassPathResource("badDateCsvProjectFile.csv");
        missingColCsvFile = new ClassPathResource("missingColCsvProjectFile.csv");
    }

    @Test
    void parseProjects() throws IOException {
        // prepare
        InputStream stream = csvFile.getInputStream();

        // tested method
        ProjectLoadResult result = csvProjectParser.parseProjects(stream);

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
        String sample = csvProjectParser.getSample();

        // verify
        String referenceSample = IOUtils.toString(csvFile.getInputStream(), StandardCharsets.UTF_8);
        assertThat(sample, equalTo(referenceSample));
    }

    @Test
    void parseInvalidDateFormatTest() throws IOException {
        // prepare
        InputStream stream = badDateCsvFile.getInputStream();

        // tested method
        ProjectLoadResult result = csvProjectParser.parseProjects(stream);

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
    void parseMissingColumnCsvFileTest() throws IOException {
        // prepare
        InputStream stream = missingColCsvFile.getInputStream();

        // tested method
        ProjectLoadResult result = csvProjectParser.parseProjects(stream);

        // verify
        assertThat(result.total(), equalTo(2));
        assertThat(result.successful(), equalTo(1));
        assertThat(result.failed(), equalTo(1));
        assertThat(result.projects().size(), equalTo(1));
        Project project = result.projects().get(0);
        assertThat(project.getTitle(), equalTo("Project title 2"));
        assertThat(project.getProcessedAnnotation(), equalTo("this is normalized text"));
    }
}