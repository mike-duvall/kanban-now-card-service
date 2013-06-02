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
        bootstrap.addCommand(embeddedServerCommand);


    }

    @Override
    public void run(CardServiceConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new CardResource( configuration.getDatabase()));
        environment.addHealthCheck(new TemplateHealthCheck("Make this a real healthCheck"));

    }


    private final EmbeddedServerCommand<CardServiceConfiguration> embeddedServerCommand =
            new EmbeddedServerCommand<CardServiceConfiguration>(this);

    public void startEmbeddedServer(String configFileName) throws Exception {
        run(new String[] {"embedded-server", configFileName});
    }

    public void stopEmbeddedServer() throws Exception {
        embeddedServerCommand.stop();
    }

    public boolean isEmbeddedServerRunning() {
        return embeddedServerCommand.isRunning();
    }




}
