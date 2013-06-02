package kanbannow.resources;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import kanbannow.core.Card;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.skife.jdbi.v2.BeanMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/cards/board")
@Produces(MediaType.APPLICATION_JSON)
public class CardResource {
    private final DatabaseConfiguration databaseConfiguration;

    public CardResource( DatabaseConfiguration aDatabaseConfiguration) {
        this.databaseConfiguration = aDatabaseConfiguration;
    }

    @GET
    @Timed
    @Path("{id}")
    public Card sayHello(@PathParam("id") int boardId, @QueryParam("name") Optional<String> name) throws IOException, ClassNotFoundException {

        String databaseDriverClassName = databaseConfiguration.getDriverClass();

        Class.forName(databaseDriverClassName);

        String dataSourceUrl = databaseConfiguration.getUrl();
        String dataSourceUsername = databaseConfiguration.getUser();
        String dataSourcePassword = databaseConfiguration.getPassword();


        DBI dbi = new DBI(dataSourceUrl, dataSourceUsername, dataSourcePassword );

        Handle h = dbi.open();

//        id, text, location, board_id

        List<Card> cards = dbi.withHandle(new HandleCallback<List<Card>>() {
            public List<Card> withHandle(Handle h) {
                return h.createQuery("select id from card")
                        .map(new BeanMapper<Card>(Card.class)).list();
            }
        });

        return cards.get(0);
    }
}
