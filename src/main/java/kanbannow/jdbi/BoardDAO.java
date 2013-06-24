package kanbannow.jdbi;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface BoardDAO {

    @SqlUpdate("delete from board")
    void deleteAllBoards();

}
