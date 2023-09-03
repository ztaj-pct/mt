package com.pct.auth.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.pct.common.model.PermissionEntity;

public interface PermissionRepository extends CrudRepository<PermissionEntity, Long>{

	PermissionEntity findByPermissionId(Integer permissionId);

	List<PermissionEntity> findByPermissionIdIn(List<Integer> permissionIdList);

	PermissionEntity findByName(String name);
}
