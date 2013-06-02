package kanbannow;

import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import com.yammer.dropwizard.validation.Validator;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;


import java.io.*;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;


public class CardServiceIntegrationTest {


    public static final String PROPERTIES_PATH = "../properties/";

    @Rule
    public DropwizardServiceRule<CardServiceConfiguration> serviceRule = new DropwizardServiceRule<CardServiceConfiguration>(CardService.class, PROPERTIES_PATH + "card-service.yml" );

    @Test
    public void test() throws Exception {

        CardService service = new CardService();

        ConfigurationFactory<CardServiceConfiguration> configurationFactory = ConfigurationFactory.forClass(CardServiceConfiguration.class, new Validator());
        File configFile = new File(PROPERTIES_PATH + "card-service.yml");
        CardServiceConfiguration configuration = configurationFactory.build(configFile);

        int port  = configuration.getHttpConfiguration().getPort();

        Properties props = new Properties();

        File dbPropertiesFile = new File(PROPERTIES_PATH + "database.properties");
        FileInputStream fileInputStream = new FileInputStream(dbPropertiesFile);
        props.load(fileInputStream);

        String databaseDriverClassName = (String) props.get("dataSource.driverClassName");


        Class.forName(databaseDriverClassName);

        String dataSourceUrl = (String) props.get("dataSource.url" );
        String dataSourceUsername = (String) props.get("dataSource.username");
        String dataSourcePassword = (String) props.get("dataSource.password");

        DBI dbi = new DBI(dataSourceUrl, dataSourceUsername, dataSourcePassword );

        Handle h = dbi.open();
        h.execute("delete from card");
        h.execute("delete from board");
        h.execute("delete from authorities");
        h.execute("delete from users");



        String username = "ted";
        h.execute("insert into users (username, password) values ( ?, ?)",  username, "password" );
        Long userId = h.createQuery("select id from users where username = '" + username + "'" )
                .map(LongMapper.FIRST)
                .first();



        String boardName = "Test board";
        h.execute("insert into board ( name, user_id) values (?, ?)" , boardName, userId );
        Long boardId = h.createQuery("select id from board where name = '" + boardName + "'" )
                .map(LongMapper.FIRST)
                .first();



        Long id = h.createQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
                .map(LongMapper.FIRST)
                .first();

        String cardText = "Test card text";
        long cardLocation = 1;
        h.execute("insert into card (id, text, location, board_id) values (?, ?, ?, ?)", id, cardText, cardLocation, boardId);


        h.close();


        HttpClient httpclient = new DefaultHttpClient();

//            String uri = "http://localhost:9595/cards/board/" + boardId + "?postponed=true";

        String uri = "http://localhost:" + port + "/cards/board/" + boardId;
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

        assertThat(result).isEqualTo("{\"id\":" + id + "}");

    }



}
