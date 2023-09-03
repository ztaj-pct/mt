package com.pct.device.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pct.device.Bean.BatTestResponseBean;
import com.pct.device.Bean.LockAndFreeToolDataResponseBean;
import com.pct.device.Bean.LockAndFreeToolListBean;
import com.pct.device.Bean.LockAndFreeToolResponseBean;
import com.pct.device.Bean.ProductionToolBean;
import com.pct.device.Bean.ProductionToolResponseBean;
import com.pct.device.Bean.SpareToolResponseBean;
import com.pct.device.Bean.SpareToolSensorBean;
import com.pct.device.Bean.SpareToolSensorResponseBean;
import com.pct.device.Bean.TireToolResponseBean;
import com.pct.device.Bean.TireToolSensorBean;
import com.pct.device.Bean.TireToolSensorResponseBean;
import com.pct.device.model.BatTest;
import com.pct.device.model.FreeTool;
import com.pct.device.model.FreeToolData;
import com.pct.device.model.LockTool;
import com.pct.device.model.LockToolData;
import com.pct.device.model.ProductionTool;
import com.pct.device.model.SpareTool;
import com.pct.device.model.SpareToolSensor;
import com.pct.device.model.TireTool;
import com.pct.device.model.TireToolSensor;
import com.pct.device.repository.IBatTestRepository;
import com.pct.device.repository.IFreeToolDataRepository;
import com.pct.device.repository.IFreeToolRepository;
import com.pct.device.repository.ILockToolDataRepository;
import com.pct.device.repository.ILockToolRepository;
import com.pct.device.repository.IProductionToolRepository;
import com.pct.device.repository.ISpareToolRepository;
import com.pct.device.repository.ISpareToolSensorRepository;
import com.pct.device.repository.ITireToolRepository;
import com.pct.device.repository.ITireToolSensorRepository;
import com.pct.device.service.IProductionToolService;
@Service
public class ProductionToolServiceImpl implements IProductionToolService {
	Logger logger = LoggerFactory.getLogger(ProductionToolServiceImpl.class);

	@Autowired
	private IBatTestRepository batTestRepository;

	@Autowired
	private ISpareToolRepository spareToolRepository;

	@Autowired
	private ISpareToolSensorRepository spareToolSensorRepository;

	@Autowired
	private ITireToolRepository tireToolRepository;

	@Autowired
	private ITireToolSensorRepository tireToolSensorRepository;

	@Autowired
	private IProductionToolRepository productionToolRepository;
	
	@Autowired
	private ILockToolRepository lockToolRepository;
	
	@Autowired
	private ILockToolDataRepository lockToolDataRepository;
	
	@Autowired
	private IFreeToolRepository freeToolRepository;
	
