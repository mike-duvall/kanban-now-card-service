package kanbannow.jdbi;

import kanbannow.core.Card;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.sql.Date;
import java.util.List;

public interface CardDAO {


    @SqlQuery( "select id, text as \"cardText\", to_char( postponed_date, 'fmmm/dd/yyyy') as \"postponedDate\" from card where postponed_date is not null and board_id = :boardId  order by postponed_date")
    @Mapper(CardMapper.class)
    List<Card> getPostponedCardForBoard(@Bind("boardId") int boardId);


    @SqlUpdate( "delete from card where text = :text")
    void deleteCardWithText(@Bind("text") String text);

    @SqlUpdate("insert into card (board_id, id, text, location, postponed_date) values (:boardId, :cardId, :cardText, :cardLocation, :postponedDate)")
    void insertCardWithPostponedDate(
            @Bind("boardId") Long boardId,
            @Bind("cardId") Long cardId,
            @Bind("cardText") String cardText,
            @Bind("cardLocation") Long cardLocation,
            @Bind("postponedDate") Date postponedDate);


    // You'd think you could just pass null for postponed date, but I get an error
    @SqlUpdate("insert into card (board_id, id, text, location) values (:boardId, :cardId, :cardText, :cardLocation)")
    void insertCardWithoutPostponedDate(
            @Bind("boardId") Long boardId,
            @Bind("cardId") Long cardId,
            @Bind("cardText") String cardText,
            @Bind("cardLocation") Long cardLocation);


    @SqlQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
    Long getNextCardIdFromSequence();

@SqlUpdate("update card set postponed_date = TO_DATE(:postponedDate, 'MM/DD/YYYY') where id = :cardId")
    void setPostponedDate(@Bind("cardId") long cardId, @Bind("postponedDate") String postponedDate);
}
