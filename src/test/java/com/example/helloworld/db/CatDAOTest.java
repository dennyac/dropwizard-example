package com.example.helloworld.db;

import com.example.helloworld.core.Cat;
import io.dropwizard.testing.junit.DAOTestRule;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CatDAOTest {

    @Rule
    public DAOTestRule daoTestRule = DAOTestRule.newBuilder()
            .addEntityClass(Cat.class)
            .build();

    private CatDAO catDAO;

    @Before
    public void setUp() throws Exception {
        catDAO = new CatDAO(daoTestRule.getSessionFactory());
    }

    @Test
    public void createCat() {
        final Cat bella = daoTestRule.inTransaction(() -> catDAO.create(new Cat("Bella", "Aegean")));
        assertThat(bella.getId()).isGreaterThan(0);
        assertThat(bella.getName()).isEqualTo("Bella");
        assertThat(bella.getBreed()).isEqualTo("Aegean");
        assertThat(catDAO.findById(bella.getId())).isEqualTo(Optional.of(bella));
    }

    @Test
    public void findAll() {
        daoTestRule.inTransaction(() -> {
            catDAO.create(new Cat("Coco", "Foldex Cat"));
            catDAO.create(new Cat("Daisy", "German Rex"));
            catDAO.create(new Cat("Felix", "Havana Brown"));
        });

        final List<Cat> cats = catDAO.findAll();
        assertThat(cats).extracting("name").containsOnly("Coco", "Daisy", "Felix");
        assertThat(cats).extracting("breed").containsOnly("Foldex Cat", "German Rex", "Havana Brown");
    }

    @Test(expected = ConstraintViolationException.class)
    public void handlesNullFullName() {
        daoTestRule.inTransaction(() -> catDAO.create(new Cat(null, "The null")));
    }
}
