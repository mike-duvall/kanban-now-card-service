package kanbannow.resources;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import kanbannow.core.Card;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.skife.jdbi.v2.BeanMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public List<Card> getCards(@PathParam("id") int boardId, @QueryParam("name") Optional<String> name) throws IOException, ClassNotFoundException {
        DBI dbi = initializeDbi();
        final String query =
                "select id, text as \"cardText\", to_char( postponed_date, 'fmmm/dd/yyyy') as \"postponedDate\" from card where postponed_date is not null and board_id = "
                        + boardId + " order by postponed_date";
//        final String query =
//                "select id, text as \"cardText\", to_char( postponed_date, 'fmmm/dd/yyyy') as \"postponedDate\" from card where postponed_date is not null and board_id = "
//                        + boardId;
        List<Card> cards = dbi.withHandle( createCallback(query) );
        return cards;
    }

    private HandleCallback<List<Card>> createCallback(final String query) {
        return new HandleCallback<List<Card>>() {
            public List<Card> withHandle(Handle h) {
                return h.createQuery(query)
                        .map(new BeanMapper<Card>(Card.class)).list();
            }
        };

    }

    private DBI initializeDbi() throws ClassNotFoundException {
        String databaseDriverClassName = databaseConfiguration.getDriverClass();
        Class.forName(databaseDriverClassName);
        String dataSourceUrl = databaseConfiguration.getUrl();
        String dataSourceUsername = databaseConfiguration.getUser();
        String dataSourcePassword = databaseConfiguration.getPassword();
        return new DBI(dataSourceUrl, dataSourceUsername, dataSourcePassword );
    }
}
