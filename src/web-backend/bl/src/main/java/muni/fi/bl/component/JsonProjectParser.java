package muni.fi.bl.component;

import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.service.impl.ProjectLoadResult;
import muni.fi.dal.entity.Author;
import muni.fi.dal.entity.Department;
import muni.fi.dal.entity.Project;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("jsonParser")
public class JsonProjectParser implements ProjectParser {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final TextNormalizer textNormalizer;

    public JsonProjectParser(TextNormalizer textNormalizer) {
        this.textNormalizer = textNormalizer;
    }

    @Override
    public ProjectLoadResult parseProjects(InputStream stream) {
        String json;
        try {
            json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String message = "Error reading JSON file while uploading projects";
            log.error(message, e);
            throw new AppException(message, e);
        }

        JSONArray jsonProjects = new JSONArray(json);
        List<Project> projects = new ArrayList<>();
        int failed = 0;
        int successful = 0;
        for (Object o : jsonProjects) {
            JSONObject project = (JSONObject) o;
            try {
                String annotationLanguage = ((String) project.get("annotationLanguage")).toLowerCase();
                if (annotationLanguage.equals("en")) {
                    projects.add(resolveJsonProject(project));
                    successful++;
                }
            } catch (RuntimeException e) {
                log.error("Failed to process project record: {}", project, e);
                failed++;
            }

        }
        return new ProjectLoadResult(jsonProjects.length(), successful, failed, projects);
    }

    @Override
    public String getSample() {
        return """
                [
                    {
                        "Id": 1,
                        "regCode": "0001/2022",
                        "title": "Project title 1",
                        "author": "Ing. John Doe",
                        "uco": "00000",
                        "state": "active",
                        "dateBegin": "01.01.2024",
                        "dateEnd": "30.06.2028",
                        "muRole": "Beneficiary-coordinator",
                        "investor": "Investor 1",
                        "hs": "LF",
                        "department": "Department of Histology and Embryology",
                        "annotation": "Some very lengthy annotation 2",
                        "annotationLanguage": "EN",
                        "annotationTransl": "",
                        "authorType": "academic, employee"
                    },
                    {
                        "Id": 2,
                        "regCode": "0002/2022",
                        "title": "Project title 2",
                        "author": "Ing. Jenna Doe",
                        "uco": "00001",
                        "state": "proposed",
                        "dateBegin": "01.07.2023",
                        "dateEnd": "30.06.2028",
                        "muRole": "Partner",
                        "investor": "Investor 2",
                        "hs": "LF",
                        "department": "International Clinical Research Centre",
                        "annotation": "Some very lengthy annotation 2",
                        "annotationLanguage": "EN",
                        "annotationTransl": "",
                        "authorType": "academic, employee"
                    }
                ]
                """;
    }

    private Project resolveJsonProject(JSONObject proj) {
        var project = new Project();

        project.setProjId(String.valueOf(proj.get("Id")));
        project.setRegCode((String) proj.get("regCode"));
        project.setTitle((String) proj.get("title"));
        project.setState((String) proj.get("state"));
        project.setDateBegin(DateTime.parse((String) proj.get("dateBegin"), DATE_TIME_FORMATTER));
        project.setDateEnd(DateTime.parse((String) proj.get("dateEnd"), DATE_TIME_FORMATTER));
        project.setMuniRole((String) proj.get("muRole"));
        project.setInvestor((String) proj.get("investor"));
        project.setAnnotation((String) proj.get("annotation"));
        project.setProcessedAnnotation(textNormalizer.normalize((String) proj.get("annotation")));

        Author author = new Author();
        author.setName((String) proj.get("author"));
        author.setUco(String.valueOf(proj.get("uco")));
        author.setType((String) proj.get("authorType"));
        project.setAuthor(author);

        Department department = new Department();
        department.setOrgUnit((String) proj.get("hs"));
        department.setDepartmentName((String) proj.get("department"));
        project.setDepartment(department);

        return project;
    }
}
