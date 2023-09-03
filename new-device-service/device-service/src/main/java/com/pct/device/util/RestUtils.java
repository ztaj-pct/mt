package com.pct.device.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.pct.common.dto.ResponseBodyDTO;
import com.pct.common.dto.ResponseDTO;
import com.pct.common.exception.InterServiceRestException;
import com.pct.common.model.Company;
import com.pct.common.model.InstallHistory;
import com.pct.common.model.User;
import com.pct.common.payload.GetInstallHistoryByAssetUuids;
import com.pct.device.bean.ShippedDevicesHubRequest;
import com.pct.device.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Abhishek on 18/05/20
 */

@Component
public class RestUtils {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private EurekaClient eurekaClient;
    @Value("${service.gateway.serviceId}")
    private String gatewayServiceId;
    @Value("${hub.endpoint.shipment}")
    private String hubShipmentEndpoint;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public User getUserFromAuthService(Long userId) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/core/" + userId;
        ResponseEntity<MessageDTO> userResponseEntity = restTemplate.getForEntity(url, MessageDTO.class);
        if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            User user = objectMapper.convertValue(userResponseEntity.getBody().getBody(), User.class);
            return user;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", userResponseEntity.getStatusCode());
        }
    }

    public List<String> getSuperAdminUser() {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/user/core/adminUser" ;
        ResponseEntity<MessageDTO> userResponseEntity = restTemplate.getForEntity(url, MessageDTO.class);
        if (userResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            List<String> user = (List<String>) objectMapper.convertValue(userResponseEntity.getBody().getBody(), List.class);
            return user;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", userResponseEntity.getStatusCode());
        }
    }
    public Company getCompanyFromCompanyService(String accountNumber) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/" + accountNumber;
        ResponseEntity<Company> companyResponseEntity = restTemplate.getForEntity(url, Company.class);
        if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Company company = companyResponseEntity.getBody();
            return company;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", companyResponseEntity.getStatusCode());
        }
    }

    public Company getCompanyFromCompanyServiceById(Long id) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/customer/core/id/" + id;
        ResponseEntity<Company> companyResponseEntity = restTemplate.getForEntity(url, Company.class);
        if (companyResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Company company = companyResponseEntity.getBody();
            return company;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", companyResponseEntity.getStatusCode());
        }
    }

    public Boolean deleteInstallDataForCompany(String companyUuid) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/installation/core/reset-company?company_uuid=" + companyUuid;
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.postForEntity(url, null, ResponseDTO.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Boolean status = responseEntity.getBody().getStatus();
            return status;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        }
    }

    public Boolean deleteInstallDataForGateway(String gatewayUuid) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/installation/core/reset-gateway?gateway_uuid=" + gatewayUuid;
        ResponseEntity<ResponseDTO> responseEntity = restTemplate.postForEntity(url, null, ResponseDTO.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            Boolean status = responseEntity.getBody().getStatus();
            return status;
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        }
    }

    public Boolean sendShippedDevicesToHub(ShippedDevicesHubRequest shippedDevicesHubRequest) {
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(hubShipmentEndpoint, shippedDevicesHubRequest, Object.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        }
        return false;
    }

    public List<InstallHistory> getInstallHistoryListForAssetUuids(List<String> assetUuids,Map<String, String> filterValues) {
        Application application = eurekaClient.getApplication(gatewayServiceId);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        GetInstallHistoryByAssetUuids request = new GetInstallHistoryByAssetUuids();
        request.setAssetUuids(assetUuids);
        request.setFilterValues(filterValues);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/installation/core/asset";
        ResponseEntity responseEntity = restTemplate.postForEntity(url, request, List.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return (List<InstallHistory>) ((List) responseEntity.getBody()).stream().map(ih ->
                    objectMapper.convertValue(ih, InstallHistory.class)).collect(Collectors.toList());
        } else {
            throw new InterServiceRestException
                    ("Http call not successful", responseEntity.getStatusCode());
        }
    }
}
