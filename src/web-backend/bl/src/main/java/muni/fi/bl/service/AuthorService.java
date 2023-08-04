package muni.fi.bl.service;

import muni.fi.dtos.AuthorDto;

import java.util.List;

public interface AuthorService {

    /**
     * Retrieves all available authors
     *
     * @return List of authors
     */
    List<AuthorDto> getAll();
}
