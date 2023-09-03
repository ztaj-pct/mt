package com.pct.device.service.impl;

import com.pct.common.constant.AssetStatus;
import com.pct.common.model.Asset;
import com.pct.device.dto.AssetDTO;
import com.pct.device.repository.IAssetRepository;
import com.pct.device.util.BeanConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AssetServiceTest {
    @Mock
    IAssetRepository iAssetRepository;
    @InjectMocks
    private AssetServiceImpl assetServiceImpl;
    @Mock
    private BeanConverter convertor;

    @Test
    public void getAssetByIdOrVinNumberTest() {
        Asset asset = new Asset();
        asset.setAssignedName("12345");
        asset.setVin("12345");
        asset.setStatus(AssetStatus.getAssetStatus("Approved"));
        AssetDTO assetDto = new AssetDTO();
        assetDto.setVin("12345");
        when(convertor.convertAssetToAssetsDto(asset)).thenReturn(assetDto);
        when(iAssetRepository.findAssetByAssetId(asset.getAssignedName(), "123")).thenReturn(asset);
        when(iAssetRepository.findAssetByVinNumber(asset.getVin(), "123")).thenReturn(asset);
        List<AssetDTO> serviceList = assetServiceImpl.getAssetByIdOrVinNumber(asset.getVin(), asset.getAssignedName(), "123");
    }
}
