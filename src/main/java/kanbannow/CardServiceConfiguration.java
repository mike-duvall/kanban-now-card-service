package kanbannow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CardServiceConfiguration extends Configuration {
//    @NotEmpty
//    @JsonProperty
//    private String template;
//
//    @NotEmpty
//    @JsonProperty
//    private String defaultName = "Stranger";
//
//    public String getTemplate() {
//        return template;
//    }
//
//    public String getDefaultName() {
//        return defaultName;
//    }


    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();


    public DatabaseConfiguration getDatabase() {
        return database;
    }


}
