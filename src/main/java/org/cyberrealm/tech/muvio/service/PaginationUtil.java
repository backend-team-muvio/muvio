package org.cyberrealm.tech.muvio.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaginationUtil {
    <T> Page<T> paginateList(final Pageable pageable, List<T> list);

    <T> Page<T> paginateListWithOneRandomBefore(
            Pageable pageable, List<T> list);
}
