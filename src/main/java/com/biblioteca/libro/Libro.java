package com.biblioteca.libro;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "libros_seq")
    @SequenceGenerator(name = "libros_seq", sequenceName = "libros_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(name = "anio_publicacion", nullable = false)
    private Integer anioPublicacion;

    @Column(nullable = false, length = 100)
    private String genero;

    protected Libro() {
    }

    public Libro(LibroRequest request) {
        actualizar(request);
    }

    public void actualizar(LibroRequest request) {
        titulo = request.titulo();
        autor = request.autor();
        anioPublicacion = request.anioPublicacion();
        genero = request.genero();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public String getGenero() {
        return genero;
    }
}