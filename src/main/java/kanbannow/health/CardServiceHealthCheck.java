package kanbannow.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.metrics.core.HealthCheck;
import kanbannow.CardServiceConfiguration;
import kanbannow.core.Card;
import kanbannow.jdbi.CardDAO;
import net.sf.json.test.JSONAssert;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;

import static org.fest.assertions.Assertions.assertThat;


public class CardServiceHealthCheck extends HealthCheck {

    public static final String CARD_1_TEXT = "zzzTest card text1zzz";
    public static final String CARD_2_TEXT = "zzzTest card text2zzz";
    public static final String CARD_3_TEXT = "zzzTest card text3zzz";
    public static final String CARD_4_TEXT = "zzzTest card text4zzz";
    public static final String CARD_5_TEXT = "zzzTest card text5zzz";
    public static final String CARD_6_TEXT = "zzzTest card text6zzz";

    private CardServiceConfiguration cardServiceConfiguration;
    private CardDAO cardDao;


    public CardServiceHealthCheck(CardServiceConfiguration aCardServiceConfiguration,  CardDAO aCardDAO) {
        super("cardService");
        this.cardServiceConfiguration = aCardServiceConfiguration;
        this.cardDao = aCardDAO;
    }

    // CHECKSTYLE:OFF
    @Override
    protected Result check() throws Exception {

        // Given
        cleanupDbData();
        Long boardId1 = 1L;
        Card card1 = createAndInsertPostponedCard(boardId1, CARD_1_TEXT, "2/2/2101");
        Card card2 = createAndInsertPostponedCard(boardId1, CARD_2_TEXT, "1/1/2095");
        insertNonPostponedCardIntoBoard(boardId1, CARD_5_TEXT);
        Long boardId2 = 2L;
        insertNonPostponedCardIntoBoard(boardId2, CARD_3_TEXT);
        insertNonPostponedCardIntoBoard(boardId2, CARD_4_TEXT);
        createAndInsertPostponedCard(boardId2, CARD_6_TEXT, "1/1/2095");


        // When
        HttpResponse httpResponse = callCardService(boardId1);


        // Then
        assertStatusCodeIs200(httpResponse);
        JsonNode actualJsonResults = getJsonResults(httpResponse);
        ArrayNode expectedJsonResults = createdExpectedJson(card1, card2);
        JSONAssert.assertEquals(expectedJsonResults, actualJsonResults);

        // HACK
        cleanupDbData();
        return Result.healthy();
    }
    // CHECKSTYLE:ON

    private JsonNode getJsonResults(HttpResponse httpResponse) throws IOException {
        String result = getStringFromHttpResponse(httpResponse);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree( result );
    }

    private String getStringFromHttpResponse(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return bufferedReader.readLine();
    }


    private ArrayNode createdExpectedJson(Card card1, Card card2) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ArrayNode expectedCardArrayJson = new ArrayNode(factory);
        // Do card2 first, since it will be sorted as first item
        ObjectNode row = createObjectNodeFromCard(card2, factory);
        expectedCardArrayJson.add(row);
        row = createObjectNodeFromCard(card1, factory);
        expectedCardArrayJson.add(row);
        return expectedCardArrayJson;
    }

    private ObjectNode createObjectNodeFromCard(Card card, JsonNodeFactory factory) {
        ObjectNode row = new ObjectNode(factory);
        Long cardIdLong = card.getId();
        row.put("id", cardIdLong.intValue() );
        row.put("cardText", card.getCardText() );
        row.put("postponedDate", card.getPostponedDate() );
        return row;
    }



    private void assertStatusCodeIs200(HttpResponse httpResponse) {
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        assertThat(statusCode).isEqualTo(200);
    }



    //            String uri = "http://localhost:9595/cards/board/" + boardId + "?postponed=true";
    private HttpResponse callCardService(Long boardId1) throws IOException, ConfigurationException {
        int port  = cardServiceConfiguration.getHttpConfiguration().getPort();
        HttpClient httpclient = new DefaultHttpClient();
        String uri = "http://localhost:" + port + "/cards/board/" + boardId1;
        HttpGet httpget = new HttpGet(uri);
        return httpclient.execute(httpget);
    }


    private Long insertNonPostponedCardIntoBoard(Long boardId, String cardText) {
        long cardLocation = 1;
        Long cardId = getNextCardIdFromSequence();
        cardDao.insertCardWithoutPostponedDate(boardId, cardId, cardText, cardLocation);
        return cardId;
    }


    private Card createAndInsertPostponedCard(Long boardId, String text, String postponedDate) {
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
        cardDao.insertCardWithPostponedDate(boardId, cardId, aCard.getCardText(), cardLocation, postponedDate);
        return cardId;
    }


    private Long getNextCardIdFromSequence() {
        return cardDao.getNextCardIdFromSequence();
    }



    private void cleanupDbData() {
        cardDao.deleteCardWithText(CARD_1_TEXT);
        cardDao.deleteCardWithText(CARD_2_TEXT);
        cardDao.deleteCardWithText(CARD_3_TEXT);
        cardDao.deleteCardWithText(CARD_4_TEXT);
        cardDao.deleteCardWithText(CARD_5_TEXT);
        cardDao.deleteCardWithText(CARD_6_TEXT);
    }


}
