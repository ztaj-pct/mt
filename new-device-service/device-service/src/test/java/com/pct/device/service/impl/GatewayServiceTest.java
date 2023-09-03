package com.pct.device.service.impl;

import com.pct.device.payload.FetchGatewayImeiResponse;
import com.pct.device.repository.IGatewayRepository;
import com.pct.device.repository.projections.GatewayIdAndImeiView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewayServiceTest {
    @Mock
    IGatewayRepository iGatewayRepository;
    @InjectMocks
    private GatewayServiceImpl gatewayServiceImpl;

    @Test
    public void getGatewayImeiResponseTest() {
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        GatewayIdAndImeiView projection = factory.createProjection(GatewayIdAndImeiView.class);
        projection.setImei(1234l);
        projection.setGatewayId(1l);
        List<GatewayIdAndImeiView> gatewayImeiList = new ArrayList<GatewayIdAndImeiView>();
        FetchGatewayImeiResponse res = new FetchGatewayImeiResponse();
        gatewayImeiList.add(projection);
        res.setGatewayImeiList(gatewayImeiList);
        when(iGatewayRepository.findIdAndImei(projection.getImei())).thenReturn(res.getGatewayImeiList());
        FetchGatewayImeiResponse serviceList = gatewayServiceImpl.getGatewayImeiResponse(projection.getImei());
        assertEquals(serviceList.getGatewayImeiList().size(), res.getGatewayImeiList().size());
    }
}