	@Autowired
	private IFreeToolDataRepository freeToolDataRepository;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void saveProductionToolData(@Valid ProductionToolBean productionToolBean, String msgUuid) {
		logger.info("msgUuid : " + msgUuid + " Inside saveProductionToolData Method ");
		logger.info("msgUuid : " + msgUuid + "Input Received" + productionToolBean);
		ProductionTool productionTool = new ProductionTool();
		BeanUtils.copyProperties(productionToolBean, productionTool);
		productionTool.setUuid(UUID.randomUUID().toString());
		logger.info("msgUuid : " + msgUuid + "Tool Name : " + productionTool.getToolName());
		if (productionToolBean.getToolName().trim().equalsIgnoreCase("BAT-Test")) {
			BatTest batTest = new BatTest();
			BeanUtils.copyProperties(productionToolBean.getEventContent(), batTest);
			batTest.setUuid(UUID.randomUUID().toString());
			BatTest batTestSaved = batTestRepository.save(batTest);
			logger.info("msgUuid : " + msgUuid + "BatTest : " + batTestSaved);
			productionTool.setBatTest(batTestSaved);
			logger.info("msgUuid : " + msgUuid + "Production Tool After adding BatTest : " + productionTool);
		} else if (productionToolBean.getToolName().trim().equalsIgnoreCase("Spare-Tool")) {
			SpareTool spareTool = new SpareTool();
			BeanUtils.copyProperties(productionToolBean.getEventContent(), spareTool);
			spareTool.setUuid(UUID.randomUUID().toString());
			SpareTool spareToolSaved = spareToolRepository.save(spareTool);
			logger.info("msgUuid : " + msgUuid + "SpareTool : " + spareToolSaved);
			for (SpareToolSensorBean spareToolSensorBean : productionToolBean.getEventContent()
					.getSpareToolSensorList()) {
				SpareToolSensor spareToolSensor = new SpareToolSensor();
				BeanUtils.copyProperties(spareToolSensorBean, spareToolSensor);
				spareToolSensor.setUuid(UUID.randomUUID().toString());
				spareToolSensor.setSpareTool(spareToolSaved);
				spareToolSensorRepository.save(spareToolSensor);
			}
			productionTool.setSpareTool(spareToolSaved);
			logger.info("msgUuid : " + msgUuid + "Production Tool After adding SpareTool : " + productionTool);
		} else if (productionToolBean.getToolName().trim().equalsIgnoreCase("Tire-Tool")) {
			TireTool tireTool = new TireTool();
			BeanUtils.copyProperties(productionToolBean.getEventContent(), tireTool);
			tireTool.setUuid(UUID.randomUUID().toString());
			TireTool tireToolSaved = tireToolRepository.save(tireTool);
			logger.info("msgUuid : " + msgUuid + "TireTool : " + tireToolSaved);
			for (TireToolSensorBean tireToolSensorBean : productionToolBean.getEventContent().getTireToolSensorList()) {
				TireToolSensor tireToolSensor = new TireToolSensor();
				BeanUtils.copyProperties(tireToolSensorBean, tireToolSensor);
				tireToolSensor.setUuid(UUID.randomUUID().toString());
				tireToolSensor.setTireTool(tireToolSaved);
				tireToolSensorRepository.save(tireToolSensor);
			}
			productionTool.setTireTool(tireToolSaved);
			logger.info("msgUuid : " + msgUuid + "Production Tool After adding TireTool : " + productionTool);
		} else if (productionToolBean.getToolName().trim().equalsIgnoreCase("Lock-Tool")) {
			LockTool lockTool = new LockTool();
			lockTool.setUuid(UUID.randomUUID().toString());
			LockTool lockToolSaved = lockToolRepository.save(lockTool);
			logger.info("msgUuid : " + msgUuid + "LockTool : " + lockToolSaved);
			for (LockAndFreeToolListBean lockAndFreeToolListBean : productionToolBean.getEventContent().getLockList()) {
				LockToolData lockToolData = new LockToolData();
				BeanUtils.copyProperties(lockAndFreeToolListBean, lockToolData);
				lockToolData.setUuid(UUID.randomUUID().toString());
				lockToolData.setLockTool(lockToolSaved);
				lockToolDataRepository.save(lockToolData);
			}
			productionTool.setLockTool(lockToolSaved);
			logger.info("msgUuid : " + msgUuid + "Production Tool After adding LockTool : " + productionTool);
		}
		else if (productionToolBean.getToolName().trim().equalsIgnoreCase("Free-Tool")) {
			FreeTool freeTool = new FreeTool();
			freeTool.setUuid(UUID.randomUUID().toString());
			FreeTool freeToolSaved = freeToolRepository.save(freeTool);
			logger.info("msgUuid : " + msgUuid + "FreeTool : " + freeToolSaved);
			for (LockAndFreeToolListBean lockAndFreeToolListBean : productionToolBean.getEventContent().getLockList()) {
				FreeToolData freeToolData = new FreeToolData();
				BeanUtils.copyProperties(lockAndFreeToolListBean, freeToolData);
				freeToolData.setUuid(UUID.randomUUID().toString());
				freeToolData.setFreeTool(freeToolSaved);
				freeToolDataRepository.save(freeToolData);
			}
			productionTool.setFreeTool(freeToolSaved);
			logger.info("msgUuid : " + msgUuid + "Production Tool After adding FreeTool : " + productionTool);
		}
		productionToolRepository.save(productionTool);
	}

