package com.example.helloworld.db;

import com.example.helloworld.core.Cat;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class CatDAO extends AbstractDAO<Cat> {
    public CatDAO(SessionFactory factory) {
        super(factory);
    }

    public Optional<Cat> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public Cat create(Cat cat) {
        return persist(cat);
    }

    public List<Cat> findAll() {
        return list(namedQuery("com.example.helloworld.core.Cat.findAll"));
    }
}
