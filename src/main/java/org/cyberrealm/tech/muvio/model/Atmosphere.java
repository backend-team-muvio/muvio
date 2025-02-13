package org.cyberrealm.tech.muvio.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "atmospheres")
public class Atmosphere {
    @Id
    private Vibe vibe;

    public enum Vibe {
        MAKE_ME_CHILD, SCARY_ME_SILLY, MAKE_ME_FEEL_GOOD, MAKE_ME_DREAM,
        MAKE_ME_CURIOUS, TAKE_ME_TO_ANOTHER_WORLD, BLOW_ME_MIND, KEEP_ME_ON_EDGE
    }
}
