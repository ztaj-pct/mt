 package com.pct.device.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pct.common.controller.IApplicationController;
import com.pct.common.model.Organisation;
import com.pct.common.payload.AttributeValueRequest;
import com.pct.device.dto.AttributeValueResponseDTO;
import com.pct.device.dto.MessageDTO;
import com.pct.device.service.impl.AttributeValueServiceImpl;

@RestController
@RequestMapping("/attribute-value")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttributeValueController implements IApplicationController<Organisation> {
	
	private static final Logger logger = LoggerFactory.getLogger(AttributeValueController.class);

    @Autowired
    private AttributeValueServiceImpl attributeValueService;

    @Autowired
    private MessageSource responseMessageSource;
    
    @PostMapping("/add-attribute-value")
    public ResponseEntity<MessageDTO<String>> addAttributeValue(@RequestBody AttributeValueRequest attributeValueRequest) {
        logger.info("Inside  addAttributeValue method");
        try {
            if(attributeValueRequest != null) {
                attributeValueService.addAttributeValue(attributeValueRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>(e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<MessageDTO<String>>(new MessageDTO<String>("Attribute value added successfully", "", true),
                HttpStatus.CREATED);
    }
    
    @GetMapping("/get-attribute-value")
   	public ResponseEntity<MessageDTO<List<AttributeValueResponseDTO>>> getAllInstallatioDetails(@RequestParam(value = "deviceId", required = true) String deviceId,
   											  HttpServletRequest httpServletRequest) {
     try {
    	 List<AttributeValueResponseDTO> details = attributeValueService.getAttributeByGatewayDeviceId(deviceId);
        return new ResponseEntity<MessageDTO<List<AttributeValueResponseDTO>>>(
				new MessageDTO<List<AttributeValueResponseDTO>>("attribute value list fetched", details), HttpStatus.OK);
     } catch (Exception e) {
         logger.error("Exception occurred while getting attribute values", e);
         return new ResponseEntity<MessageDTO<List<AttributeValueResponseDTO>>>(new MessageDTO<List<AttributeValueResponseDTO>>(e.getMessage()),
                 HttpStatus.INTERNAL_SERVER_ERROR);
     }
   	}
/*

     @GetMapping(value = "/{uuid}")
     public ResponseEntity<AttributeResponse> getAttibuteByUuid(@Validated @PathVariable("uuid") String attributId) {
         attributeControllerLogger.info("Inside product getAttibuteByUuid method");
        try{
              AttributeResponse attributeResponse = attributeService.getAttibuteByUuid(attributId);
            return new ResponseEntity<>(attributeResponse, HttpStatus.OK);
        }catch (Exception e){
             attributeControllerLogger.error("Exception while getting Attribute for uuid {}", attributId, e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping()
    public ResponseEntity<MessageDTO<String>> updateAttribute(@Validated @RequestBody AttributeRequest attributeRequest) {
        attributeControllerLogger.info("Inside updateAttribute method");
        try {
           attributeService.update(attributeRequest);
        }
         catch (Exception e) {
            throw new RuntimeException("Failed while updating Attribute");
        }
        return new ResponseEntity<MessageDTO<String>>(
                new MessageDTO<String>("Attribute Updated successfully", "", true), HttpStatus.OK);

    }

    //@PostMapping("/getAll")
    public ResponseEntity<Object> getAllAttributes(@RequestParam(value = "_page", required = false) Integer page,
                                              @RequestParam(value = "_limit", required = false) Integer pageSize,
                                               @RequestParam(value = "_sort", required = false) String sort,
                                              @RequestParam(value = "_order", required = false) String order
                                              ) {
        attributeControllerLogger.info("Inside getAllAttributes");
         try {

             MessageDTO<Object> messageDto = new MessageDTO<>("Attributes Fetched Successfully", true);
             Page<AttributeResponse> packageResponse = attributeService.getAllAttributes(getPageable(page - 1, pageSize, sort, order));

             messageDto.setBody(packageResponse.getContent());
             messageDto.setTotalKey(packageResponse.getTotalElements());
             messageDto.setCurrentPage(packageResponse.getNumber());
             messageDto.setTotal_pages(packageResponse.getTotalPages());

             return new ResponseEntity(messageDto, HttpStatus.OK);

         } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(new ResponseBodyDTO(false, "Exception occurred while fetching attributes"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/getAll")
    public ResponseEntity<MessageDTO<List<AttributeResponse>>> getCustomerCompanies() {
        logger.info("Inside getAllActiveCompanies Method");
        try {
            List<AttributeResponse> attributeResponseList = attributeService.getAllAttributeList();
            return new ResponseEntity<MessageDTO<List<AttributeResponse>>>(
                    new MessageDTO<List<AttributeResponse>>("Fetched all active companies", attributeResponseList), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Failed while getting All active Companies");
        }

    }
*/
    
    

}
