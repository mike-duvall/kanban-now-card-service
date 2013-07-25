package kanbannow.resources;

import kanbannow.core.Card;
import com.yammer.metrics.annotation.Timed;
import kanbannow.jdbi.CardDAO;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;


@Path("/cards/")
@Produces(MediaType.APPLICATION_JSON)
public class CardResource {
    private CardDAO cardDAO;


    public CardResource(CardDAO aCardDAO) {
        this.cardDAO = aCardDAO;
    }

    @GET
    @Timed
    @Path("board/{id}")
    public List<Card> getCards(@PathParam("id") int boardId) throws IOException, ClassNotFoundException {
        List<Card> cardList = cardDAO.getPostponedCardForBoard(boardId);
        return cardList;
    }


    @POST
    @Timed
    @Path("{cardId}/postpone")
    public void postponeCard(String numDaysToPostponeString, @PathParam("cardId") long cardId ) {
        int numDaysToPostpone = Integer.parseInt(numDaysToPostponeString);
        DateTime currentDateTime = new DateTime();
        DateMidnight dateMidnight = currentDateTime.toDateMidnight();
        DateTime postponedDateTime = dateMidnight.toDateTime().plusDays(numDaysToPostpone).plusSeconds(1);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy");
        String formattedDate = postponedDateTime.toString( fmt );
        cardDAO.setPostponedDate( cardId, formattedDate);
    }


}
