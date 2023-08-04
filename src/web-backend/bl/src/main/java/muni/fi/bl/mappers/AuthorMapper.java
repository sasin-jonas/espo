package muni.fi.bl.mappers;

import muni.fi.dal.entity.Author;
import muni.fi.dtos.AuthorDto;
import org.mapstruct.Mapper;

@Mapper
public interface AuthorMapper {
    AuthorDto toDto(Author source);

    Author toEntity(AuthorDto destination);
}
