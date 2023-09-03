package com.pct.device.repository;

import com.pct.device.model.Lookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ILookupRepository extends JpaRepository<Lookup, Integer>, JpaSpecificationExecutor<Lookup> {

	 @Query("From Lookup l where l.field =  :field")
	 List<Lookup> findByField(@Param("field") String field);

    @Query("From Lookup l where l.field in :fields")
    List<Lookup> findByField(@Param("fields") List<String> fields);
    
    @Query("From Lookup l where l.field =  :eventId")
    Lookup findByFieldId(@Param("eventId") String eventId);


}
