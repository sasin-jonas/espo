package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import muni.fi.enums.Role;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Schema(description = "Role data transfer object")
public class RoleDto {

    @Schema(description = "Name of the role", example = "ROLE_ADMIN")
    private Role name;

}
