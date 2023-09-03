package com.pct.organisation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import com.pct.organisation.util.Constants;


public interface IApplicationController<T extends Object> {

	Logger logger = LoggerFactory.getLogger(IApplicationController.class);
	 

	/**
	 * @param sort
	 * @return
	 */
	default Pageable getPageable(int page, int size, String sort,String _order) {

		if (null == sort)
			return  PageRequest.of(page, size);

		Pattern pattern = Pattern.compile(Constants.SORT_PATTERN);
		Matcher matcher = pattern.matcher(sort + Constants.COMMA);
		List<Sort.Order> orderList = new ArrayList<Sort.Order>();
		while (matcher.find()) {
			orderList.add(new Sort.Order(getEnum(matcher.group(3)), matcher.group(1)));
			logger.debug("shorting order: " + matcher.group(1) + ": " + matcher.group(3));
		}
		if("ASC".equalsIgnoreCase(_order)) {
			return PageRequest.of(page, size,  Sort.by(sort).ascending());
		}else {
			return PageRequest.of(page, size,  Sort.by(sort).descending());
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

}
