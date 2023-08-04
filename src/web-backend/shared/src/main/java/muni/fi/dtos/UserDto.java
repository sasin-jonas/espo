package muni.fi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Schema(description = "User data transfer object")
public class UserDto extends BaseDto {

    @Schema(description = "JWT identifier of the user", example = "a1b2c3d4e5f6")
    private String jwtIdentifier;

    @Schema(description = "UCO (user identification number) of the user", example = "123456")
    private String uco;

    @Schema(description = "Name of the user", example = "Bc. John Doe")
    private String name;

    @Schema(description = "Email address of the user", example = "johndoe@example.com")
    private String email;

    @Schema(description = "Set of roles assigned to the user")
    private Set<RoleDto> roles = new HashSet<>();

}
