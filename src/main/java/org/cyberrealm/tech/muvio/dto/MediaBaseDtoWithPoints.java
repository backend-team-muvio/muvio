package org.cyberrealm.tech.muvio.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediaBaseDtoWithPoints extends MediaBaseDto {
    private Integer points;
}
