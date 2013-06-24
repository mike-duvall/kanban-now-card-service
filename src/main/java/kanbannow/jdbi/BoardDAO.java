package kanbannow.jdbi;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;


public interface BoardDAO {


    @SqlUpdate("insert into board ( name, user_id) values (:aBoardName, :aUserId)" )
    void createBoard( @Bind("aBoardName") String aBoardName, @Bind("aUserId") Long aUserId);


    @SqlUpdate("delete from board")
    void deleteAllBoards();

    @SqlQuery("select id from board where name = :boardName")
    Long findBoardWithName(@Bind("boardName") String boardName);
}
