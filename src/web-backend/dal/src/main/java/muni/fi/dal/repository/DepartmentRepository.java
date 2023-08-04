package muni.fi.dal.repository;

import muni.fi.dal.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByOrgUnitAndDepartmentName(String orgUnit, String departmentName);

}
