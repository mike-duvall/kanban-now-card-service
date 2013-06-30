package kanbannow.health;

import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.metrics.core.HealthCheck;
import kanbannow.core.Card;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;

import java.sql.Date;

public class CardServiceHealthCheck extends HealthCheck {


    private DatabaseConfiguration databaseConfiguration;
    private Environment environment;
    private Handle databaseHandle;


    public static final String CARD_1_TEXT = "zzzTest card text1zzz";
    public static final String CARD_2_TEXT = "zzzTest card text2zzz";
    public static final String CARD_3_TEXT = "zzzTest card text3zzz";
    public static final String CARD_4_TEXT = "zzzTest card text4zzz";



    public CardServiceHealthCheck(Environment anEnvironment, DatabaseConfiguration aDatabaseConfiguration) {
        super("cardService");
        this.environment = anEnvironment;
        this.databaseConfiguration = aDatabaseConfiguration;
    }

    @Override
    protected Result check() throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI dbi = factory.build(environment, databaseConfiguration, "oracle");
        databaseHandle = dbi.open();
        cleanupDbData(dbi);
        Long boardId1 = 1L;
        Card card1 = createAndInsertPostponedCard(CARD_1_TEXT, "2/2/2101", boardId1);
        Card card2 = createAndInsertPostponedCard(CARD_2_TEXT, "1/1/2095", boardId1);
        return Result.healthy();
    }


    private Card createAndInsertPostponedCard(String text, String postponedDate, Long boardId) {
        Card card1 = new Card();
        card1.setCardText(text);
        card1.setPostponedDate(postponedDate);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime postponedDate1 = formatter.parseDateTime(card1.getPostponedDate());
        Long cardId1 = insertPostponedCardIntoBoard(boardId, card1, new Date(postponedDate1.getMillis()));
        card1.setId(cardId1);
        return card1;
    }


    private Long insertPostponedCardIntoBoard(Long boardId, Card aCard, Date postponedDate) {
        long cardLocation = 1;
        Long cardId = getNextCardIdFromSequence();

        databaseHandle.execute("insert into card (id, text, location, board_id, postponed_date) values (?, ?, ?, ?, ?)", cardId, aCard.getCardText(), cardLocation, boardId, postponedDate);

        return cardId;
    }


    private Long getNextCardIdFromSequence() {
        return databaseHandle.createQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
                .map(LongMapper.FIRST)
                .first();
    }


    // CHECKSTYLE:OFF
    private void cleanupDbData(DBI dbi) {

        databaseHandle.execute("delete from card where text ='" + CARD_1_TEXT + "'");
        databaseHandle.execute("delete from card where text ='" + CARD_2_TEXT + "'");
        databaseHandle.execute("delete from card where text ='" + CARD_3_TEXT + "'");
        databaseHandle.execute("delete from card where text ='" + CARD_4_TEXT + "'");
    }
    // CHECKSTYLE:ON


}
