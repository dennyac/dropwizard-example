package com.example.helloworld;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.*;
import com.example.helloworld.db.CatDAO;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.filter.DateRequiredFeature;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.*;
import com.example.helloworld.service.RandomDataGeneratorScheduledService;
import com.example.helloworld.tasks.EchoTask;
import com.github.javafaker.Faker;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.Map;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
        new HibernateBundle<HelloWorldConfiguration>(Person.class) {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        };

    private final HibernateBundle<HelloWorldConfiguration> secondHibernateBundle =
            new HibernateBundle<HelloWorldConfiguration>(Cat.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                    return configuration.getSecondDataSourceFactory();
                }

                @Override
                public String name() {
                    return "hibernate-second";
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getSecondDataSourceFactory();
            }

            @Override
            public String name() {
                return "second_db";
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(secondHibernateBundle);
        bootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(HelloWorldConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {
        final PersonDAO personDAO = new PersonDAO(hibernateBundle.getSessionFactory());
        final CatDAO catDAO = new CatDAO(secondHibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.admin().addTask(new EchoTask());
        environment.jersey().register(DateRequiredFeature.class);
        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new ExampleAuthenticator())
                .setAuthorizer(new ExampleAuthorizer())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new HelloWorldResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(personDAO));
        environment.jersey().register(new PersonResource(personDAO));
        environment.jersey().register(new CatResource(catDAO));
        environment.jersey().register(new FilteredResource());
        RandomDataGenerator randomDataGenerator = new UnitOfWorkAwareProxyFactory(hibernateBundle, secondHibernateBundle)
                .create(RandomDataGenerator.class, new Class[] {CatDAO.class, PersonDAO.class, Faker.class}, new Object[] {catDAO, personDAO, new Faker()});
        RandomDataGeneratorScheduledService randomDataGeneratorScheduledService = new RandomDataGeneratorScheduledService(randomDataGenerator);
        environment.lifecycle().manage(randomDataGeneratorScheduledService);
    }
}
