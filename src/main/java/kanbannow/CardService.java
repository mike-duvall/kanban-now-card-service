package kanbannow;

import com.yammer.metrics.reporting.GraphiteReporter;
import kanbannow.health.CardServiceHealthCheck;
import kanbannow.resources.CardResource;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import java.util.concurrent.TimeUnit;

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
        environment.addHealthCheck(new CardServiceHealthCheck(configuration.getDatabase()));

        GraphiteReporter.enable(15, TimeUnit.SECONDS, "carbon.hostedgraphite.com", 2003, "0cb986a9-f3e9-4292-8d08-0d3a759e448f");

    }


}
