package org.cyberrealm.tech.muvio.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MovieBaseDtoWithPoints extends MovieBaseDto {
    private Integer points;
}
