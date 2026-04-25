package com.example.handmademarket.service;

import com.example.handmademarket.dto.CreateOrderRequest;
import com.example.handmademarket.dto.EvaluationRequest;
import com.example.handmademarket.util.ResponseResult;
import jakarta.servlet.http.HttpServletResponse;

public interface OrderService {

    /** 买家：查看我的订单（支持按状态筛选） */
    ResponseResult getBuyerOrders(String username, Integer status);

    /** 卖家：查看销售订单（支持按状态筛选） */
    ResponseResult getSellerOrders(String username, Integer status);

    /** 买家：重新下单（基于已取消订单） */
    ResponseResult reorder(String username, String orderId);

    /** 创作者：提醒消费者支付 */
    ResponseResult remindPayment(String username, String orderId);

    /** 创作者：上传定制订单实物图 */
    ResponseResult uploadProductImage(String username, String orderId, String imageUrl);

    /** 卖家：销售统计 */
    ResponseResult getSellerStats(String username);

    /** 管理员：查看所有订单 */
    ResponseResult getAllOrders();

    /** 管理员：按条件搜索订单（订单号/用户ID/状态/时间范围/订单类型） */
    ResponseResult searchOrders(String orderNo, Integer buyerId, Integer sellerId,
                                Integer status, String startTime, String endTime, Integer orderType);

    /** 管理员：处理订单纠纷 */
    ResponseResult handleDispute(String orderId, String disputeResult);

    /** 管理员：导出订单数据（Excel） */
    void exportOrders(Integer status, String startTime, String endTime,
                      Integer orderType, HttpServletResponse response);

    /** 管理员：获取订单统计概览 */
    ResponseResult getAdminOrderStats();

    /** 创建订单（从购物车结算） */
    ResponseResult createOrder(String username, CreateOrderRequest request);

    /** 订单详情 */
    ResponseResult getOrderDetail(String username, String orderId);

    /** 买家支付订单（模拟支付：全款/定金） */
    ResponseResult payOrder(String username, String orderId, String payType);

    /** 买家支付定制订单尾款 */
    ResponseResult payBalance(String username, String orderId, String payType);

    /** 创作者申请尾款支付 */
    ResponseResult requestBalancePayment(String username, String orderId);

    /** 卖家发货 */
    ResponseResult shipOrder(String username, String orderId, String logisticsInfo);

    /** 买家确认收货 */
    ResponseResult confirmReceipt(String username, String orderId);

    /** 取消订单 */
    ResponseResult cancelOrder(String username, String orderId, String cancelReason);

    /** 评价订单 */
    ResponseResult evaluateOrder(String username, String orderId, EvaluationRequest request);

    /** 买家申请维权 */
    ResponseResult applyDispute(String username, String orderId, String reason);

    /** 统一交易管理：获取用户所有订单（买家+卖家，支持按角色和状态筛选） */
    ResponseResult getAllUserOrders(String username, Integer status, String role);

    /** 统一交易管理：获取交易统计（买+卖） */
    ResponseResult getTradeStats(String username);
}
