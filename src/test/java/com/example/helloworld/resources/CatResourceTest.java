package com.example.helloworld.resources;

import com.example.helloworld.core.Cat;
import com.example.helloworld.db.CatDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CatResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CatResourceTest {
    private static final CatDAO CAT_DAO = mock(CatDAO.class);
    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new CatResource(CAT_DAO))
            .build();
    @Captor
    private ArgumentCaptor<Cat> catCaptor;
    private Cat cat;

    @Before
    public void setUp() {
        cat = new Cat();
        cat.setName("Felix");
        cat.setBreed("Abyssinian");
    }

    @After
    public void tearDown() {
        reset(CAT_DAO);
    }

    @Test
    public void createCat() throws JsonProcessingException {
        when(CAT_DAO.create(any(Cat.class))).thenReturn(cat);
        final Response response = RESOURCES.target("/cats")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(cat, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(CAT_DAO).create(catCaptor.capture());
        assertThat(catCaptor.getValue()).isEqualTo(cat);
    }

    @Test
    public void listCats() throws Exception {
        final ImmutableList<Cat> cats = ImmutableList.of(cat);
        when(CAT_DAO.findAll()).thenReturn(cats);

        final List<Cat> response = RESOURCES.target("/cats")
                .request().get(new GenericType<List<Cat>>() {
                });

        verify(CAT_DAO).findAll();
        assertThat(response).containsAll(cats);
    }
}
