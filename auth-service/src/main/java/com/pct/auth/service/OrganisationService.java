package com.pct.auth.service;

import com.pct.common.model.Organisation;
import com.pct.common.util.Context;

public interface OrganisationService {
	
	public Organisation getByname(String name,  String token, Context context);
	
}
