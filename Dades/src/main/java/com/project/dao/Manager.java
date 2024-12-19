package com.project.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.project.domain.*;

public class Manager {
    private static SessionFactory factory;

    /**
     * Crea la SessionFactory per defecte
     */
    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            
            // Registrem totes les classes que tenen anotacions JPA
            configuration.addAnnotatedClass(Biblioteca.class);
            configuration.addAnnotatedClass(Llibre.class);
            configuration.addAnnotatedClass(Exemplar.class);
            configuration.addAnnotatedClass(Prestec.class);
            configuration.addAnnotatedClass(Persona.class);
            configuration.addAnnotatedClass(Autor.class);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("No s'ha pogut crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Crea la SessionFactory amb un fitxer de propietats específic
     */
    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            
            configuration.addAnnotatedClass(Biblioteca.class);
            configuration.addAnnotatedClass(Llibre.class);
            configuration.addAnnotatedClass(Exemplar.class);
            configuration.addAnnotatedClass(Prestec.class);
            configuration.addAnnotatedClass(Persona.class);
            configuration.addAnnotatedClass(Autor.class);

            Properties properties = new Properties();
            try (InputStream input = Manager.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
                if (input == null) {
                    throw new IOException("No s'ha trobat " + propertiesFileName);
                }
                properties.load(input);
            }

            configuration.addProperties(properties);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Error creant la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    /**
     * Tanca la SessionFactory
     */
    public static void close() {
        if (factory != null) {
            factory.close();
        }
    }
    private static <T> T saveEntity(T entity) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return entity;
        } catch (HibernateException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Autor addAutor(String nom) {
        Autor autor = new Autor();
        autor.setNom(nom);
        return saveEntity(autor);
    }

    public static void updateAutor(long autorId, String nom, Set<Llibre> llibres) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            Autor autor = session.get(Autor.class, autorId);
            if (autor != null) {
                autor.setNom(nom);
                autor.setLlibres(llibres);
                session.merge(autor);
            }
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    public static Llibre addLlibre(String isbn, String titol, String editorial, int anyPublicacio) {
        Llibre llibre = new Llibre();
        llibre.setIsbn(isbn);
        llibre.setTitol(titol);
        llibre.setEditorial(editorial);
        llibre.setAnyPublicacio(anyPublicacio);
        return saveEntity(llibre);
    }

    public static Biblioteca addBiblioteca(String nom, String ciutat, String adreca, String telefon, String email) {
        Biblioteca biblioteca = new Biblioteca();
        biblioteca.setNom(nom);
        biblioteca.setCiutat(ciutat);
        biblioteca.setAdreca(adreca);
        biblioteca.setTelefon(telefon);
        biblioteca.setEmail(email);
        return saveEntity(biblioteca);
    }

    public static Exemplar addExemplar(String codiBarres, Llibre llibre, Biblioteca biblioteca) {
        Exemplar exemplar = new Exemplar();
        exemplar.setCodiBarres(codiBarres);
        exemplar.setLlibre(llibre);
        exemplar.setBiblioteca(biblioteca);
        exemplar.setDisponible(true);
        return saveEntity(exemplar);
    }

    public static Persona addPersona(String dni, String nom, String telefon, String email) {
        Persona persona = new Persona();
        persona.setDni(dni);
        persona.setNom(nom);
        persona.setTelefon(telefon);
        persona.setEmail(email);
        return saveEntity(persona);
    }

    public static Prestec addPrestec(Exemplar exemplar, Persona persona, LocalDate dataPrestec, LocalDate dataRetornPrevista) {
        Prestec prestec = new Prestec();
        prestec.setExemplar(exemplar);
        prestec.setPersona(persona);
        prestec.setDataPrestec(dataPrestec);
        prestec.setDataRetornPrevista(dataRetornPrevista);
        prestec.setActiu(true);
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            exemplar.setDisponible(false);
            session.merge(exemplar);
            session.persist(prestec);
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return prestec;
    }

    public static void registrarRetornPrestec(long prestecId, LocalDate dataRetornReal) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            Prestec prestec = session.get(Prestec.class, prestecId);
            if (prestec != null) {
                prestec.setDataRetornReal(dataRetornReal);
                prestec.setActiu(false);
                Exemplar exemplar = prestec.getExemplar();
                exemplar.setDisponible(true);
                session.merge(exemplar);
                session.merge(prestec);
            }
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    public static List<Llibre> findLlibresAmbAutors() {
        try (Session session = factory.openSession()) {
            String hql = "SELECT DISTINCT l FROM Llibre l JOIN FETCH l.autors";
            return session.createQuery(hql, Llibre.class).list();
        }
    }

    public static List<Object[]> findLlibresEnPrestec() {
        try (Session session = factory.openSession()) {
            String hql = "SELECT p.exemplar.llibre.titol, p.persona.nom FROM Prestec p WHERE p.actiu = true";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public static List<Object[]> findLlibresAmbBiblioteques() {
        try (Session session = factory.openSession()) {
            String hql = "SELECT l.titol, e.biblioteca.nom FROM Exemplar e JOIN e.llibre l";
            return session.createQuery(hql, Object[].class).list();
        }
    }

    public static <T> Collection<T> listCollection(Class<T> clazz) {
        try (Session session = factory.openSession()) {
            String hql = "FROM " + clazz.getSimpleName();
            return session.createQuery(hql, clazz).list();
        }
    }
    
public static <T> String collectionToString(Class<T> clazz, Collection<T> collection) {
    StringBuilder sb = new StringBuilder();
    try (Session session = factory.openSession()) {
        for (T item : collection) {
            if (item instanceof Autor) {
                Hibernate.initialize(((Autor) item).getLlibres());
            } else if (item instanceof Llibre) {
                Hibernate.initialize(((Llibre) item).getAutors());
            }
            // Agrega más casos si hay otras colecciones perezosas
            sb.append(item.toString()).append("\n");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return sb.toString();
}

    public static String formatMultipleResult(List<Object[]> results) {
        StringBuilder sb = new StringBuilder();
        for (Object[] row : results) {
            sb.append("[");
            for (Object col : row) {
                sb.append(col).append(", ");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}