	@Override
	public Page<ProductionToolResponseBean> getProductionToolDataPagination(String startDate, String endDate,
			String toolName, Pageable pageable, String msgUuid,String queryParam,String queryParamValue) {
		logger.info("msgUuid : " + msgUuid + " Inside getProductionToolDataPagination Method : ");
		logger.info("msgUuid : " + msgUuid + " startDate : " + startDate + " endDate : " + endDate + " toolName : "
				+ toolName + " pageable : " + pageable);
		Specification<ProductionTool> productionToolSpec = getProductionToolSpecification(startDate, endDate, toolName,queryParam,queryParamValue);
		Page<ProductionTool> page = productionToolRepository.findAll(productionToolSpec, pageable);
		if( queryParam != null  && queryParam.equalsIgnoreCase("IMEI"))
		{
		Specification<ProductionTool> productionToolSpec1 = getProductionToolSpecification1(startDate, endDate, toolName, queryParam, queryParamValue);
		Specification<ProductionTool> productionToolSpec2 = getProductionToolSpecification2(startDate, endDate, toolName, queryParam, queryParamValue);
		Page<ProductionTool> page1 = productionToolRepository.findAll(productionToolSpec1, pageable);
		Page<ProductionTool> page2 = productionToolRepository.findAll(productionToolSpec2, pageable);
		List<ProductionTool> list = new ArrayList<>(page.getContent());
		list.addAll(page1.getContent());
		list.addAll(page2.getContent());
		Long totalElement = page.getTotalElements() + page1.getTotalElements() + page2.getTotalElements();
		page = new PageImpl<>(list, pageable, totalElement);
		
		}
		
		
		logger.info("msgUuid : " + msgUuid + " FindAll Result : " + page.getContent());
		List<ProductionToolResponseBean> list = new ArrayList<>();
		for (ProductionTool productionTool : page.getContent()) {
			ProductionToolResponseBean productionToolResponseBean = new ProductionToolResponseBean();
			BeanUtils.copyProperties(productionTool, productionToolResponseBean);
			if (productionTool.getBatTest() != null) {
				BatTestResponseBean batTestResponseBean = new BatTestResponseBean();
				BeanUtils.copyProperties(productionTool.getBatTest(), batTestResponseBean);
				productionToolResponseBean.setBatTest(batTestResponseBean);
			}

			if (productionTool.getSpareTool() != null) {
				SpareToolResponseBean spareToolResponseBean = new SpareToolResponseBean();
				BeanUtils.copyProperties(productionTool.getSpareTool(), spareToolResponseBean);

				List<SpareToolSensorResponseBean> spareToolSensorResponseBeans = new ArrayList<>();
				for (SpareToolSensor spareToolSensor : productionTool.getSpareTool().getSpareToolSensors()) {
					SpareToolSensorResponseBean spareToolSensorResponseBean = new SpareToolSensorResponseBean();
					BeanUtils.copyProperties(spareToolSensor, spareToolSensorResponseBean);
					spareToolSensorResponseBeans.add(spareToolSensorResponseBean);
				}
				spareToolResponseBean.setSpareToolSensors(spareToolSensorResponseBeans);
				productionToolResponseBean.setSpareTool(spareToolResponseBean);
			}

			if (productionTool.getTireTool() != null) {
				TireToolResponseBean tireToolResponseBean = new TireToolResponseBean();
				BeanUtils.copyProperties(productionTool.getTireTool(), tireToolResponseBean);
				productionToolResponseBean.setTireTool(tireToolResponseBean);

				List<TireToolSensorResponseBean> tireToolSensorResponseBeans = new ArrayList<>();
				for (TireToolSensor tireToolSensor : productionTool.getTireTool().getTireToolSensors()) {
					TireToolSensorResponseBean tireToolSensorResponseBean = new TireToolSensorResponseBean();
					BeanUtils.copyProperties(tireToolSensor, tireToolSensorResponseBean);
					tireToolSensorResponseBeans.add(tireToolSensorResponseBean);
				}
				tireToolResponseBean.setTireToolSensors(tireToolSensorResponseBeans);
				productionToolResponseBean.setTireTool(tireToolResponseBean);
			}

			if (productionTool.getLockTool() != null) {
				LockAndFreeToolResponseBean lockAndFreeToolResponseBean = new LockAndFreeToolResponseBean();
				BeanUtils.copyProperties(productionTool.getLockTool(), lockAndFreeToolResponseBean);
				productionToolResponseBean.setLockTool(lockAndFreeToolResponseBean);

				List<LockAndFreeToolDataResponseBean> lockandFreeToolDataResponseBeans = new ArrayList<>();
				for (LockToolData lockToolData : productionTool.getLockTool().getLockList()) {
					LockAndFreeToolDataResponseBean lockAndFreeToolDataResponseBean = new LockAndFreeToolDataResponseBean();
					BeanUtils.copyProperties(lockToolData, lockAndFreeToolDataResponseBean);
					lockandFreeToolDataResponseBeans.add(lockAndFreeToolDataResponseBean);
				}
				lockAndFreeToolResponseBean.setLockList(lockandFreeToolDataResponseBeans);
				productionToolResponseBean.setLockTool(lockAndFreeToolResponseBean);
			}
			
			if (productionTool.getFreeTool() != null) {
				LockAndFreeToolResponseBean lockAndFreeToolResponseBean = new LockAndFreeToolResponseBean();
				BeanUtils.copyProperties(productionTool.getFreeTool(), lockAndFreeToolResponseBean);
				productionToolResponseBean.setFreeTool(lockAndFreeToolResponseBean);

				List<LockAndFreeToolDataResponseBean> lockandFreeToolDataResponseBeans = new ArrayList<>();
				for (FreeToolData freeToolData : productionTool.getFreeTool().getLockList()) {
					LockAndFreeToolDataResponseBean lockAndFreeToolDataResponseBean = new LockAndFreeToolDataResponseBean();
					BeanUtils.copyProperties(freeToolData, lockAndFreeToolDataResponseBean);
					lockandFreeToolDataResponseBeans.add(lockAndFreeToolDataResponseBean);
				}
				lockAndFreeToolResponseBean.setLockList(lockandFreeToolDataResponseBeans);
				productionToolResponseBean.setFreeTool(lockAndFreeToolResponseBean);
			}

			list.add(productionToolResponseBean);
		}
		logger.info("msgUuid : " + msgUuid + " After Converting to Response Bean : " + list);
		Page<ProductionToolResponseBean> responsePage = new PageImpl<>(list, pageable, page.getTotalElements());
		return responsePage;
	}

