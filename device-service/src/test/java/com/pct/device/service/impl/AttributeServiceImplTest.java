package com.pct.device.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pct.common.model.Attribute;
import com.pct.common.model.ProductMaster;
import com.pct.device.payload.AttributeDetails;
import com.pct.device.payload.AttributeRequest;
import com.pct.device.payload.AttributeResponse;
import com.pct.device.payload.ProductMasterResponse;
import com.pct.device.repository.IAttributeRepository;
import com.pct.device.repository.IProductMasterRepository;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class AttributeServiceImplTest {

	@InjectMocks
	AttributeServiceImpl attributeServiceImpl;

	@Mock
	private IAttributeRepository attributeRepository;

	@Mock
	private IProductMasterRepository productMasterRepository;

	@Mock
	private Pageable pageableMock;

	@Test
	public void addAttribute() throws InstantiationException {
		AttributeRequest attributeRequest = new AttributeRequest();
		attributeRequest.setProductUuid("1ea4e734-e551-4d22-99b2-0be6962ee8b3");
		attributeRequest.setAttributeName("name");
		attributeRequest.setAttributeValue("value");

		AttributeDetails attributeDetails = new AttributeDetails();
		attributeDetails.setApplicable(true);
		attributeDetails.setAttributeName("name");
		attributeDetails.setAttributeValue("value");

		List<AttributeDetails> list = new ArrayList<AttributeDetails>();
		list.add(attributeDetails);

		attributeRequest.setAttributeDetails(list);

		for (AttributeDetails attributeDetails1 : attributeRequest.getAttributeDetails()) {
			Attribute attribute = new Attribute();
			attribute.setAttributeName(attributeDetails1.getAttributeName());
			attribute.setAttributeValue(attributeDetails1.getAttributeValue());
			attribute.setApplicable(attributeDetails1.isApplicable());

			ProductMaster prod = new ProductMaster();
			prod.setProductCode("product_code");
			prod.setProductName("product_name");
			prod.setSubtype("subtype");
			prod.setUuid("1ea4e734-e551-4d22-99b2-0be6962eehf57");

			when(productMasterRepository.findByUuid(attributeRequest.getProductUuid())).thenReturn(prod);

			attribute.setProductMaster(prod);
			boolean isAttributeUuidUnique = false;
			String attributeUuid = "";
			while (!isAttributeUuidUnique) {
				attributeUuid = UUID.randomUUID().toString();
				Attribute byUuid = attributeRepository.findByUuid(attributeUuid);
				if (byUuid == null) {
					isAttributeUuidUnique = true;
				}
			}
			List<Attribute> att = attributeRepository.findByProductMasterUuidAndAttributeName(
					attribute.getAttributeName(), attribute.getProductMaster().getUuid());
			attribute.setUuid(attributeUuid);

			when(attributeRepository.save(any(Attribute.class))).thenReturn(attribute);

			Boolean addAttribute = attributeServiceImpl.addAttribute(attributeRequest);
			assertNotNull(addAttribute);

			verify(attributeRepository, atLeast(1)).save(attribute);
		}
	}

	@Test
	public void getAllAttributes() {
		ProductMaster productMaster = new ProductMaster();
		productMaster.setUuid("1ea4e734-e551-4d22-99b2-0be6962eehf57");
		productMaster.setProductName("Name");
		productMaster.setProductCode("34234");
		productMaster.setType("type");
		productMaster.setSubtype("subtype");

		Attribute attribute = new Attribute();
		attribute.setUuid("1ea4e734-e551-4d22-99b2-0be6962eehf57");
		attribute.setProductMaster(productMaster);
		attribute.setAttributeName("name");
		attribute.setAttributeValue("value");

		List<Attribute> list = new ArrayList<Attribute>();
		list.add(attribute);

		Page<Attribute> all = new PageImpl<Attribute>(list);
		PageRequest pageable = PageRequest.of(0, 20);

		when(attributeRepository.findAll(pageable)).thenReturn(all);

		List<AttributeResponse> attributeResponseList = new ArrayList<>();

		all.forEach(attribute1 -> {
			AttributeResponse attributeResponse = attributeToAttributeResponse(attribute);
			attributeResponseList.add(attributeResponse);
		});

		Page<AttributeResponse> pageOfAttributeResponse = new PageImpl<>(attributeResponseList, pageable,
				all.getTotalElements());
		assertNotNull(pageOfAttributeResponse);

		Page<AttributeResponse> allAttributes = attributeServiceImpl.getAllAttributes(pageable);
		assertNotNull(allAttributes);

		verify(attributeRepository, atLeast(1)).findAll(pageable);
	}

	@Test
	public AttributeResponse attributeToAttributeResponse(Attribute attribute) {
		ProductMaster productMaster = new ProductMaster();
		productMaster.setProductCode("5465454");
		productMaster.setProductName("name");
		productMaster.setUuid("1ea4e734-e551-4d22-99b2-0be6962eeh");
		productMaster.setBlocker(true);
		productMaster.setType("INSTALLER");
		productMaster.setId(45L);

		attribute = new Attribute();
		attribute.setUuid("1ea4e734-e551-4d22-99b2-0be6962eehf57");
		attribute.setAttributeName("name");
		attribute.setAttributeValue("value");
		attribute.setCreatedOn(Instant.now());
		attribute.setProductMaster(productMaster);

		AttributeResponse response = new AttributeResponse();
		response.setName(attribute.getAttributeName());
		response.setThreshold_Value(attribute.getAttributeValue());
		response.setAttribute_uuid(attribute.getUuid());
		ProductMaster prod = attribute.getProductMaster();
		ProductMasterResponse pmr = new ProductMasterResponse();
		pmr.setUuid(prod.getUuid());
		pmr.setProduct_name(prod.getProductName());
		pmr.setProduct_code(prod.getProductCode());
		pmr.setType(prod.getType());
		pmr.setId(prod.getId());
		response.setProduct_response(pmr);

		AttributeResponse attributeToAttributeResponse = attributeServiceImpl.attributeToAttributeResponse(attribute);
		assertNotNull(attributeToAttributeResponse);
		return attributeToAttributeResponse;
	}

	@Test
	public void getAllAttributeList() {
		ProductMaster productMaster = new ProductMaster();
		productMaster.setUuid("1ea4e734-e551-4d22-99b2-0be6962eehf57");
		productMaster.setProductName("Name");
		productMaster.setProductCode("34234");
		productMaster.setType("type");
		productMaster.setSubtype("subtype");

		Attribute attribute = new Attribute();
		attribute.setUuid("1ea4e734-e551-4d22-99b2-0be6962eehf57");
		attribute.setProductMaster(productMaster);
		attribute.setAttributeName("name");
		attribute.setAttributeValue("value");

		List<Attribute> all = new ArrayList<>();
		all.add(attribute);

		when(attributeRepository.findAll()).thenReturn(all);

		List<AttributeResponse> attributeResponseList = new ArrayList<>();

		all.forEach(attribute1 -> {
			AttributeResponse attributeResponse = attributeToAttributeResponse(attribute1);
			attributeResponseList.add(attributeResponse);
		});

		List<AttributeResponse> allAttributeList = attributeServiceImpl.getAllAttributeList();
		assertNotNull(allAttributeList);

		verify(attributeRepository, atLeast(1)).findAll();
	}
}