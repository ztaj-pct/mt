package com.pct.device.specification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.pct.common.constant.AssetCategory;
import com.pct.common.constant.DeviceStatus;
import com.pct.common.constant.GatewayStatus;
import com.pct.common.constant.GatewayType;
import com.pct.common.constant.IOTType;
import com.pct.common.model.Device;
import com.pct.common.model.DeviceForwarding;
import com.pct.common.model.LatestDeviceReportCount;
import com.pct.common.model.ReportCount;
import com.pct.common.model.Role;
import com.pct.common.model.User;
import com.pct.device.model.DeviceReportCount;
import com.pct.device.repository.IAssetDeviceXrefRepository;
import com.pct.device.util.AuthoritiesConstants;
import com.pct.device.util.StringUtils;
import com.pct.es.dto.Filter;

public final class DeviceSpecification {
	static Logger logger = LoggerFactory.getLogger(DeviceSpecification.class);

	@Autowired
	static IAssetDeviceXrefRepository assetDeviceXrefRepository;

	@Autowired
	private DeviceSpecification(IAssetDeviceXrefRepository assetDeviceXrefRepository) {
		DeviceSpecification.assetDeviceXrefRepository = assetDeviceXrefRepository;
	}

	public static Specification<Device> getDeviceListSpecification(String accountNumber, String imei, String uuid,
			DeviceStatus status, IOTType type, String mac, Map<String, Filter> attributeToValueMap,
			String filterModelCountFilter, User user, List<String> ls2, String sort) {
		List<String> nameList = new ArrayList<String>();
		return (root, query, criteriaBuilder) -> {
			Join<Device, DeviceForwarding> deviceForwardingJoin = root.join("deviceForwarding", JoinType.LEFT);
//			Join<Device, Asset_Device_xref> assetDeviceXrefJoin = root.join("assetDeviceXref", JoinType.LEFT);
//			Join<Asset_Device_xref, Asset> assetJoin = assetDeviceXrefJoin.join("asset", JoinType.LEFT);
			List<Predicate> predicates = new ArrayList<>();
			boolean roleAvailable = false;
			boolean roleCallCenter = false;

			if (sort != null) {
				if (sort.contains(".")) {
					Path path = null;
					From from = root;
					String[] sortArr = sort.split("[.]");
					for (int i = 0; i < sortArr.length; i++) {
						if (i == sortArr.length - 1) {
							path = from.get(sortArr[i]);
						} else {
							from = from.join(sortArr[i]);
						}
					}
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(path),
							criteriaBuilder.notEqual(path.as(String.class), "")));
				} else {
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get(sort)),
							criteriaBuilder.notEqual(root.get(sort).as(String.class), "")));
				}
			}

			for (Role roles : user.getRole()) {
				if (roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)) {
					roleAvailable = true;
				}
				if (roles.getName().contains(AuthoritiesConstants.ROLE_CALL_CENTER)) {
					roleCallCenter = true;
				}
			}
			if (roleAvailable) {
				// predicates.add(criteriaBuilder.equal(root.get("organisation").get("id"),
				// user.getOrganisation().getId()));
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("id"), "1"));
			}
			if (roleCallCenter) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), "A-00243"));
			}

			if (!StringUtils.isEmpty(imei)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
				logger.info("IMEIIIIII " + imei);
			}

			if (!StringUtils.isEmpty(status)) {
				predicates.add(criteriaBuilder.equal(root.get("status"), status));
				logger.info("Status " + status);
			}

			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
				logger.info("uuid " + uuid);
			}

			if (!StringUtils.isEmpty(accountNumber)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), accountNumber));
				logger.info("accountNumber " + accountNumber);
			}
			if (!StringUtils.isEmpty(mac)) {
				predicates.add(criteriaBuilder.equal(root.get("macAddress"), mac));
				logger.info("mac " + mac);
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
				logger.info("iotType " + type);
			}
			List<Predicate> criteriaImei = new ArrayList<Predicate>();
			List<Predicate> criteriaProduct = new ArrayList<Predicate>();

			attributeToValueMap.forEach((attribute, value) -> {
//				if (!StringUtils.isEmpty(value.getValue())) {

				if (value.getKey().equalsIgnoreCase("lastReportDate")
						|| value.getKey().equalsIgnoreCase("last_report")) {
					String dateFormate = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
					if (value.getKey().equalsIgnoreCase("last_report")) {
						dateFormate = "yyyy-MM-dd HH:mm:ss";
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
					Date date1;
					try {
						date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("latestReport"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("latestReport"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("latestReport"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("latestReport"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (value.getKey().equalsIgnoreCase("installedDateTimestamp")) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
					try {
						Date date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(
									criteriaBuilder.greaterThanOrEqualTo(root.get("installedDateTimestamp"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates
									.add(criteriaBuilder.lessThanOrEqualTo(root.get("installedDateTimestamp"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("installedDateTimestamp"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("installedDateTimestamp"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

//				if (value.getKey().equalsIgnoreCase("deviceType")) {
//					nameList.add("deviceType");
//					criteriaProduct.add(criteriaBuilder.equal(root.get("productName"), value.getValue()));
//				}
				if (value.getKey().equalsIgnoreCase("product_name")) {
					nameList.add("productName");
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						criteriaProduct.add(criteriaBuilder.equal(root.get("productName"), pStatus));
					}
				}

				if (value.getKey().equalsIgnoreCase("device_type")) {
					nameList.add("deviceType");
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						criteriaProduct.add(criteriaBuilder.equal(root.get("deviceType"), pStatus));
					}
				}

				if (value.getKey().equalsIgnoreCase("accountNumber")) {
					List<Predicate> accoutnNumberStatus = new ArrayList<Predicate>();
					String[] allNumber = value.getValue().split(",");
					for (String customerAccountNumber : allNumber) {
						accoutnNumberStatus.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"),
								customerAccountNumber));
					}
					predicates.add(criteriaBuilder.or(accoutnNumberStatus.toArray(new Predicate[0])));
				}
				if (value.getKey().equalsIgnoreCase("productCode")) {
					predicates.add(criteriaBuilder.like(root.get("productCode"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config_name")) {
					predicates.add(criteriaBuilder.like(root.get("configName"), "%" + value.getValue() + "%"));
				}
				

				if (value.getKey().equalsIgnoreCase("imei")) {
					predicates.add(criteriaBuilder.like(root.get("imei"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("imeiList") || value.getKey().equalsIgnoreCase("assetIdList")) {
					nameList.add("imeiList");
					if (value.getKey().equalsIgnoreCase("imeiList")) {
						String[] allImei = value.getValue().split(",");
						for (String imeis : allImei) {
							criteriaImei.add(criteriaBuilder.equal(root.get("imei"), imeis));
						}
					} else {
						for (String imeis : ls2) {
							criteriaImei.add(criteriaBuilder.equal(root.get("imei"), imeis));
						}
					}
				}
				if (value.getKey().equalsIgnoreCase("son")) {
					predicates.add(criteriaBuilder.like(root.get("son"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("status")) {
					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						if (pStatus != "")
							criteriaStatus.add(criteriaBuilder.equal(root.get("status"),
									DeviceStatus.getGatewayStatusInSearch(pStatus)));
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}

				if (value.getKey().equalsIgnoreCase("imei_hashed")) {
					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						if (pStatus != "" && pStatus.equals("Unsecured")) {
							criteriaStatus.add(
									criteriaBuilder.equal(root.get("deviceSignature").get("imeiHashed"), "REVOKED"));
						} else if (pStatus != "" && pStatus.equals("Secured")) {
							criteriaStatus.add(
									criteriaBuilder.notEqual(root.get("deviceSignature").get("imeiHashed"), "REVOKED"));
						}
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}

				if (value.getKey().equalsIgnoreCase("cellular")) {
					predicates.add(
							criteriaBuilder.like(root.get("cellular").get("cellular"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("phone")) {
					predicates
							.add(criteriaBuilder.like(root.get("cellular").get("phone"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("imsi")) {
					predicates
							.add(criteriaBuilder.like(root.get("cellular").get("imsi"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("service_country")) {
					predicates.add(criteriaBuilder.like(root.get("cellular").get("serviceCountry"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("service_network")) {
					predicates.add(criteriaBuilder.like(root.get("cellular").get("serviceNetwork"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("created_by")) {
					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						criteriaStatus
								.add(criteriaBuilder.equal(root.get("organisation").get("organisationName"), pStatus));
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}
				if (value.getKey().equalsIgnoreCase("qa_status")) {

					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						criteriaStatus.add(criteriaBuilder.equal(root.get("qaStatus"), pStatus));
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}

				if (value.getKey().equalsIgnoreCase("qa_date") || value.getKey().equalsIgnoreCase("qa_date_advance")) {
					String date = value.getValue();
					try {
						String dateFormate = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
						SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
						if (value.getKey().equalsIgnoreCase("qa_date")) {
							SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							date = dateFormat.format(dateFormat1.parse(date));
						}

						Instant instant = Instant.parse(date);
						Instant instant2 = instant.plus(23 * 59, ChronoUnit.MINUTES);
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("qaDate"), instant));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("qaDate"), instant));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("qaDate"), instant, instant2));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("qaDate"), instant));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (value.getKey().equalsIgnoreCase("usage_status")) {
					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						criteriaStatus.add(criteriaBuilder.equal(root.get("usageStatus"), pStatus));
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}

				if (value.getKey().equalsIgnoreCase("url1") || value.getKey().equalsIgnoreCase("url2")
						|| value.getKey().equalsIgnoreCase("url3") || value.getKey().equalsIgnoreCase("url4")) {
					predicates.add(criteriaBuilder.like(deviceForwardingJoin.get("url"), "%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("type1") || value.getKey().equalsIgnoreCase("type2")
						|| value.getKey().equalsIgnoreCase("type3") || value.getKey().equalsIgnoreCase("type4")) {
					List<Predicate> criteriaType = new ArrayList<Predicate>();
					String[] allType = value.getValue().split(",");
					for (String types : allType) {
						criteriaType.add(criteriaBuilder.equal(deviceForwardingJoin.get("type"), types));
					}
					predicates.add(criteriaBuilder.or(criteriaType.toArray(new Predicate[0])));
//						predicates.add(criteriaBuilder.equal(deviceForwardingJoin.get("type"), value.getValue()));
				}
				if (value.getKey().equalsIgnoreCase("asset_name")) {
					predicates.add(criteriaBuilder.like(root.get("assetDeviceXref").get("asset").get("assignedName"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("asset_type")) {
					List<Predicate> criteriaAssetCategory = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String assetStatus : allStatus) {
						criteriaAssetCategory
								.add(criteriaBuilder.equal(root.get("assetDeviceXref").get("asset").get("category"),
										AssetCategory.getAssetCategory(assetStatus)));
					}
					predicates.add(criteriaBuilder.or(criteriaAssetCategory.toArray(new Predicate[0])));
//					predicates.add( criteriaBuilder.equal(root.get("assetDeviceXref").get("asset").get("category"),
//							AssetCategory.getAssetCategory(value.getValue())));
				}
				if (value.getKey().equalsIgnoreCase("vin")) {
					predicates.add(criteriaBuilder.like(root.get("assetDeviceXref").get("asset").get("vin"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("manufacturer")) {
					predicates.add(criteriaBuilder.like(
							root.get("assetDeviceXref").get("asset").get("manufacturer").get("name"),
							"%" + value.getValue() + "%"));
				}

				// Details Filter

				if (value.getKey().equalsIgnoreCase("last_report_date_time")) {
					try {
						String dateFormate = "yyyy-MM-dd HH:mm:ss";
						SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
						Date date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder
									.greaterThanOrEqualTo(root.get("deviceDetails").get("latestReport"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder
									.lessThanOrEqualTo(root.get("deviceDetails").get("latestReport"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("deviceDetails").get("latestReport"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(
									criteriaBuilder.notEqual(root.get("deviceDetails").get("latestReport"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

//				if (value.getKey().equalsIgnoreCase("event_id")) {
//					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
//					String[] allStatus = value.getValue().split(",");
//					for (String pStatus : allStatus) {
//						if(pStatus!="")
//						criteriaStatus.add(criteriaBuilder.equal(root.get("deviceDetails").get("eventId"), Integer.parseInt(pStatus)));
//					}
//					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
//				}
				if (value.getKey().equalsIgnoreCase("event_type")) {
					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						if (pStatus != "")
							criteriaStatus
									.add(criteriaBuilder.equal(root.get("deviceDetails").get("eventType"), pStatus));
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}

				if (value.getKey().equalsIgnoreCase("event_id")) {
					predicates.add(criteriaBuilder.equal(root.get("deviceDetails").get("eventId"), value.getValue()));
				}

				if (value.getKey().equalsIgnoreCase("battery")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("battery").as(String.class),
							"%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("lat")) {
					if (value.getOperator().equalsIgnoreCase("eq")) {
						predicates.add(criteriaBuilder.equal(root.get("deviceDetails").get("lat").as(String.class),
								value.getValue()));
					} else {
						predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("lat").as(String.class),
								"%" + value.getValue() + "%"));
					}

				}
				if (value.getKey().equalsIgnoreCase("longitude")) {
					if (value.getOperator().equalsIgnoreCase("eq")) {
						predicates.add(criteriaBuilder
								.equal(root.get("deviceDetails").get("longitude").as(String.class), value.getValue()));
					} else {
						predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("longitude").as(String.class),
								"%" + value.getValue() + "%"));
					}

				}
				if (value.getKey().equalsIgnoreCase("bin_version")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("binVersion"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("app_version")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("appVersion"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("mcu_version")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("mcuVersion"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("ble_version")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("bleVersion"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config1_name")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config1Name"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config1_crc")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config1CRC"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config2_name")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config2Name"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config2_crc")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config2CRC"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config3_name")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config3Name"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config3_crc")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config3CRC"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config4_name")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config4Name"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("config4_crc")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("config4CRC"),
							"%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("devuser_cfg_name")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("devuserCfgName"),
							"%" + value.getValue() + "%"));
				}
				if (value.getKey().equalsIgnoreCase("devuser_cfg_value")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("devuserCfgValue"),
							"%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("installed_status_flag")) {
					List<Predicate> criteriaStatus = new ArrayList<Predicate>();
					String[] allStatus = value.getValue().split(",");
					for (String pStatus : allStatus) {
						if (pStatus != "")
							criteriaStatus.add(criteriaBuilder
									.equal(root.get("deviceDetails").get("installedStatusFlag"), pStatus));
					}
					predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
				}

				if (value.getKey().equalsIgnoreCase("hw_id_version")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("hwIdVersion"),
							"%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("hw_version_revision")) {
					predicates.add(criteriaBuilder.like(root.get("deviceDetails").get("hwVersionRevision"),
							"%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("forwarding_group")) {
					if (value.getValue().equalsIgnoreCase("")) {
						predicates.add(criteriaBuilder.isEmpty(root.get("organisation").get("forwardingGroupMappers")));
					} else {
						String[] forwardingGroupArray = value.getValue().split(",");
						List<String> forwardingGroupList = Arrays.asList(forwardingGroupArray);
						predicates
								.add(criteriaBuilder
										.in(root.join("organisation").join("forwardingGroupMappers")
												.get("customerForwardingGroup").get("name"))
										.value(forwardingGroupList));
					}
				}

				if (value.getKey().equalsIgnoreCase("purchase_by_name")) {
					if (value.getValue().equalsIgnoreCase("")) {
						predicates.add(criteriaBuilder.isNull(root.get("purchaseBy")));
					} else {
						String[] purchaseByArray = value.getValue().split(",");
						List<String> purchaseByList = Arrays.asList(purchaseByArray);
						predicates.add(criteriaBuilder.in(root.get("purchaseBy").get("organisationName"))
								.value(purchaseByList));
					}
				}

				if (value.getKey().equalsIgnoreCase("revoked_time")) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");
					try {
						Date date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder
									.greaterThanOrEqualTo(root.get("deviceSignature").get("revokedTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder
									.lessThanOrEqualTo(root.get("deviceSignature").get("revokedTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("deviceSignature").get("revokedTime"),
									date1, new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(
									criteriaBuilder.notEqual(root.get("deviceSignature").get("revokedTime"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

//				}
			});
			if (nameList.contains("imeiList")) {
				predicates.add(criteriaBuilder.or(criteriaImei.toArray(new Predicate[0])));
			}
			if (nameList.contains("deviceType") || nameList.contains("productName")) {
				predicates.add(criteriaBuilder.or(criteriaProduct.toArray(new Predicate[0])));
			}

			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).distinct(true)
					.getRestriction();
		};
	}

	public static Specification<Device> getDeviceWithSensorListSpecification(String accountNumber,
			Map<String, String> attributeToValueMap, String filterModelCountFilter, User user, String sort) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			boolean roleAvailable = false;
			for (Role roles : user.getRole()) {
				if (roles.getName().contains(AuthoritiesConstants.ROLE_CUSTOMER_ADMIN)) {
					roleAvailable = true;
					break;
				}
			}
			if (roleAvailable) {
				predicates
						.add(criteriaBuilder.equal(root.get("organisation").get("id"), user.getOrganisation().getId()));
			}
			if (!StringUtils.isEmpty(accountNumber)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("id"), accountNumber));
				logger.info("accountNumber " + accountNumber);
			}

			if (sort != null) {
				if (sort.contains(".")) {
					Path path = null;
					From from = root;
					String[] sortArr = sort.split("[.]");
					for (int i = 0; i < sortArr.length; i++) {
						if (i == sortArr.length - 1) {
							path = from.get(sortArr[i]);
						} else {
							from = from.join(sortArr[i]);
						}
					}
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(path),
							criteriaBuilder.notEqual(path.as(String.class), "")));
				} else {
					predicates.add(criteriaBuilder.and(criteriaBuilder.isNotNull(root.get(sort)),
							criteriaBuilder.notEqual(root.get(sort).as(String.class), "")));
				}
			}

			predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("iotType"), IOTType.GATEWAY),
					criteriaBuilder.equal(root.get("iotType"), IOTType.BEACON)));

			attributeToValueMap.forEach((attribute, value) -> {
				if (!StringUtils.isEmpty(value)) {

					if (attribute.equalsIgnoreCase("created_at")) {
						String dateFormate = "yyyy-MM-dd hh:mm:ss";
						SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);

						try {
							Date date = dateFormat.parse(value);
							Instant instant2 = new Date(date.getTime() + 86340000).toInstant();
							Instant instant = date.toInstant();
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), instant));
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), instant2));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					if (attribute.equalsIgnoreCase("updated_at")) {
						String dateFormate = "yyyy-MM-dd hh:mm:ss";
						SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);

						try {
							Date date = dateFormat.parse(value);
							Instant instant2 = new Date(date.getTime() + 86340000).toInstant();
							Instant instant = date.toInstant();
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), instant));
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), instant2));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					if (attribute.equalsIgnoreCase("son")) {
						predicates.add(criteriaBuilder.like(root.get("son"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("createdBy")) {
//						predicates.add(criteriaBuilder.like(root.get("organisation").get("organisationName"), "%" + value + "%"));
						predicates.add(criteriaBuilder.like(root.get("createdBy").get("firstName"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("updatedBy")) {
//						predicates.add(criteriaBuilder.like(root.get("organisation").get("organisationName"), "%" + value + "%"));
						predicates.add(criteriaBuilder.like(root.get("updatedBy").get("firstName"), "%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("companyName")) {
						predicates.add(criteriaBuilder.like(root.get("organisation").get("organisationName"),
								"%" + value + "%"));
					}

					if (attribute.equalsIgnoreCase("imei")) {
						predicates.add(criteriaBuilder.like(root.get("imei"), "%" + value + "%"));
//						predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("macAddress"), "%" + value + "%")));
					}

					if (attribute.equalsIgnoreCase("productName")) {
						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String pStatus : allStatus) {
							criteriaStatus.add(criteriaBuilder.equal(root.get("productName"), pStatus));
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					}

					if (attribute.equalsIgnoreCase("status")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String status : allStatus) {
							if (DeviceStatus.getGatewayStatusInSearch(status) != null) {
								criteriaStatus.add(criteriaBuilder.equal(root.get("status"),
										DeviceStatus.getGatewayStatusInSearch(status)));
							}
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					}

					if (attribute.equalsIgnoreCase("gateway_type")) {

						List<Predicate> criteriaStatus = new ArrayList<Predicate>();
						String[] allStatus = value.split(",");
						for (String status : allStatus) {
							if (DeviceStatus.getGatewayStatusInSearch(status) != null) {
								criteriaStatus
										.add(criteriaBuilder.equal(root.get("iotType"), IOTType.getValue(status)));
							}
						}
						predicates.add(criteriaBuilder.or(criteriaStatus.toArray(new Predicate[0])));
					}
				}
			});
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<Device> getDeviceSpec(String can, String imei, String uuid, IOTType type) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
				logger.info("uuid " + uuid);
			}

			if (!StringUtils.isEmpty(can)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), can));
				logger.info("accountNumber " + can);
			}
			if (!StringUtils.isEmpty(imei)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
				logger.info("imei " + imei);
			}
			if (!StringUtils.isEmpty(type)) {
				predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
				logger.info("iotType " + type);
			}
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<Device> getDeviceListSpec(String accountNumber, String uuid, String deviceId) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (!StringUtils.isEmpty(uuid)) {
				predicates.add(criteriaBuilder.equal(root.get("uuid"), uuid));
			}

			if (!StringUtils.isEmpty(accountNumber)) {
				predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), accountNumber));
			}
			if (!StringUtils.isEmpty(deviceId)) {
				predicates.add(criteriaBuilder.equal(root.get("imei"), deviceId));
			}
			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<LatestDeviceReportCount> getLatestDeviceReportCountListSpecification(
			Map<String, Filter> attributeToValueMap, String filterModelCountFilter, User user) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			attributeToValueMap.forEach((attribute, value) -> {
				if (value.getKey().equalsIgnoreCase("ms2dateTime")) {
					String dateFormate = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
					if (value.getKey().equalsIgnoreCase("ms2dateTime")) {
						dateFormate = "yyyy-MM-dd HH:mm:ss";
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
					Date date1;
					try {
						date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ms2dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ms2dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("ms2dateTime"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("ms2dateTime"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (value.getKey().equalsIgnoreCase("ms1dateTime")) {
					String dateFormate = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
					if (value.getKey().equalsIgnoreCase("ms1dateTime")) {
						dateFormate = "yyyy-MM-dd HH:mm:ss";
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
					Date date1;
					try {
						date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ms1dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ms1dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("ms1dateTime"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("ms1dateTime"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (value.getKey().equalsIgnoreCase("deviceId")) {
					predicates.add(criteriaBuilder.like(root.get("deiviceId"), "%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("ms1lastRecordId")) {
					predicates.add(criteriaBuilder.equal(root.get("ms1lastRecordId"), value.getValue()));
				}

				if (value.getKey().equalsIgnoreCase("ms1recordcount")) {
					predicates.add(criteriaBuilder.equal(root.get("ms1recordcount"), value.getValue()));
				}

				if (value.getKey().equalsIgnoreCase("ms2lastRecordId")) {
					predicates.add(criteriaBuilder.equal(root.get("ms2lastRecordId"), value.getValue()));
				}

				if (value.getKey().equalsIgnoreCase("ms2recordcount")) {
					predicates.add(criteriaBuilder.equal(root.get("ms2recordcount"), value.getValue()));
				}

				if (value.getKey().equalsIgnoreCase("can")) {
					predicates.add(criteriaBuilder.like(root.get("can"), "%" + value.getValue() + "%"));
				}

			});

			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<DeviceReportCount> getDeviceReportCountListSpecification(
			Map<String, Filter> attributeToValueMap, String filterModelCountFilter, User user) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			attributeToValueMap.forEach((attribute, value) -> {

				if (value.getKey().equalsIgnoreCase("ms2OrganisationName")) {
					predicates.add(criteriaBuilder.like(root.get("ms2OrganisationName"), "%" + value.getValue() + "%"));
				}

//				if (value.getKey().equalsIgnoreCase("ms1lastRecordId")) {
//					predicates.add(criteriaBuilder.equal(root.get("ms1lastRecordId"),  value.getValue()));
//				}

				if (value.getKey().equalsIgnoreCase("ms1recordcount")) {
					predicates.add(criteriaBuilder.equal(root.get("ms1recordcount"), value.getValue()));
				}

//				if (value.getKey().equalsIgnoreCase("diff")) {
//					predicates.add(criteriaBuilder.equal(root.get("diff"),  value.getValue()));
//				}
//
				if (value.getKey().equalsIgnoreCase("ms2Count")) {
					predicates.add(criteriaBuilder.equal(root.get("ms2Count"), value.getValue()));
				}
//				
//				if (value.getKey().equalsIgnoreCase("can")) {
//					predicates.add(criteriaBuilder.like(root.get("can"), "%" + value.getValue() + "%"));
//				}

			});

			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}

	public static Specification<ReportCount> getReportCountListSpecification(Map<String, Filter> attributeToValueMap,
			String filterModelCountFilter, User user) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			attributeToValueMap.forEach((attribute, value) -> {
				if (value.getKey().equalsIgnoreCase("ms2dateTime")) {
					String dateFormate = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
					if (value.getKey().equalsIgnoreCase("ms2dateTime")) {
						dateFormate = "yyyy-MM-dd HH:mm:ss";
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
					Date date1;
					try {
						date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ms2dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ms2dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("ms2dateTime"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("ms2dateTime"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (value.getKey().equalsIgnoreCase("ms1dateTime")) {
					String dateFormate = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
					if (value.getKey().equalsIgnoreCase("ms1dateTime")) {
						dateFormate = "yyyy-MM-dd HH:mm:ss";
					}
					SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormate);
					Date date1;
					try {
						date1 = dateFormat.parse(value.getValue());
						if (value.getOperator().equalsIgnoreCase("gt")) {
							predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ms1dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("lt")) {
							predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ms1dateTime"), date1));
						} else if (value.getOperator().equalsIgnoreCase("date")) {
							predicates.add(criteriaBuilder.between(root.get("ms1dateTime"), date1,
									new Date(date1.getTime() + 86400000)));
						} else if (value.getOperator().equalsIgnoreCase("notequal")) {
							predicates.add(criteriaBuilder.notEqual(root.get("ms1dateTime"), date1));
						}

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (value.getKey().equalsIgnoreCase("deviceId")) {
					predicates.add(criteriaBuilder.like(root.get("deviceId"), "%" + value.getValue() + "%"));
				}

				if (value.getKey().equalsIgnoreCase("count")) {
					predicates.add(criteriaBuilder.equal(root.get("count"), value.getValue()));
				}

				if (value.getKey().equalsIgnoreCase("can")) {
					predicates.add(criteriaBuilder.like(root.get("can"), "%" + value.getValue() + "%"));
				}

			});

			return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
		};
	}
	
	public static Specification<Device> getGatewayListSpecification(String accountNumber, String imei, String gatewayUuid, DeviceStatus status, IOTType type, String mac, Instant lastDownloadeTime) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList();
            
            // added condition for not deleted records 
            //predicates.add(criteriaBuilder.equal(root.get("isDeleted"), Boolean.FALSE));

            if (!StringUtils.isEmpty(imei)) {
                predicates.add(criteriaBuilder.equal(root.get("imei"), imei));
            }

            if (!StringUtils.isEmpty(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (!StringUtils.isEmpty(gatewayUuid)) {
                predicates.add(criteriaBuilder.equal(root.get("uuid"), gatewayUuid));
            }

            if (!StringUtils.isEmpty(accountNumber)) {
                predicates.add(criteriaBuilder.equal(root.get("organisation").get("accountNumber"), accountNumber));
            }
            if (!StringUtils.isEmpty(mac)) {
                predicates.add(criteriaBuilder.equal(root.get("macAddress"), mac));
            }
            if (!StringUtils.isEmpty(type)) {
                predicates.add(criteriaBuilder.equal(root.get("iotType"), type));
            }
            
            if(lastDownloadeTime != null) {
            	 predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeOfLastDownload"), lastDownloadeTime));
            }
            

            return query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0]))).getRestriction();
        };
    }


}
