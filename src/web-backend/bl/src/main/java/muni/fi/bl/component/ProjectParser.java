package muni.fi.bl.component;

import muni.fi.bl.service.impl.ProjectLoadResult;

import java.io.InputStream;

public interface ProjectParser {

    /**
     * Tries to parse projects from inputStream. Enforces best-effort strategy, that skips malformed records
     *
     * @param stream Input stream with projects data
     * @return Result of the projects parsing. Contains information about the number of successfully parsed projects and other data
     */
    ProjectLoadResult parseProjects(InputStream stream);

    /**
     * Retrieves expected file format for projects loading
     *
     * @return An example content as string. The content type is based on the parser implementation
     */
    String getSample();

}
