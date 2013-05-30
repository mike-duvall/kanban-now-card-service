package kanbannow.resources;

import kanbannow.core.Card;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;
import org.skife.jdbi.v2.BeanMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

@Path("/cards/board")
@Produces(MediaType.APPLICATION_JSON)
public class CardResource {
    private final String template;
    private final String defaultName;
    private final AtomicLong counter;

    public CardResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    @Path("{id}")
    public Card sayHello(@PathParam("id") int boardId, @QueryParam("name") Optional<String> name) throws IOException, ClassNotFoundException {
//        return new Card(counter.incrementAndGet(),
//                          String.format(template, name.or(defaultName)));
        Properties props = new Properties();

        File dbPropertiesFile = new File("../properties/database.properties");
        FileInputStream fileInputStream = new FileInputStream(dbPropertiesFile);
        props.load(fileInputStream);

        String databaseDriverClassName = (String) props.get("dataSource.driverClassName");

        Class.forName(databaseDriverClassName);

        String dataSourceUrl = (String) props.get("dataSource.url" );
        String dataSourceUsername = (String) props.get("dataSource.username");
        String dataSourcePassword = (String) props.get("dataSource.password");

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
