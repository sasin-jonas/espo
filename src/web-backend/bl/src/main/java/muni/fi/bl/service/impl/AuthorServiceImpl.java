package muni.fi.bl.service.impl;

import muni.fi.bl.mappers.AuthorMapper;
import muni.fi.bl.service.AuthorService;
import muni.fi.dal.repository.AuthorRepository;
import muni.fi.dtos.AuthorDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    @Override
    public List<AuthorDto> getAll() {
        return authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .toList();
    }
}
