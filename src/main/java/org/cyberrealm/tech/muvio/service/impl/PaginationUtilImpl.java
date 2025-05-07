package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.MINUS_ONE;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.service.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PaginationUtilImpl implements PaginationUtil {
    private static final Random RANDOM = new Random();

    @Override
    public <T> Page<T> paginateList(Pageable pageable, List<T> list) {
        if (list.isEmpty()) {
            return emptyPage(pageable);
        }
        final List<T> sortedList = sortList(list, pageable.getSort());
        return createPage(sortedList, pageable);
    }

    @Override
    public <T> Page<T> paginateListWithOneRandomBefore(Pageable pageable, List<T> list) {
        if (list.isEmpty()) {
            return emptyPage(pageable);
        }
        final List<T> sortedList = sortList(list, pageable.getSort());
        final List<T> modifiedList = placeRandomItemFirst(sortedList);
        return createPage(modifiedList, pageable);
    }

    private <T> Page<T> emptyPage(Pageable pageable) {
        return new PageImpl<>(Collections.emptyList(), pageable, ZERO);
    }

    private <T> Page<T> createPage(List<T> list, Pageable pageable) {
        final int start = Math.min((int) pageable.getOffset(), list.size());
        final int end = Math.min(start + pageable.getPageSize(), list.size());
        final List<T> pageContent = list.subList(start, end);
        return new PageImpl<>(pageContent, pageable, list.size());
    }

    private <T> List<T> placeRandomItemFirst(List<T> list) {
        if (list.size() <= ONE) {
            return new ArrayList<>(list);
        }
        final List<T> modified = new ArrayList<>(list);
        final T randomItem = modified.remove(RANDOM.nextInt(Math.min(modified.size(), TEN)));
        modified.addFirst(randomItem);
        return modified;
    }

    private <T> List<T> sortList(List<T> list, Sort sort) {
        if (sort.isUnsorted() || list.isEmpty()) {
            return list;
        }
        final List<T> sortedList = new ArrayList<>(list);
        sortedList.sort((o1, o2) -> {
            for (Sort.Order order : sort) {
                try {
                    var field = o1.getClass().getDeclaredField(order.getProperty());
                    field.setAccessible(true);
                    Object value1 = field.get(o1);
                    Object value2 = field.get(o2);
                    if (value1 == null && value2 == null) {
                        return ZERO;
                    } else if (value1 == null) {
                        return ONE;
                    } else if (value2 == null) {
                        return MINUS_ONE;
                    }
                    if (value1 instanceof Comparable<?> && value2 instanceof Comparable<?>) {
                        Comparable<Object> comparable1 = (Comparable<Object>) value1;
                        Comparable<Object> comparable2 = (Comparable<Object>) value2;
                        int comparison = comparable1.compareTo(comparable2);

                        if (comparison != ZERO) {
                            return order.isAscending() ? comparison : -comparison;
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new EntityNotFoundException("Error sorting field: "
                            + order.getProperty());
                }
            }
            return ZERO;
        });
        return sortedList;
    }
}
