package com.example.handmademarket.service;

import com.example.handmademarket.dto.CustomRequest;
import com.example.handmademarket.util.ResponseResult;

public interface CustomService {

    /** 提交定制需求（需求方） */
    ResponseResult submitCustom(String username, CustomRequest request);

    /** 查看我提交的定制需求列表（需求方） */
    ResponseResult getMyCustoms(String username);

    /** 取消定制需求（需求方） */
    ResponseResult cancelCustom(String username, Integer customId);

    /** 确认定制完成（需求方，接单方交付后） */
    ResponseResult confirmCustom(String username, Integer customId, String deliveryAddress);

    /** 查看可接单的定制需求（接单方） */
    ResponseResult getAvailableCustoms(String username);

    /** 查看我接的定制单（接单方） */
    ResponseResult getMyAcceptedCustoms(String username);

    /** 接受定制需求（接单方，含报价） */
    ResponseResult acceptCustom(String username, Integer customId, java.math.BigDecimal quotedPrice);

    /** 拒绝定制需求（接单方） */
    ResponseResult rejectCustom(String username, Integer customId, String reason);

    /** 交付定制作品（接单方） */
    ResponseResult deliverCustom(String username, Integer customId, String deliverContent);

    /** 查看定制需求详情 */
    ResponseResult getCustomDetail(Integer customId);

    /** 管理员：查看所有定制需求 */
    ResponseResult getAllCustoms();
}
