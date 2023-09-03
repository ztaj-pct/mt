//package com.pct.auth.service.impl;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.modelmapper.ModelMapper;
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.pct.auth.dto.MethodType;
//import com.pct.auth.dto.RoleDto;
//import com.pct.auth.entity.PermissionEntity;
//import com.pct.auth.entity.RoleEntity;
//import com.pct.auth.redis.common.RedisService;
//import com.pct.auth.repository.PermissionRepository;
//import com.pct.auth.repository.RoleRepository;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootConfiguration
//@EnableJpaRepositories
//public class RoleServiceImplTest {
//
//	@Mock
//	private RoleRepository roleRepository; 
//
//	@Mock
//	private PermissionRepository permissionRepository;
//
//	@Mock
//	private RedisService redisService;
//
//	@Mock
//	private ModelMapper modelMapper;
//
//	@InjectMocks 
//	private RoleServiceImpl roleService = new RoleServiceImpl(roleRepository, permissionRepository, redisService, modelMapper);
//
//	@Test
//	public void addRoleTest() throws Exception {
//
//		List<Integer> permissionlistId = new ArrayList<Integer>();
//		permissionlistId.add(1);
//		RoleDto roleDto = new RoleDto();
//		roleDto.setName("admin");
//		roleDto.setDescription("add role");
//		roleDto.setPermissionIdList(permissionlistId);
//
//		List<PermissionEntity> permissionEntityList = new ArrayList<PermissionEntity>();
//		PermissionEntity permissionEntity = new PermissionEntity();
//		permissionEntity.setPermissionId(1);
//		permissionEntity.setName("admin");
//		permissionEntity.setDescription("test permission");
//		permissionEntityList.add(permissionEntity);
//
//		RoleEntity roleEntity = new RoleEntity();
//		roleEntity.setName("admin");
//		roleEntity.setDescription("add role");
//		roleEntity.setPermissions(permissionEntityList);
//
//		when(permissionRepository.findByPermissionId(1)).thenReturn(permissionEntity);
//		when(roleRepository.save(roleEntity)).thenReturn(roleEntity);
//
//		Assert.assertTrue(roleService.addRole(roleDto));
//		verify(roleRepository).save(roleEntity);
//	}	
//
////	@Test
////	public void updateRoleTest() throws Exception {
////
////		List<Integer> permissionlistId = new ArrayList<Integer>();
////		permissionlistId.add(1);
////		RoleDto roleDto = new RoleDto();
////		roleDto.setName("admin1");
////		roleDto.setDescription("update role");
////		roleDto.setPermissionIdList(permissionlistId);
////
////		List<PermissionEntity> permissionEntityList = new ArrayList<PermissionEntity>();
////		PermissionEntity permissionEntity = new PermissionEntity();
////		permissionEntity.setPermissionId(1);
////		permissionEntity.setName("admin");
////		permissionEntity.setDescription("update role");
////		permissionEntity.setMethodType(MethodType.PUT);
////		permissionEntity.setPath("/updatePermission");
////		permissionEntityList.add(permissionEntity);
////
////		RoleEntity roleEntity = new RoleEntity();
////		roleEntity.setRoleId(1);
////		roleEntity.setName("admin2");
////		roleEntity.setDescription("update role");
////		roleEntity.setPermissions(permissionEntityList);
////        
////		when(permissionRepository.findByPermissionId(1)).thenReturn(permissionEntity);
////		when(roleRepository.findByRoleId(1)).thenReturn(roleEntity);
////		
////		Assert.assertTrue(roleService.updateRole(1,roleDto));
////		verify(roleRepository).save(roleEntity);
////		verify(roleRepository).findByRoleId(1);
////	}	
//
//	@Test(expected = RuntimeException.class)
//	public void should_throw_exception_when_role_doesnt_exist() {
//
//		RoleDto roleDto = new RoleDto();
//		roleDto.setName("updaterole");
//		roleDto.setDescription("update role");
//
//		RoleEntity roleEntity = new RoleEntity();
//		roleEntity.setRoleId(1);
//		roleEntity.setName("update role");
//		roleEntity.setDescription("It is updated role");
//
//		when(roleRepository.findByRoleId(1)).thenReturn(roleEntity);
//
//		roleService.updateRole(2, roleDto);
//	}
//
//	@Test
//	public void whenGivenId_shouldDeleteRole_ifFound() {
//
//		RoleEntity roleEntity = new RoleEntity();
//		roleEntity.setRoleId(1);
//		roleEntity.setName("update role");
//		roleEntity.setDescription("It is updated role");
//
//		when(roleRepository.findByRoleId(roleEntity.getRoleId()))
//		.thenReturn(roleEntity);
//
//		roleService.deleteRole(roleEntity.getRoleId());
//		verify(roleRepository).delete(roleEntity);
//	}
//
//	@Test(expected = RuntimeException.class)
//	public void should_throw_exception_when_role_doesnt_exists() {
//
//		RoleEntity roleEntity = new RoleEntity();
//		roleEntity.setRoleId(1);
//		roleEntity.setName("update role");
//		roleEntity.setDescription("It is updated role");
//
//		when(roleRepository.findByRoleId(roleEntity.getRoleId()))
//		.thenReturn(roleEntity);
//
//		roleService.deleteRole(2);;
//	}
//}
