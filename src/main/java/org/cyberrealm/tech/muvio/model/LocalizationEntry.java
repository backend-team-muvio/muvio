package org.cyberrealm.tech.muvio.model;

import lombok.Data;

@Data
public class LocalizationEntry {
    private String lang;
    private String movie;
    private String tvShows;
    private String shorts;
    private String timeH;
    private String timeM;
    private String ampersand;
}
