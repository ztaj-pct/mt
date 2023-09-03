package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pct.device.model.Lookup;
import com.pct.device.payload.LookupPayload;
import com.pct.device.repository.ILookupRepository;
import com.pct.device.util.BeanConverter;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class LookupServiceImplTest {

	@InjectMocks
	private LookupServiceImpl service;

	@Mock
	private ILookupRepository lookupRepository;

	@Mock
	private BeanConverter beanConverter;

	@Test
	@Order(1)
	public void getAllAssetConfigurationTest() {

		Long id = 11L;
		String field = "field";
		String value = "value";
		String displayLabel = "displayLabel";

		List<String> fields = new ArrayList<String>();
		fields.add("fghjd");
		fields.add("rtyui");

		Lookup lookup = new Lookup();
		lookup.setId(id);
		lookup.setField(field);
		lookup.setValue(value);
		lookup.setDisplayLabel(displayLabel);

		List<Lookup> lookups = new ArrayList<Lookup>();
		lookups.add(lookup);

		when(lookupRepository.findByField(fields)).thenReturn(lookups);

		LookupPayload lookupPayload = new LookupPayload();
		lookupPayload.setId(id);
		lookupPayload.setField(field);
		lookupPayload.setValue(value);
		lookupPayload.setDisplayLabel(displayLabel);

		List<LookupPayload> lookupPayloads = new ArrayList<LookupPayload>();
		lookupPayloads.add(lookupPayload);

		Map<String, List<LookupPayload>> assetConfigurationsMap = new HashMap<String, List<LookupPayload>>();
		assetConfigurationsMap.put("aa", lookupPayloads);

		LookupPayload lookupPayload1 = new LookupPayload();
		lookupPayload1.setId(lookup.getId());
		lookupPayload1.setField(lookup.getField());
		lookupPayload1.setValue(lookup.getValue());
		lookupPayload1.setDisplayLabel(lookup.getDisplayLabel());

		Mockito.when(beanConverter.assetConfigurationToAssetConfigurationPayload(lookup)).thenReturn(lookupPayload1);
		LookupPayload assetConfigurationToAssetConfigurationPayload = beanConverter
				.assetConfigurationToAssetConfigurationPayload(lookup);

//		Mockito.when(lookupPayloads.stream().collect(Collectors.groupingBy(asc -> asc.getField(), Collectors.toList())))
//				.thenReturn(assetConfigurationsMap);
		Map<String, List<LookupPayload>> allAssetConfiguration = service.getAllAssetConfiguration(fields);
		assertNotNull(allAssetConfiguration);
	}

	@Test
	@Order(2)
	public void saveLookupTest() {

		Long id = 11L;
		String field = "field";
		String value = "value";
		String displayLabel = "displayLabel";

		Lookup lookups = new Lookup();
		lookups.setId(id);
		lookups.setField(field);
		lookups.setValue(value);
		lookups.setDisplayLabel(displayLabel);

		LookupPayload lookupPayloads = new LookupPayload();
		lookupPayloads.setId(id);
		lookupPayloads.setField(field);
		lookupPayloads.setValue(value);
		lookupPayloads.setDisplayLabel(displayLabel);

		when(beanConverter.lookupPayloadToLookup(lookupPayloads)).thenReturn(lookups);
		when(lookupRepository.save(lookups)).thenReturn(lookups);
		service.saveLookup(lookupPayloads);
		assertNotNull(lookupPayloads);
		verify(lookupRepository, atLeast(1)).save(lookups);
	}

	@Test
	@Order(3)
	public void saveLookupTest1() {
		LookupPayload saveLookupPayload = null;
		try {
			service.saveLookup(saveLookupPayload);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@Order(4)
	public void getAllAssetConfigurationTest1() {
		List<String> fields = new ArrayList<String>();
		List<Lookup> lookups = new ArrayList<Lookup>();
		when(lookupRepository.findByField(fields)).thenReturn(lookups);
		try {
			Map<String, List<LookupPayload>> allAssetConfiguration = service.getAllAssetConfiguration(fields);
			assertNotNull(allAssetConfiguration);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}