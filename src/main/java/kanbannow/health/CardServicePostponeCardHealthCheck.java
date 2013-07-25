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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.fest.assertions.Assertions.assertThat;


public class CardServicePostponeCardHealthCheck extends HealthCheck {

    public static final String CARD_1_TEXT = "zzzTest card text1zzz";

    private CardServiceConfiguration cardServiceConfiguration;
    private CardDAO cardDao;


    public CardServicePostponeCardHealthCheck(CardServiceConfiguration aCardServiceConfiguration, CardDAO aCardDAO) {
        super("cardService-postponeCard");
        this.cardServiceConfiguration = aCardServiceConfiguration;
        this.cardDao = aCardDAO;
    }

    // CHECKSTYLE:OFF
    @Override
    protected Result check() throws Exception {

        cleanupDbData();


        // Given
        Long boardId1 = 1L;
        Card card3 = createAndInsertNonPostponedCardIntoBoard( boardId1, CARD_1_TEXT);

//        really need to clean all this up:
//        * Remove duplication
//        * handle cleanup of data after tests, so health checks don't conflict
//        * Fix having to manually set card3 postponed date below

         // When
        Integer numDaysToPostpone = 7;
        HttpResponse httpResponse = callCardServiceToPostponeCard( card3.getId(), numDaysToPostpone);
        assertStatusCodeIs204(httpResponse);
        Card returnedCard = parseCardFromResponse(httpResponse);


        // Then
        httpResponse = callCardServiceToGetPostponedCardsForBoard(boardId1);

        assertStatusCodeIs200(httpResponse);
        JsonNode actualJsonResults = getJsonResults(httpResponse);
        DateTime currentDateTime = new DateTime();
        DateMidnight dateMidnight = currentDateTime.toDateMidnight();
        DateTime postponedDateTime = dateMidnight.toDateTime().plusDays(numDaysToPostpone).plusSeconds(1);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("M/d/yyyy");
        String formattedDate = postponedDateTime.toString(fmt);


        Card expectedCard = new Card();
        expectedCard.setPostponedDate(formattedDate);
        expectedCard.setId(card3.getId());
        expectedCard.setCardText(CARD_1_TEXT);

        ArrayNode expectedJsonResults = createdExpectedJson(expectedCard);
        JSONAssert.assertEquals(expectedJsonResults, actualJsonResults);

        cleanupDbData();


        return Result.healthy();
    }
    // CHECKSTYLE:ON


    private Card parseCardFromResponse(HttpResponse anHttpResponse) {
        return null;

    }

    private HttpResponse callCardServiceToGetPostponedCardsForBoard(Long boardId1) throws IOException, ConfigurationException {
        int port  = cardServiceConfiguration.getHttpConfiguration().getPort();
        HttpClient httpclient = new DefaultHttpClient();
        String uri = "http://localhost:" + port + "/cards/board/" + boardId1;
        HttpGet httpget = new HttpGet(uri);
        return httpclient.execute(httpget);
    }

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


    private ArrayNode createdExpectedJson(Card card) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ArrayNode expectedCardArrayJson = new ArrayNode(factory);
        ObjectNode row = createObjectNodeFromCard(card, factory);
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

    private void assertStatusCodeIs204(HttpResponse httpResponse) {
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        assertThat(statusCode).isEqualTo(204);
    }

    private HttpResponse callCardServiceToPostponeCard(long cardId, Integer numDaysToPostpone) throws IOException, ConfigurationException {
        int port  = cardServiceConfiguration.getHttpConfiguration().getPort();
        HttpClient httpclient = new DefaultHttpClient();
        String uri = "http://localhost:" + port + "/cards/" + cardId + "/postpone";
        HttpPost httpPost = new HttpPost(uri);
        StringEntity stringEntity = new StringEntity(numDaysToPostpone.toString());
        httpPost.setEntity(stringEntity);
        return httpclient.execute(httpPost);
    }

    private Card createAndInsertNonPostponedCardIntoBoard(Long boardId, String cardText) {
        Card card1 = new Card();
        card1.setCardText(cardText);
        Long cardId1 = insertNonPostponedCardIntoBoard(boardId, cardText);
        card1.setId(cardId1);
        return card1;

    }


    private Long insertNonPostponedCardIntoBoard(Long boardId, String cardText) {
        long cardLocation = 1;
        Long cardId = getNextCardIdFromSequence();
        cardDao.insertCardWithoutPostponedDate(boardId, cardId, cardText, cardLocation);
        return cardId;
    }


    private Long getNextCardIdFromSequence() {
        return cardDao.getNextCardIdFromSequence();
    }


    private void cleanupDbData() {
        cardDao.deleteCardWithText(CARD_1_TEXT);
    }


}
