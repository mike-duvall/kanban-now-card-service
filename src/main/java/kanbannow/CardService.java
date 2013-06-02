package kanbannow;

import kanbannow.health.TemplateHealthCheck;
import kanbannow.resources.CardResource;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class CardService extends Service<CardServiceConfiguration> {
    public static void main(String[] args) throws Exception {
        new CardService().run(args);
    }


    @Override
    public void initialize(Bootstrap<CardServiceConfiguration> bootstrap) {
        bootstrap.setName("hello-world");
    }

    @Override
    public void run(CardServiceConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new CardResource( configuration.getDatabase()));
        environment.addHealthCheck(new TemplateHealthCheck("Make this a real healthCheck"));

    }


}
