package muni.fi.bl.component;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.service.impl.ProjectLoadResult;
import muni.fi.dal.entity.Author;
import muni.fi.dal.entity.Department;
import muni.fi.dal.entity.Project;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Qualifier("csvParser")
public class CsvProjectParser implements ProjectParser {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy");
    public static final CSVParserBuilder CSV_PARSER = new CSVParserBuilder().withSeparator(';');

    private final TextNormalizer textNormalizer;

    public CsvProjectParser(TextNormalizer textNormalizer) {
        this.textNormalizer = textNormalizer;
    }

    @Override
    public ProjectLoadResult parseProjects(InputStream stream) {
        var lines = readAllLines(stream);
        List<Project> projects = new ArrayList<>();
        int i = 0;
        int failed = 0;
        int successful = 0;
        int columnCount = 0;
        for (var line : lines) {
            if (i == 0) {
                log.info("Loaded project CSV columns: " + String.join(",", line));
                columnCount = line.length;
            } else if (line.length != columnCount) {
                log.error("Project record has invalid number of columns (should be {} but is {}): {}", columnCount, line.length, line);
                failed++;
            } else {
                if (line[13].equals("EN")) {
                    try {
                        projects.add(resolveCsvProject(line));
                        successful++;
                    } catch (RuntimeException e) {
                        log.error("Failed to process project record: {}", line, e);
                        failed++;
                    }
                }
            }
            i++;
        }
        return new ProjectLoadResult(i - 1, successful, failed, projects);
    }

    @Override
    public String getSample() {
        return """
                Id;regCode;title;author;uco;state;dateBegin;dateEnd;muRole;investor;hs;department;annotation;annotationLanguage;annotationTransl;authorType
                000001;0001/2022;Project title 1;Ing. John Doe;00000;active;01.01.2024;30.06.2028;Beneficiary-coordinator;Investor 1;LF;Department of Histology and Embryology;Some lengthy annotation 1;EN;;academic, employee
                000002;0002/2022;Project title 2;Ing. Jenna Doe;00001;proposed;01.07.2023;30.06.2028;Partner;Investor 2;LF;International Clinical Research Centre;Some lengthy annotation 2;EN;;academic, employee
                """;
    }

    private List<String[]> readAllLines(InputStream stream) {
        try (Reader reader = new InputStreamReader(stream)) {
            try (CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(CSV_PARSER.build())
                    .build()) {
                return csvReader.readAll();
            }
        } catch (IOException | CsvException e) {
            String message = "Error reading csv file";
            log.error(message, e);
            throw new AppException(message, e);
        }
    }

    private Project resolveCsvProject(String[] line) {
        var project = new Project();

        project.setProjId(line[0]);
        project.setRegCode(line[1]);
        project.setTitle(line[2]);
        project.setState(line[5]);
        project.setDateBegin(DateTime.parse(line[6], DATE_TIME_FORMATTER));
        project.setDateEnd(DateTime.parse(line[7], DATE_TIME_FORMATTER));
        project.setMuniRole(line[8]);
        project.setInvestor(line[9]);
        project.setAnnotation(line[12]);
        project.setProcessedAnnotation(textNormalizer.normalize(line[12]));

        Author author = new Author();
        author.setName(line[3]);
        author.setUco(line[4]);
        author.setType(line[15]);
        project.setAuthor(author);

        Department department = new Department();
        department.setOrgUnit(line[10]);
        department.setDepartmentName(line[11]);
        project.setDepartment(department);

        return project;
    }
}
