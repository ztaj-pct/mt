package com.pct.device.version.util;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pct.device.version.model.CampaignGatewayDetails;
import com.pct.device.version.payload.CampaignDeviceDetail;
import com.pct.device.version.payload.DeviceStepStatus;

public class CampaignExcelExporter {
	Logger logger = LoggerFactory.getLogger(CampaignExcelExporter.class);
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<CampaignGatewayDetails> listUsers;
    private List<CampaignDeviceDetail> campaignDeviceDetailList;
    
    AtomicInteger atomicIntegerHeader=new AtomicInteger(0);
    AtomicInteger atomicIntegerStep=new AtomicInteger(0);
    AtomicInteger atomicIntegerSNO=new AtomicInteger(1);
    public CampaignExcelExporter(List<CampaignGatewayDetails> listUsers) {
        this.listUsers = listUsers;
        workbook = new XSSFWorkbook();
    }
    public CampaignExcelExporter(List<CampaignDeviceDetail> campaignDeviceDetailList, int value) {
        this.campaignDeviceDetailList = campaignDeviceDetailList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("gatewayList");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        DataFormat fmt = workbook.createDataFormat();
		style.setDataFormat(fmt.getFormat("@"));
        createCell(row, atomicIntegerHeader.getAndIncrement(), "S.NO", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "IMEI", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "Customer", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "Status", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "Comments", style);
        List<List<DeviceStepStatus>> details = listUsers.stream().map(x -> x.getDeviceStepStatus()).collect(Collectors.toList());
        Integer maxSize=details.stream().max(Comparator.comparing(List::size)).get().size();
        for(int i=0;i<maxSize;i++){
            createCell(row, atomicIntegerHeader.getAndIncrement(), "Step "+atomicIntegerStep.incrementAndGet()+" (Time in UTC)", style);
        }

    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        DataFormat fmt = workbook.createDataFormat();
		 style.setDataFormat(fmt.getFormat("@"));
        for (List<CampaignGatewayDetails> batch : Lists.partition(listUsers,50)) {
            rowCount = writeDataInCellsForBatch(rowCount, style, batch);
        }

    }

    private int writeDataInCellsForBatch(int rowCount, CellStyle style, List<CampaignGatewayDetails> batch) {
        for(CampaignGatewayDetails user: batch){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, atomicIntegerSNO.getAndIncrement(), style);
            createCell(row, columnCount++, user.getImei(), style);
            createCell(row, columnCount++, user.getCustomerName(), style);
            createCell(row, columnCount++, user.getDeviceStatusForCampaign(), style);
            createCell(row, columnCount++, user.getComments(), style);
            if(user.getDeviceStepStatus().size()>0){
                for(DeviceStepStatus stepStatus: user.getDeviceStepStatus()){
                    createCell(row, columnCount++, stepStatus.getStepStatus(), style);
                }
            }
        }
        return rowCount;
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();

    }
    public void exportGateways(HttpServletResponse response,String msgUuid) throws IOException {
		logger.info("exportGateways  msgUuid "+msgUuid);

    	writeHeaderLineGateways(msgUuid);
    	writeDataLinesGateways(msgUuid);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();

    }
    private void writeHeaderLineGateways(String msgUuid) {
		logger.info("writeHeaderLineGateways  msgUuid "+msgUuid);

        sheet = workbook.createSheet("gatewayList");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        DataFormat fmt = workbook.createDataFormat();
		style.setDataFormat(fmt.getFormat("@"));
        createCell(row, atomicIntegerHeader.getAndIncrement(), "S.NO", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "IMEI", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "Customer", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "Status", style);
        createCell(row, atomicIntegerHeader.getAndIncrement(), "Comments", style);
        List<List<DeviceStepStatus>> details = campaignDeviceDetailList.stream().map(x -> x.getDeviceStepStatus()).collect(Collectors.toList());
        Integer maxSize=details.stream().max(Comparator.comparing(List::size)).get().size();
        for(int i=0;i<maxSize;i++){
            createCell(row, atomicIntegerHeader.getAndIncrement(), "Step "+atomicIntegerStep.incrementAndGet()+" (Time in UTC)", style);
        }
		logger.info("executed writeHeaderLineGateways  msgUuid "+msgUuid);


    }
    private void writeDataLinesGateways(String msgUuid) {
		logger.info("writeDataLinesGateways  msgUuid "+msgUuid);

        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        DataFormat fmt = workbook.createDataFormat();
		 style.setDataFormat(fmt.getFormat("@"));
        for (List<CampaignDeviceDetail> batch : Lists.partition(campaignDeviceDetailList,50)) {
            rowCount = writeDataInCellsForBatchGateways(rowCount, style, batch);
        }
		logger.info("executed writeDataLinesGateways  msgUuid "+msgUuid);

    }
    private int writeDataInCellsForBatchGateways(int rowCount, CellStyle style, List<CampaignDeviceDetail> batch) {
        for(CampaignDeviceDetail user: batch){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, atomicIntegerSNO.getAndIncrement(), style);
            createCell(row, columnCount++, user.getImei(), style);
            createCell(row, columnCount++, user.getCustomerName(), style);
            createCell(row, columnCount++, user.getDeviceStatusForCampaign(), style);
            createCell(row, columnCount++, user.getComments(), style);
            if(user.getDeviceStepStatus().size()>0){
                for(DeviceStepStatus stepStatus: user.getDeviceStepStatus()){
                    createCell(row, columnCount++, stepStatus.getStepStatus(), style);
                }
            }
        }
        return rowCount;
    }
}
