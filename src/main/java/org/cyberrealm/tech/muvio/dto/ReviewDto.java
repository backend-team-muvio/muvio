package org.cyberrealm.tech.muvio.dto;

import java.time.LocalDateTime;

public record ReviewDto(String id, String nickname, String text, LocalDateTime time){
}
