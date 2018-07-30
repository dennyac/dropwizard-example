package com.example.helloworld.core;

import com.example.helloworld.core.Cat;
import com.example.helloworld.core.Person;
import com.example.helloworld.db.CatDAO;
import com.example.helloworld.db.PersonDAO;
import com.github.javafaker.Faker;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.stream.Stream;

public class RandomDataGenerator {

    private final CatDAO catDAO;
    private final PersonDAO personDAO;
    private Faker faker;

    public RandomDataGenerator(CatDAO catDAO, PersonDAO personDAO, Faker faker) {
        this.catDAO = catDAO;
        this.personDAO = personDAO;
        this.faker = faker;
    }

    @UnitOfWork(value="hibernate-second")
    public void populateCatData() {
        Stream.generate(() -> new Cat(faker.cat().name(),faker.cat().breed())).limit(10).forEach(dog -> catDAO.create(dog));
    }


    @UnitOfWork
    public void populatePeopleData() {
        Stream.generate(() -> new Person(faker.name().fullName(), faker.job().title())).limit(10).forEach(person -> personDAO.create(person));
    }
}
