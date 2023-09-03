package com.pct.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Role;
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
	
	Role findByRoleId(Long roleId);

	Role findByName(String name);
	
	@Query("Select role.roleId, role.name, role.description FROM Role role")
	List<Role> findAllRoleName();
}