	private Specification<ProductionTool> getProductionToolSpecification(String startDate, String endDate,
			String toolName, String queryParam, String queryParamValue) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			if (startDate != null && endDate != null) {
				predicates.add(criteriaBuilder.between(root.get("eventTimestamp"), startDate, endDate));
			}

			if (toolName != null) {
				predicates.add(criteriaBuilder.equal(root.get("toolName"), toolName.trim()));
			}
			if(queryParam != null && queryParamValue != null && queryParam != "" && queryParamValue != "")
			{
				if(queryParam.equalsIgnoreCase("receiver_serial_number"))
				{
					predicates.add(criteriaBuilder.equal(root.get("tireTool").get("receiverSerialNumber"), queryParamValue.trim()));
				}
				else if(queryParam.equalsIgnoreCase("IMEI"))
				{
				predicates.add(criteriaBuilder.equal(root.get("spareTool").get("imei"), queryParamValue.trim()));
				}
				else if(queryParam.equalsIgnoreCase("tpms_mac"))
				{
					predicates.add(criteriaBuilder.equal(root.get("batTest").get("tpmsMac"), queryParamValue.trim()));
				}
			}
			
			
			return  query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
	
	private Specification<ProductionTool> getProductionToolSpecification1(String startDate, String endDate,
			String toolName, String queryParam, String queryParamValue) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			if (startDate != null && endDate != null) {
				predicates.add(criteriaBuilder.between(root.get("eventTimestamp"), startDate, endDate));
			}

			if (toolName != null) {
				predicates.add(criteriaBuilder.equal(root.get("toolName"), toolName.trim()));
			}
			if(queryParam != null && queryParamValue != null && queryParam != "" && queryParamValue != "")
			{
				if(queryParam.equalsIgnoreCase("receiver_serial_number"))
				{
					predicates.add(criteriaBuilder.equal(root.get("tireTool").get("receiverSerialNumber"), queryParamValue.trim()));
				}
				else if(queryParam.equalsIgnoreCase("IMEI"))
				{
				predicates.add(criteriaBuilder.equal(root.join("lockTool").join("lockList").get("imei"), queryParamValue.trim()));
				}
				else if(queryParam.equalsIgnoreCase("tpms_mac"))
				{
					predicates.add(criteriaBuilder.equal(root.get("batTest").get("tpmsMac"), queryParamValue.trim()));
				}
			}
			
			
			return  query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
	
	private Specification<ProductionTool> getProductionToolSpecification2(String startDate, String endDate,
			String toolName, String queryParam, String queryParamValue) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			if (startDate != null && endDate != null) {
				predicates.add(criteriaBuilder.between(root.get("eventTimestamp"), startDate, endDate));
			}

			if (toolName != null) {
				predicates.add(criteriaBuilder.equal(root.get("toolName"), toolName.trim()));
			}
			if(queryParam != null && queryParamValue != null && queryParam != "" && queryParamValue != "")
			{
				if(queryParam.equalsIgnoreCase("receiver_serial_number"))
				{
					predicates.add(criteriaBuilder.equal(root.get("tireTool").get("receiverSerialNumber"), queryParamValue.trim()));
				}
				else if(queryParam.equalsIgnoreCase("IMEI"))
				{
				predicates.add(criteriaBuilder.equal(root.join("freeTool").join("lockList").get("imei"), queryParamValue.trim()));
				}
				else if(queryParam.equalsIgnoreCase("tpms_mac"))
				{
					predicates.add(criteriaBuilder.equal(root.get("batTest").get("tpmsMac"), queryParamValue.trim()));
				}
			}
			
			
			return  query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

}
