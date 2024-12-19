package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "biblioteques")
public class Biblioteca implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "biblioteca_id")
    private long bibliotecaId;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String ciutat;

    @Column(nullable = false, length = 150)
    private String adreca;

    @Column(length = 15)
    private String telefon;

    @Column(length = 100)
    private String email;

    @OneToMany(mappedBy = "biblioteca", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Exemplar> exemplars = new HashSet<>();

    // Constructor por defecto
    public Biblioteca() {}

    // Constructor con parámetros
    public Biblioteca(String nom, String ciutat, String adreca, String telefon, String email) {
        this.nom = nom;
        this.ciutat = ciutat;
        this.adreca = adreca;
        this.telefon = telefon;
        this.email = email;
    }

    // Getters y Setters
    public long getBibliotecaId() {
        return bibliotecaId;
    }

    public void setBibliotecaId(long bibliotecaId) {
        this.bibliotecaId = bibliotecaId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCiutat() {
        return ciutat;
    }

    public void setCiutat(String ciutat) {
        this.ciutat = ciutat;
    }

    public String getAdreca() {
        return adreca;
    }

    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Exemplar> getExemplars() {
        return exemplars;
    }

    public void setExemplars(Set<Exemplar> exemplars) {
        this.exemplars = exemplars;
    }

    // Métodos para relaciones
    public void addExemplar(Exemplar exemplar) {
        exemplars.add(exemplar);
        exemplar.setBiblioteca(this);
    }

    public void removeExemplar(Exemplar exemplar) {
        exemplars.remove(exemplar);
        exemplar.setBiblioteca(null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Biblioteca[id=%d, nom='%s', ciutat='%s'", bibliotecaId, nom, ciutat));

        if (adreca != null) {
            sb.append(String.format(", adreca='%s'", adreca));
        }
        if (telefon != null) {
            sb.append(String.format(", tel='%s'", telefon));
        }
        if (email != null) {
            sb.append(String.format(", email='%s'", email));
        }

        if (!exemplars.isEmpty()) {
            sb.append(", exemplars={");
            boolean first = true;
            for (Exemplar e : exemplars) {
                if (!first) sb.append(", ");
                sb.append(e.getCodiBarres());
                first = false;
            }
            sb.append("}");
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biblioteca that = (Biblioteca) o;
        return bibliotecaId == that.bibliotecaId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(bibliotecaId);
    }
}
