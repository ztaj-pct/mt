package com.pct.auth.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pct.common.model.Location;
import com.pct.common.model.Role;
import com.pct.common.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	Optional<User> findByUserName(String userName);

	User findUserByUserName(String userName);

	Boolean existsByUserName(String userName);

	Boolean existsByEmail(String email);

	Boolean existsByPhone(String phone);

	Boolean existsByUuid(String uuid);

	Optional<User> findByEmail(String email);

	List<User> findByRole(Role role);

	@Transactional
	@Modifying
	@Query("Update User set password = :password, isPasswordChange = false where email = :email")
	Integer updatePassword(@Param("password") String password, @Param("email") String email);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE User set lastLoginTime=:dt WHERE email=:email")
	public void update(@Param(value = "email") String email,@Param(value = "dt") String dt);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE User set landingPage=:landing WHERE email=:email")
	public void updateLandingPage(@Param(value = "email") String email,@Param(value = "landing") String landing);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE User set isActive=:status WHERE email=:email")
	public void updateActiveStatus(@Param(value = "email") String email, @Param(value = "status") boolean status);

	@Query(value = "select user from User user LEFT JOIN user.additionalLocations addloc where addloc.id =:locationId")
	public List<User> getUserByLocationId(@Param(value="locationId") Long locationId);
	
}