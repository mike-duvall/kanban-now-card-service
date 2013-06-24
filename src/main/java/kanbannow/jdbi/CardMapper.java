package kanbannow.jdbi;


import kanbannow.core.Card;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardMapper implements ResultSetMapper<Card>
{
    public Card map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        Card newCard = new Card();
        newCard.setId(r.getLong("id"));
        newCard.setCardText(r.getString("cardText"));
        newCard.setPostponedDate(r.getString("postponedDate"));
        return newCard;
    }
}
