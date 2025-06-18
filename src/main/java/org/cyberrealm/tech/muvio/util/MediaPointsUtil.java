package org.cyberrealm.tech.muvio.util;

import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import java.util.Set;
import org.cyberrealm.tech.muvio.model.Category;
import org.cyberrealm.tech.muvio.model.Media;

public class MediaPointsUtil {

    public static int calculatePoints(Media media, Set<String> categories) {
        int points = ZERO;
        if (categories != null && !categories.isEmpty()) {
            for (Category category : media.getCategories()) {
                if (categories.contains(category.name())) {
                    points++;
                }
            }
        }
        return points;
    }
}
