package com.pct.device.util;

import com.opencsv.CSVWriter;
import com.pct.device.dto.AssetResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;

public class WriteCsvToResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteCsvToResponse.class);

    private static final String[] DEVICE_MGMT_CSV_HEADER = { "Device id" };
    
    
    
    public static void writeCommunicationsToCsvUsingStringArray(PrintWriter writer, List<AssetResponseDTO> list) {
    	String[] CSV_HEADER = {"Asset Id", "Asset Nickname" ,"Product Approved", "VIN", "Asset Type 1", "Tires","Axles","Lenght","Door" ,"Model Year", "Manufacturer", "Status", "Added", "Updated","Comment"};	
        try (
        		CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ){
          csvWriter.writeNext(CSV_HEADER);
     
          for (AssetResponseDTO asset : list) {
            String[] data = {
            		asset.getAssignedName(),
            		asset.getAssetNickName(),
            		asset.getEligibleGateway(),
            		asset.getVin(),
            		asset.getCategory(),
            		asset.getNoOfTires(),
            		asset.getNoOfAxel(),
            		asset.getExternalLength(),
            		asset.getDoorType(),
            		asset.getManufacturerDetails().getYear(),
            		asset.getManufacturerDetails().getMake(),
            		asset.getStatus(),
            		asset.getDatetimeCreated(),
            		asset.getDatetimeUpdated()
            };
            
            csvWriter.writeNext(data);
          }
          System.out.println("Write CSV using CSVWriter successfully!");
          csvWriter.close();
        }catch (Exception e) {
          System.out.println("Writing CSV error!");
          e.printStackTrace();
        }
      }
    
}
