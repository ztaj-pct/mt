package com.pct.device.controller;

import com.pct.device.payload.FetchGatewayImeiResponse;
import com.pct.device.repository.IGatewayRepository;
import com.pct.device.repository.projections.GatewayIdAndImeiView;
import com.pct.device.service.IGatewayService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewayControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @MockBean
    private IGatewayService gatewayService;

    @Mock
    private IGatewayRepository iGatewayRepository;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).dispatchOptions(true).build();

    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void getGatewayImeiResponseTest() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).dispatchOptions(true).build();

        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        FetchGatewayImeiResponse res = new FetchGatewayImeiResponse();
        GatewayIdAndImeiView projection = factory.createProjection(GatewayIdAndImeiView.class);
        projection.setImei(1234l);
        projection.setGatewayId(1l);
        List<GatewayIdAndImeiView> gatewayImeiList = new ArrayList<GatewayIdAndImeiView>();
        gatewayImeiList.add(projection);
        res.setGatewayImeiList(gatewayImeiList);
        String URI = "/gateway?imei=" + projection.getImei();
        Mockito.when(gatewayService.getGatewayImeiResponse(Mockito.anyLong())).thenReturn(res);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URI).accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }

}