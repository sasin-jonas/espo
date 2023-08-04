package muni.fi.bl.mappers;

import muni.fi.dal.entity.User;
import muni.fi.dtos.UserDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    UserDto toDto(User source);

    List<UserDto> toDtos(List<User> source);

    User toEntity(UserDto destination);
}
