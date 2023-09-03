package com.pct.device.payload;

import com.pct.device.repository.projections.GatewayIdAndImeiView;

import java.util.List;

public class FetchGatewayImeiResponse {

    private List<GatewayIdAndImeiView> gatewayImeiList;

    public List<GatewayIdAndImeiView> getGatewayImeiList() {
        return gatewayImeiList;
    }

    public void setGatewayImeiList(List<GatewayIdAndImeiView> gatewayImeiList) {
        this.gatewayImeiList = gatewayImeiList;
    }

}
