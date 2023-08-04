package muni.fi.dal.specification;

import muni.fi.dal.entity.Project;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecifications {

    public static Specification<Project> hasTitleContaining(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Project> hasRegCodeContaining(String regCode) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("regCode")), "%" + regCode.toLowerCase() + "%");
    }

    public static Specification<Project> hasMuniRoleContaining(String muniRole) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("muniRole")), "%" + muniRole.toLowerCase() + "%");
    }

    public static Specification<Project> hasAuthorUcoContaining(String authorUco) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.join("author").get("uco")), "%" + authorUco.toLowerCase() + "%");
    }

    public static Specification<Project> hasDepartmentOrgUnitContaining(String orgUnit) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.join("department").get("orgUnit")), "%" + orgUnit.toLowerCase() + "%");
    }

    public static Specification<Project> hasDepartmentNameContaining(String departmentName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.join("department").get("departmentName")), "%" + departmentName.toLowerCase() + "%");
    }
}
