package com.biblioteca.libro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService service;

    public LibroController(LibroService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable @Positive Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@Valid @RequestBody LibroRequest request) {
        Libro libro = service.crear(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(libro.getId()).toUri();
        return ResponseEntity.created(location).body(libro);
    }

    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable @Positive Long id,
                            @Valid @RequestBody LibroRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}