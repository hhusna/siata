package com.siata.client.service;

import com.siata.client.dto.AssetDto;

public class DataServiceAPI {

    public void processAsetData(AssetDto[] asset) {
        System.out.println("[Middleware] Data valid. Menyimpan asset ke DB/Cache: ");
    }
}
