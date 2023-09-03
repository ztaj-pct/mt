package com.pct.common.criteria;

/**
 *
 */

import com.pct.common.dto.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

//import org.springframework.data.jpa.domain.Specifications;

/**
 * @author Mayank
 *
 */
public class CriteriaSpecificationsBuilder<T extends Object> {

    private final List<SearchCriteria> params;

    public CriteriaSpecificationsBuilder() {
        params = new ArrayList<SearchCriteria>();
    }

    public CriteriaSpecificationsBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<T> build() {
        if (params.size() == 0) {
            return null;
        }

        List<Specification<T>> specs = new ArrayList<Specification<T>>();
        for (SearchCriteria param : params) {
            specs.add(new CriteriaSpecification(param));
        }

        Specification<T> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }
        return result;
    }
}