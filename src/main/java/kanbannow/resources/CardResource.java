package kanbannow.resources;

import kanbannow.core.Saying;
import com.google.common.base.Optional;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    public Saying sayHello(@PathParam("id") int boardId, @QueryParam("name") Optional<String> name) {
        return new Saying(counter.incrementAndGet(),
                          String.format(template, name.or(defaultName)));
    }
}
