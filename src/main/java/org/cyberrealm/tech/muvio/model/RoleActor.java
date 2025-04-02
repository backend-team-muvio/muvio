package org.cyberrealm.tech.muvio.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class RoleActor {
    @Id
    private String id;
    private String role;
    private Actor actor;
}
