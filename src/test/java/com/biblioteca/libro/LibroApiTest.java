package com.biblioteca.libro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibroApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private LibroRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void cicloCrudPersisteLosCambios() throws Exception {
        var request = new LibroRequest("Don Quijote", "Miguel de Cervantes", 1605, "Novela");
        var result = mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Don Quijote"))
                .andReturn();
        long id = mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo("http://localhost/api/libros/" + id);

        mvc.perform(get("/api/libros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("Miguel de Cervantes"))
                .andExpect(jsonPath("$.anioPublicacion").value(1605))
                .andExpect(jsonPath("$.genero").value("Novela"));

        var updated = new LibroRequest("El principito", "Antoine de Saint-Exupery", 1943, "Fabula");
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value(updated.titulo()));
        mvc.perform(get("/api/libros/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value(updated.titulo()))
                .andExpect(jsonPath("$.autor").value(updated.autor()))
                .andExpect(jsonPath("$.anioPublicacion").value(updated.anioPublicacion()))
                .andExpect(jsonPath("$.genero").value(updated.genero()));

        mvc.perform(delete("/api/libros/{id}", id)).andExpect(status().isNoContent());
        assertThat(repository.existsById(id)).isFalse();
        mvc.perform(get("/api/libros/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void libroInexistenteDevuelve404SinCrearRegistros() throws Exception {
        var request = new LibroRequest("Titulo", "Autor", 2000, "Novela");
        mvc.perform(get("/api/libros/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Libro no encontrado"));
        mvc.perform(put("/api/libros/999999").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/libros/999999")).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void rechazaDatosInvalidosEnCreacionYActualizacion() throws Exception {
        var invalid = new LibroRequest(" ", "", 0, "");
        String json = mapper.writeValueAsString(invalid);
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.length()").value(4));
        Libro existing = repository.save(new Libro(new LibroRequest("Titulo", "Autor", 2000, "Novela")));
        mvc.perform(put("/api/libros/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        assertThat(repository.findById(existing.getId()).orElseThrow().getTitulo()).isEqualTo("Titulo");
    }

    @Test
    void rechazaCamposAusentesLimitesYJsonInvalido() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        var invalid = new LibroRequest("T".repeat(201), "A".repeat(151), 10000, "G".repeat(101));
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.length()").value(4));
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        assertThat(repository.count()).isZero();
    }

    @Test
    void rechazaIdentificadoresInvalidos() throws Exception {
        mvc.perform(get("/api/libros/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/libros/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/libros/abc")).andExpect(status().isBadRequest());
    }
}