package kanbannow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import com.yammer.dropwizard.validation.Validator;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;
import java.io.*;
import java.sql.Date;

import static org.fest.assertions.Assertions.assertThat;


public class CardServiceIntegrationTest {


    public static final String PROPERTIES_PATH = "../properties/";
    private Handle h;

    @Rule
    public DropwizardServiceRule<CardServiceConfiguration> serviceRule = new DropwizardServiceRule<CardServiceConfiguration>(CardService.class, PROPERTIES_PATH + "card-service.yml" );



    @Before
    public void before() throws Exception {
        CardServiceConfiguration cardServiceConfiguration = serviceRule.getConfiguration();
        DatabaseConfiguration databaseConfiguration = cardServiceConfiguration.getDatabase();

        String databaseDriverClassName = databaseConfiguration.getDriverClass();

        Class.forName(databaseDriverClassName);

        String dataSourceUrl = databaseConfiguration.getUrl();
        String dataSourceUsername = databaseConfiguration.getUser();
        String dataSourcePassword = databaseConfiguration.getPassword();


        DBI dbi = new DBI(dataSourceUrl, dataSourceUsername, dataSourcePassword );

        h = dbi.open();

        h.execute("delete from card");
        h.execute("delete from board");
        h.execute("delete from authorities");
        h.execute("delete from user_feature_toggle");
        h.execute("delete from users");
    }



    @Test
    public void shouldReturnOnlyPostponedCardsFromCorrectBoard() throws Exception {

        ConfigurationFactory<CardServiceConfiguration> configurationFactory = ConfigurationFactory.forClass(CardServiceConfiguration.class, new Validator());
        File configFile = new File(PROPERTIES_PATH + "card-service.yml");
        CardServiceConfiguration configuration = configurationFactory.build(configFile);
        int port  = configuration.getHttpConfiguration().getPort();

        Long userId = createUser();

        String boardName1 = "Test board1";
        h.execute("insert into board ( name, user_id) values (?, ?)", boardName1, userId);
        Long boardId1 = h.createQuery("select id from board where name = '" + boardName1 + "'")
                .map(LongMapper.FIRST)
                .first();

        String cardText1 = "Test card text1";
        String cardText2 = "Test card text2";

        int year = 2101;
        int month = 1;
        int day = 1;

        DateTime postponedDate = new DateTime(year,month,day,0,0,0);
        Long cardId1 = insertPostponedCardIntoBoard(boardId1, cardText1, new Date(postponedDate.getMillis()));
        Long cardId2 = insertPostponedCardIntoBoard(boardId1, cardText2, new Date(postponedDate.getMillis()));
        insertCardIntoBoard(boardId1, "non postponed card");


        String boardName2 = "Test board2";
        h.execute("insert into board ( name, user_id) values (?, ?)", boardName2, userId);
        Long boardId2 = h.createQuery("select id from board where name = '" + boardName2 + "'")
                .map(LongMapper.FIRST)
                .first();

        insertCardIntoBoard(boardId2, cardText1);
        insertCardIntoBoard(boardId2, cardText2);

        h.close();


        HttpClient httpclient = new DefaultHttpClient();

//            String uri = "http://localhost:9595/cards/board/" + boardId + "?postponed=true";

        String uri = "http://localhost:" + port + "/cards/board/" + boardId1;
        HttpGet httpget = new HttpGet(uri);

        System.out.println("executing request " + httpget.getURI());

        HttpResponse httpResponse = httpclient.execute(httpget);
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();



        assertThat(statusCode).isEqualTo(200);

        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String result = bufferedReader.readLine();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResults = mapper.readTree( result );

        JsonNodeFactory factory = JsonNodeFactory.instance;

        ArrayNode expectedCardArrayJson = new ArrayNode(factory);

        ObjectNode row = new ObjectNode(factory);
        row.put("id", cardId1.intValue());
        row.put("cardText", cardText1);
        String expectedPostponedDateString = "" + month + "/" + day + "/" + year;
        row.put("postponedDate", expectedPostponedDateString);
        expectedCardArrayJson.add(row);

        row = new ObjectNode(factory);
        row.put("id", cardId2.intValue());
        row.put("cardText", cardText2);
        row.put("postponedDate", expectedPostponedDateString);
        expectedCardArrayJson.add(row);

        assertThat(jsonResults.equals(expectedCardArrayJson)).isTrue();

    }

    private Long insertCardIntoBoard(Long boardId, String cardText) {
        long cardLocation = 1;
        Long cardId = h.createQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
                .map(LongMapper.FIRST)
                .first();

        h.execute("insert into card (id, text, location, board_id) values (?, ?, ?, ?)", cardId, cardText, cardLocation, boardId);

        return cardId;
    }

    private Long insertPostponedCardIntoBoard(Long boardId, String cardText, Date postponedDate) {
        long cardLocation = 1;
        Long cardId = h.createQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
                .map(LongMapper.FIRST)
                .first();

        h.execute("insert into card (id, text, location, board_id, postponed_date) values (?, ?, ?, ?, ?)", cardId, cardText, cardLocation, boardId, postponedDate);

        return cardId;
    }


    private Long createUser() {
        String username = "ted";
        h.execute("insert into users (username, password) values ( ?, ?)", username, "password");
        Long userId = h.createQuery("select id from users where username = '" + username + "'" )
                .map(LongMapper.FIRST)
                .first();

        return userId;

    }


}
