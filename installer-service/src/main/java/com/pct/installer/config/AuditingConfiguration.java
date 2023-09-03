package com.pct.installer.config;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pct.common.model.User;
import com.pct.common.util.JwtUser;
import com.pct.installer.util.RestUtils;


/**
 * This class provide {@link Bean} managed by Spring container returning object
 * for {@link SpringSecurityAuditAwareImpl} class.
 * 
 * @author Aakash
 * 
 * @see AuditorAware
 *
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfiguration implements AuditorAware<User> {
	
	@Autowired
	private RestUtils restUtils;

	/**
	 * This method returns the username of current auditor of the application.
	 * 
	 * @return username of current auditor i.e. current logged in user.
	 *
	 * @see SecurityContextHolder#getContext()
	 *
	 */
	@Override
	public Optional<User> getCurrentAuditor() {
		JwtUser jwtUser = (JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = restUtils.getUserFromAuthServiceByName(jwtUser.getUsername());
		return Optional.of(user);
	}

	/**
	 * This method provides the {@link Bean} managed by Spring container returning
	 * {@link AuditingConfiguration} object.
	 * 
	 * @return {@link AuditorAware}
	 */
	@Bean
	public AuditorAware<User> auditorAware() {
		return new AuditingConfiguration();
	}
}

