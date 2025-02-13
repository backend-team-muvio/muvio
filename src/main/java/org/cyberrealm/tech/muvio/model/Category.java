package org.cyberrealm.tech.muvio.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {
    @Id
    private About about;

    public enum About {
        MOVIES_BASED_ON_A_TRUE_STORY, SPY_MOVIES_AND_COP_MOVIES,
        MOVIES_BASED_ON_A_BOOK, MUST_WATCH_LIST, GIRL_POWER_MOVIES,
        LIFE_CHANGING_MOVIES, SPORT_LIFE_MOVIES, IMD_TOP_250_MOVIES
    }
}
