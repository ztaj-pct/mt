/**
 *
 */
package com.pct.device.controller;

import com.pct.common.constant.Constants;
import com.pct.common.criteria.CriteriaSpecificationsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pct.device.constant.*;
import com.pct.common.criteria.CriteriaSpecificationsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author Exatip
 *
 */
public interface IApplicationController<T extends Object> {

    Logger logger = LoggerFactory.getLogger(IApplicationController.class);

    /**
     * @param search
     * @return
     */
    default Specification<T> getSpecification(String search) {

        logger.debug("Inside ICommunicationController method: getSpecification()");
        logger.debug("search value: " + search);
        CriteriaSpecificationsBuilder<T> builder = new CriteriaSpecificationsBuilder<T>();
        Pattern pattern = Pattern.compile(Constants.SEARCH_PATTERN);
        Matcher matcher = pattern.matcher(search + Constants.COMMA);

        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
        }

        Specification<T> spec = builder.build();

        logger.debug("Exiting from ICommunicationController method: getSpecification()");
        return spec;
    }

    /**
     * @param sort
     * @return
     */
    default Pageable getPageable(int page, int size, String sort, String _order) {

        if (null == sort)
            return  PageRequest.of(page, size);

        Pattern pattern = Pattern.compile(Constants.SORT_PATTERN);
        Matcher matcher = pattern.matcher(sort + Constants.COMMA);
        List<Sort.Order> orderList = new ArrayList<Sort.Order>();
        while (matcher.find()) {
            orderList.add(new Sort.Order(getEnum(matcher.group(3)), matcher.group(1)));
            logger.debug("shorting order: " + matcher.group(1) + ": " + matcher.group(3));
        }
        if ("ASC".equalsIgnoreCase(_order)) {
        	
            return  PageRequest.of(page, size, Sort.by(Sort.Direction.ASC,sort));
        } else {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,sort));
        }
    }


    /**
     * @param sort
     * @return
     */
    @SuppressWarnings("deprecation")
    default Pageable getPageableCustomizedSort(int page, int size, String sort, String _order) {

        if (null == sort)
            return  PageRequest.of(page, size);

        Pattern pattern = Pattern.compile(Constants.SORT_PATTERN);
        Matcher matcher = pattern.matcher(sort + Constants.COMMA);
        List<Sort.Order> orderList = new ArrayList<Sort.Order>();
        while (matcher.find()) {
            orderList.add(new Sort.Order(getEnum(matcher.group(3)), matcher.group(1)));
            logger.debug("shorting order: " + matcher.group(1) + ": " + matcher.group(3));
        }
        if (_order.equalsIgnoreCase("ASC")) {
            return  PageRequest.of(page, size, JpaSort.unsafe(Sort.Direction.ASC, "(" + sort + ")"));
        } else {
            return  PageRequest.of(page, size, JpaSort.unsafe(Sort.Direction.DESC, "(" + sort + ")"));
        }
    }

    /**
     * @param direction
     * @return
     */
    default Direction getEnum(String direction) {

        if (direction.equalsIgnoreCase("desc"))
            return Direction.DESC;
        else
            return Direction.ASC;
    }

    /**
     * @param page
     * @param size
     * @return
     */
    default boolean checkPagination(Integer page, Integer size) {

        return page != null && size != null && page >= 0 && size >= 0;
    }

}
