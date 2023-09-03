//package com.pct.auth.service.impl;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.pct.auth.dto.MethodType;
//import com.pct.auth.dto.PermissionDto;
//import com.pct.auth.entity.PermissionEntity;
//import com.pct.auth.repository.PermissionRepository;
//
//@RunWith(SpringRunner.class)
//@SpringBootConfiguration
//@EnableJpaRepositories
//public class PermissionServiceImplTest {
//
//	@Mock
//	private PermissionRepository permissionRepository; 
//	
//	@InjectMocks 
//	private PermissionServiceImpl permissionService = new PermissionServiceImpl(permissionRepository);
//	
//	@Test
//	public void addPermissionTest() throws Exception {
//		
//		PermissionDto permissionDto = new PermissionDto();
//		permissionDto.setName("add permission");
//		permissionDto.setDescription("It is add permission.");
//		permissionDto.setMethodType(MethodType.POST);
//		permissionDto.setPath("/addPermission");
//		
//	    PermissionEntity permissionEntity = new PermissionEntity();
//	    permissionEntity.setName("add permission");
//	    permissionEntity.setDescription("It is add permission.");
//	    permissionEntity.setMethodType(MethodType.POST);
//	    permissionEntity.setPath("/addPermission");
//	    
//		when(permissionRepository.save(permissionEntity)).thenReturn(permissionEntity);
//
//		Assert.assertTrue(permissionService.addPermission(permissionDto));
//		verify(permissionRepository).save(permissionEntity);
//	}
//	
//	@Test
//	public void updatePermissionTest() throws Exception {
//		
//		PermissionDto permissionDto = new PermissionDto();
//		permissionDto.setName("update permission");
//		permissionDto.setDescription("It is update permission.");
//		permissionDto.setMethodType(MethodType.PUT);
//		permissionDto.setPath("/updatePermission");
//		
//		PermissionEntity permissionEntity = new PermissionEntity();
//		permissionEntity.setPermissionId(1);
//	    permissionEntity.setName("update permission");
//	    permissionEntity.setDescription("It is update permission.");
//	    permissionEntity.setMethodType(MethodType.PUT);
//	    permissionEntity.setPath("/updatePermission");
//		
//		when(permissionRepository.findByPermissionId(1)).thenReturn(permissionEntity);
//		
//		Assert.assertTrue(permissionService.updatePermission(1, permissionDto));
//		verify(permissionRepository).save(permissionEntity);
//		verify(permissionRepository).findByPermissionId(1);
//	}
//	
//	@Test(expected = RuntimeException.class)
//    public void should_throw_exception_when_permission_doesnt_exist() {
//		
//		PermissionDto permissionDto = new PermissionDto();
//		permissionDto.setName("update permission");
//		permissionDto.setDescription("It is update permission.");
//		permissionDto.setMethodType(MethodType.PUT);
//		permissionDto.setPath("/updatePermission");
//
//        PermissionEntity permissionEntity = new PermissionEntity();
//		permissionEntity.setPermissionId(1);
//	    permissionEntity.setName("update permission");
//	    permissionEntity.setDescription("It is update permission.");
//	    permissionEntity.setMethodType(MethodType.PUT);
//	    permissionEntity.setPath("/updatePermission");
//
//	    when(permissionRepository.findByPermissionId(1)).thenReturn(permissionEntity);
//	    
//        permissionService.updatePermission(5, permissionDto);
//    }
//	
//	@Test
//    public void whenGivenId_shouldDeletePermission_ifFound() {
//		
//		PermissionEntity permissionEntity = new PermissionEntity();
//		permissionEntity.setPermissionId(1);
//	    permissionEntity.setName("permission");
//	    permissionEntity.setDescription("It is delete permission.");
//	    permissionEntity.setMethodType(MethodType.DELETE);
//	    permissionEntity.setPath("/deletePermission");
//
//        when(permissionRepository.findByPermissionId(permissionEntity.getPermissionId()))
//        .thenReturn(permissionEntity);
//
//        permissionService.deletePermission(permissionEntity.getPermissionId());
//        verify(permissionRepository).delete(permissionEntity);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void should_throw_exception_when_permission_doesnt_exists() {
//    	
//    	PermissionEntity permissionEntity = new PermissionEntity();
//		permissionEntity.setPermissionId(1);
//	    permissionEntity.setName("permission");
//	    permissionEntity.setDescription("It is delete permission.");
//	    permissionEntity.setMethodType(MethodType.DELETE);
//	    permissionEntity.setPath("/deletePermission");
//
//        when(permissionRepository.findByPermissionId(permissionEntity.getPermissionId()))
//        .thenReturn(permissionEntity);
//        
//        permissionService.deletePermission(2);;
//    }
//}
