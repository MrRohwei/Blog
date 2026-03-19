package com.voidpen.server.module.advertisement.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.advertisement.model.request.AdvertisementQueryRequest;
import com.voidpen.server.module.advertisement.model.request.CreateAdvertisementRequest;
import com.voidpen.server.module.advertisement.model.request.UpdateAdvertisementRequest;
import com.voidpen.server.module.advertisement.model.request.UpdateAdvertisementStatusRequest;
import com.voidpen.server.module.advertisement.model.response.AdvertisementVO;
import java.util.List;

public interface AdvertisementService {

    List<AdvertisementVO> listPublicAdvertisements(String position);

    PageResult<AdvertisementVO> listAdminAdvertisements(AdvertisementQueryRequest request);

    void createAdvertisement(CreateAdvertisementRequest request);

    void updateAdvertisement(Long id, UpdateAdvertisementRequest request);

    void deleteAdvertisement(Long id);

    void updateAdvertisementStatus(Long id, UpdateAdvertisementStatusRequest request);
}
