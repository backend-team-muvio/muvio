package org.cyberrealm.tech.muvio.dto;

public record ReviewDto(String id, String author, String avatarPath,
                        String content, String time, Double rating) {
}
