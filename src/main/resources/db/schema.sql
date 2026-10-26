CREATE SEQUENCE libros_seq START WITH 1 INCREMENT BY 1 NOCYCLE;

CREATE TABLE libros (
    id NUMBER(19) NOT NULL,
    titulo VARCHAR2(200 CHAR) NOT NULL,
    autor VARCHAR2(150 CHAR) NOT NULL,
    anio_publicacion NUMBER(10) NOT NULL,
    genero VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_libros PRIMARY KEY (id),
    CONSTRAINT ck_libros_anio CHECK (anio_publicacion BETWEEN 1 AND 9999)
);