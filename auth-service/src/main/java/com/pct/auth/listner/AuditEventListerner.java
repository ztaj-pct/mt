package com.pct.auth.listner;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuditEventListerner implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {

		Authentication auth =SecurityContextHolder.getContext().getAuthentication();
		if(null != auth && auth.isAuthenticated() &&  !auth.getPrincipal().equals("anonymousUser")){

			return Optional.of(((UserDetails) auth.getPrincipal()).getUsername());
		}else {
			return Optional.of("SYSTEM");
		}
	}

}
