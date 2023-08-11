package muni.fi.bl;

import muni.fi.dal.entity.Project;

import java.util.List;

public record ProjectLoadResult(int total, int successful, int failed, List<Project> projects) {
}
