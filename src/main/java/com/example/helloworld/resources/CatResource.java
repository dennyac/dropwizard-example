package com.example.helloworld.resources;

import com.example.helloworld.core.Cat;
import com.example.helloworld.db.CatDAO;
import io.dropwizard.hibernate.UnitOfWork;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/cats")
@Produces(MediaType.APPLICATION_JSON)
public class CatResource {

    private final CatDAO catDAO;

    public CatResource(CatDAO catDAO) {
        this.catDAO = catDAO;
    }

    @POST
    @UnitOfWork(value="hibernate-second")
    public Cat createPerson(Cat cat) {
        return catDAO.create(cat);
    }

    @GET
    @UnitOfWork(value="hibernate-second")
    public List<Cat> listCats() {
        return catDAO.findAll();
    }

}
