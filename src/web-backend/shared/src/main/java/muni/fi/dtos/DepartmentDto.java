package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class DepartmentDto extends BaseDto {

    @Schema(description = "Organizational unit of the department", example = "FI")
    private String orgUnit;

    @Schema(description = "Name of the department", example = "Department of Computer Systems and Communications")
    private String departmentName;
}
