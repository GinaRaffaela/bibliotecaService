package com.biblioteca.libro;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LibroService {

    private final LibroRepository repository;

    public LibroService(LibroRepository repository) {
        this.repository = repository;
    }

    public Libro obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    @Transactional
    public Libro crear(LibroRequest request) {
        return repository.save(new Libro(request));
    }

    @Transactional
    public Libro actualizar(Long id, LibroRequest request) {
        Libro libro = obtener(id);
        libro.actualizar(request);
        return libro;
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}