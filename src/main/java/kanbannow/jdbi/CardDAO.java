package kanbannow.jdbi;

import kanbannow.core.Card;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface CardDAO {


    @SqlQuery( "select id, text as \"cardText\", to_char( postponed_date, 'fmmm/dd/yyyy') as \"postponedDate\" from card where postponed_date is not null and board_id = :boardId  order by postponed_date")
    @Mapper(CardMapper.class)
    List<Card> getPostponedCardForBoard(@Bind("boardId") int boardId);


    @SqlUpdate( "delete from card where text = :text")
    void deleteCardWithText(@Bind("text") String text);
}
