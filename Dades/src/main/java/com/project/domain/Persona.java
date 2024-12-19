package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "persones")
public class Persona implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "persona_id")
    private long personaId;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 15)
    private String telefon;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prestec> prestecs = new HashSet<>();

    // Constructor por defecto
    public Persona() {}

    // Constructor con parámetros
    public Persona(String dni, String nom, String telefon, String email) {
        this.dni = dni;
        this.nom = nom;
        this.telefon = telefon;
        this.email = email;
    }

    // Getters y Setters
    public long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(long personaId) {
        this.personaId = personaId;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    public Set<Prestec> getPrestecs() {
        return prestecs;
    }

    public void setPrestecs(Set<Prestec> prestecs) {
        this.prestecs = prestecs;
    }

    // Métodos para relaciones
    public void addPrestec(Prestec prestec) {
        prestecs.add(prestec);
        prestec.setPersona(this);
    }

    public void removePrestec(Prestec prestec) {
        prestecs.remove(prestec);
        prestec.setPersona(null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Persona[id=%d, dni='%s', nom='%s'", personaId, dni, nom));

        if (telefon != null) {
            sb.append(String.format(", telefon='%s'", telefon));
        }

        if (email != null) {
            sb.append(String.format(", email='%s'", email));
        }

        sb.append(String.format(", prestecsActius=%d", getPrestecsActius()));
        sb.append("]");
        return sb.toString();
    }

    private long getPrestecsActius() {
        return prestecs.stream().filter(Prestec::isActiu).count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persona persona = (Persona) o;
        return personaId == persona.personaId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(personaId);
    }
}
