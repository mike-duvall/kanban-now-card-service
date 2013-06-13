package kanbannow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import com.yammer.dropwizard.validation.Validator;
import kanbannow.core.Card;
import net.sf.json.test.JSONAssert;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;

import static org.fest.assertions.Assertions.assertThat;


public class CardServiceIntegrationTest {


    public static final String PROPERTIES_PATH = "../properties/";
    public static final String CARD_SERVICE_YML = "card-service.yml";
    public static final String FORWARD_SLASH = "/";
    public static final String SINGLE_QUOTE = "'";
    private Handle h;



    private DropwizardServiceRule<CardServiceConfiguration> serviceRule = new DropwizardServiceRule<CardServiceConfiguration>(CardService.class, PROPERTIES_PATH + CARD_SERVICE_YML);


    @Rule
    public DropwizardServiceRule<CardServiceConfiguration> getServiceRule() {
        return serviceRule;
    }


    @Before
    public void before() throws Exception {
        DBI dbi = initializeDB();
        cleanupDbData(dbi);
    }

    private DBI initializeDB() throws ClassNotFoundException {
        CardServiceConfiguration cardServiceConfiguration = serviceRule.getConfiguration();
        DatabaseConfiguration databaseConfiguration = cardServiceConfiguration.getDatabase();
        String databaseDriverClassName = databaseConfiguration.getDriverClass();
        Class.forName(databaseDriverClassName);
        String dataSourceUrl = databaseConfiguration.getUrl();
        String dataSourceUsername = databaseConfiguration.getUser();
        String dataSourcePassword = databaseConfiguration.getPassword();
        return new DBI(dataSourceUrl, dataSourceUsername, dataSourcePassword );
    }

    private void cleanupDbData(DBI dbi) {
        h = dbi.open();
        h.execute("delete from card");
        h.execute("delete from board");
        h.execute("delete from authorities");
        h.execute("delete from user_feature_toggle");
        h.execute("delete from users");
    }


    @Test
    public void shouldReturnOnlyPostponedCardsFromCorrectBoard() throws Exception {
        Long userId = createUser();

        String boardName1 = "Test board1";
        Long boardId1 = createBoard(userId, boardName1);

        String cardText1 = "Test card text1";
        String cardText2 = "Test card text2";

        Card card1 = new Card();
        card1.setCardText(cardText1);
        card1.setPostponedDate("2/2/2101");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime postponedDate1 = formatter.parseDateTime(card1.getPostponedDate());
        Long cardId1 = insertPostponedCardIntoBoard(boardId1, cardText1, new Date(postponedDate1.getMillis()));
        card1.setId(cardId1);


        Card card2 = new Card();
        card2.setCardText(cardText2);
        card2.setPostponedDate("1/1/2095");
        DateTime postponedDate2 = formatter.parseDateTime(card2.getPostponedDate());
        Long cardId2 = insertPostponedCardIntoBoard(boardId1, cardText2, new Date(postponedDate2.getMillis()));
        insertCardIntoBoard(boardId1, "non postponed card");
        card2.setId(cardId2);


        String boardName2 = "Test board2";
        Long boardId2 = createBoard(userId, boardName2);

        insertCardIntoBoard(boardId2, cardText1);
        insertCardIntoBoard(boardId2, cardText2);

        h.close();

        HttpResponse httpResponse = callCardService(boardId1);
        assertStatusCodeIs200(httpResponse);

        String result = getJsonFromHttpResponse(httpResponse);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResults = mapper.readTree( result );


        JsonNodeFactory factory = JsonNodeFactory.instance;
        ArrayNode expectedCardArrayJson = new ArrayNode(factory);

        ObjectNode row = createObjectNodeFromCard( card2, factory);
        expectedCardArrayJson.add(row);

        row = createObjectNodeFromCard(card1, factory );
        expectedCardArrayJson.add(row);

        JSONAssert.assertEquals(  expectedCardArrayJson,  jsonResults );
    }

    private String getJsonFromHttpResponse(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return bufferedReader.readLine();
    }

    private void assertStatusCodeIs200(HttpResponse httpResponse) {
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        assertThat(statusCode).isEqualTo(200);
    }

    private HttpResponse callCardService(Long boardId1) throws IOException, ConfigurationException {
        ConfigurationFactory<CardServiceConfiguration> configurationFactory = ConfigurationFactory.forClass(CardServiceConfiguration.class, new Validator());
        File configFile = new File(PROPERTIES_PATH + CARD_SERVICE_YML);
        CardServiceConfiguration configuration = configurationFactory.build(configFile);
        int port  = configuration.getHttpConfiguration().getPort();
        HttpClient httpclient = new DefaultHttpClient();
//            String uri = "http://localhost:9595/cards/board/" + boardId + "?postponed=true";
        String uri = "http://localhost:" + port + "/cards/board/" + boardId1;
        HttpGet httpget = new HttpGet(uri);
        return httpclient.execute(httpget);
    }

    private ObjectNode createObjectNode(String cardText, int year, int month, int day, Long cardId, JsonNodeFactory factory) {
        ObjectNode row = new ObjectNode(factory);
        row.put("id", cardId.intValue());
        row.put("cardText", cardText);
        String expectedPostponedDateString2 = "" + month + FORWARD_SLASH + day + FORWARD_SLASH + year;
        row.put("postponedDate", expectedPostponedDateString2);
        return row;
    }

    private ObjectNode createObjectNodeFromCard(Card card, JsonNodeFactory factory) {
        ObjectNode row = new ObjectNode(factory);
        Long cardIdLong = card.getId();
        row.put("id", cardIdLong.intValue() );
        row.put("cardText", card.getCardText() );
        row.put("postponedDate", card.getPostponedDate() );
        return row;
    }


    private Long createBoard(Long userId, String boardName1) {
        h.execute("insert into board ( name, user_id) values (?, ?)", boardName1, userId);
        return h.createQuery("select id from board where name = '" + boardName1 + SINGLE_QUOTE)
                .map(LongMapper.FIRST)
                .first();
    }

    private Long insertCardIntoBoard(Long boardId, String cardText) {
        long cardLocation = 1;
        Long cardId = getNextCardIdFromSequence();

        h.execute("insert into card (id, text, location, board_id) values (?, ?, ?, ?)", cardId, cardText, cardLocation, boardId);

        return cardId;
    }

    private Long getNextCardIdFromSequence() {
        return h.createQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
                    .map(LongMapper.FIRST)
                    .first();
    }

    private Long insertPostponedCardIntoBoard(Long boardId, String cardText, Date postponedDate) {
        long cardLocation = 1;
        Long cardId = getNextCardIdFromSequence();

        h.execute("insert into card (id, text, location, board_id, postponed_date) values (?, ?, ?, ?, ?)", cardId, cardText, cardLocation, boardId, postponedDate);

        return cardId;
    }


    private Long createUser() {
        String username = "ted";
        h.execute("insert into users (username, password) values ( ?, ?)", username, "password");
        return h.createQuery("select id from users where username = '" + username + SINGLE_QUOTE)
                .map(LongMapper.FIRST)
                .first();
    }


}
