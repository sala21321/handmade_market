package com.example.handmademarket.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.handmademarket.dto.CreateOrderRequest;
import com.example.handmademarket.dto.EvaluationRequest;
import com.example.handmademarket.entity.Evaluation;
import com.example.handmademarket.entity.Goods;
import com.example.handmademarket.entity.Order;
import com.example.handmademarket.entity.OrderGoods;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.EvaluationRepository;
import com.example.handmademarket.repository.GoodsRepository;
import com.example.handmademarket.repository.OrderGoodsRepository;
import com.example.handmademarket.repository.OrderRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.OrderService;
import com.example.handmademarket.util.ResponseResult;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderGoodsRepository orderGoodsRepository;
    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderGoodsRepository orderGoodsRepository,
                            GoodsRepository goodsRepository,
                            UserRepository userRepository,
                            EvaluationRepository evaluationRepository) {
        this.orderRepository = orderRepository;
        this.orderGoodsRepository = orderGoodsRepository;
        this.goodsRepository = goodsRepository;
        this.userRepository = userRepository;
        this.evaluationRepository = evaluationRepository;
    }

    // 状态映射：DB integer -> 前端 string
    private String statusToString(Integer status) {
        if (status == null) return "pending";
        return switch (status) {
            case 0 -> "pending";
            case 1 -> "paid";
            case 2 -> "paid";     // 待发货也算已支付
            case 3 -> "shipped";
            case 4 -> "completed";
            case 5 -> "cancelled";
            default -> "pending";
        };
    }

    // 支付状态映射
    private String paymentStatusToString(Integer paymentStatus) {
        if (paymentStatus == null) return "unpaid";
        return switch (paymentStatus) {
            case 0 -> "unpaid";           // 未支付
            case 1 -> "paying";           // 支付中
            case 2 -> "paid";             // 支付成功（全款/定金）
            case 3 -> "pay_failed";       // 支付失败
            case 4 -> "balance_requested";// 已申请尾款
            case 5 -> "balance_paid";     // 尾款已支付
            default -> "unpaid";
        };
    }

    // 生成订单号：HM + 日期 + 4位随机数
    private String generateOrderId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "HM" + datePart + randomPart;
    }

    // 根据 username 获取 userId
    private User getUserByUsername(String username) {
        return userRepository.findByUserAccount(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public ResponseResult getBuyerOrders(String username, Integer status) {
        User user = getUserByUsername(username);
        Integer userId = user.getUserId().intValue();

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByBuyerIdAndStatusOrderByCreateTimeDesc(userId, status);
        } else {
            orders = orderRepository.findByBuyerIdOrderByCreateTimeDesc(userId);
        }

        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getOrderId());
            map.put("no", order.getOrderId());
            map.put("time", order.getCreateTime() != null
                    ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            map.put("status", statusToString(order.getStatus()));
            map.put("statusText", statusToChinese(order.getStatus()));
            map.put("orderType", order.getOrderType() != null && order.getOrderType() == 2 ? "定制订单" : "普通订单");
            map.put("payType", order.getPayType());
            map.put("paymentStatus", paymentStatusToString(order.getPaymentStatus()));

            // 获取订单商品
            List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
            if (!goodsList.isEmpty()) {
                OrderGoods firstItem = goodsList.get(0);
                map.put("name", firstItem.getGoodsName());
                map.put("quantity", firstItem.getNum());
                map.put("price", firstItem.getPrice());

                // 获取商品图片
                Optional<Goods> goods = goodsRepository.findById(firstItem.getGoodsId().longValue());
                map.put("image", goods.map(Goods::getImageUrl).orElse(""));
            } else {
                map.put("name", "");
                map.put("quantity", 0);
                map.put("price", BigDecimal.ZERO);
                map.put("image", "");
            }

            map.put("total", order.getAmount());
            map.put("deposit", order.getDeposit());
            map.put("balance", order.getBalance());
            map.put("address", order.getDeliveryAddress());
            map.put("logisticsInfo", order.getLogisticsInfo());
            map.put("cancelReason", order.getCancelReason());
            map.put("disputeStatus", disputeStatusToChinese(order.getDisputeStatus()));
            map.put("disputeResult", order.getDisputeResult());
            // 检查是否已评价
            map.put("commented", evaluationRepository.existsByOrderId(order.getOrderId()));

            // 卖家信息
            if (order.getSellerId() != null) {
                userRepository.findById(order.getSellerId().longValue())
                        .ifPresent(seller -> {
                            map.put("sellerName", seller.getUserName() != null ? seller.getUserName() : seller.getUserAccount());
                        });
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult getSellerOrders(String username, Integer status) {
        User user = getUserByUsername(username);
        Integer userId = user.getUserId().intValue();

        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findBySellerIdAndStatusOrderByCreateTimeDesc(userId, status);
        } else {
            orders = orderRepository.findBySellerIdOrderByCreateTimeDesc(userId);
        }

        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getOrderId());
            map.put("no", order.getOrderId());
            map.put("status", statusToString(order.getStatus()));
            map.put("statusText", statusToChinese(order.getStatus()));
            map.put("orderType", order.getOrderType() != null && order.getOrderType() == 2 ? "定制订单" : "普通订单");
            map.put("amount", order.getAmount());
            map.put("deposit", order.getDeposit());
            map.put("balance", order.getBalance());
            map.put("payType", order.getPayType());
            map.put("paymentStatus", paymentStatusToString(order.getPaymentStatus()));
            map.put("time", order.getCreateTime() != null
                    ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            map.put("payTime", order.getPayTime() != null
                    ? order.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            map.put("address", order.getDeliveryAddress());
            map.put("logisticsInfo", order.getLogisticsInfo());
            map.put("cancelReason", order.getCancelReason());
            map.put("remark", order.getRemark());

            // 获取买家信息
            if (order.getBuyerId() != null) {
                userRepository.findById(order.getBuyerId().longValue())
                        .ifPresent(buyer -> {
                            map.put("buyer", buyer.getUserName() != null ? buyer.getUserName() : buyer.getUserAccount());
                            map.put("buyerPhone", buyer.getPhone());
                        });
            }

            // 获取订单商品
            List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
            if (!goodsList.isEmpty()) {
                OrderGoods firstItem = goodsList.get(0);
                map.put("name", firstItem.getGoodsName());
                map.put("quantity", firstItem.getNum());
                map.put("price", firstItem.getPrice());
                Optional<Goods> goods = goodsRepository.findById(firstItem.getGoodsId().longValue());
                map.put("image", goods.map(Goods::getImageUrl).orElse(""));
            } else {
                map.put("name", "");
                map.put("quantity", 0);
                map.put("price", BigDecimal.ZERO);
                map.put("image", "");
            }

            // 评价信息
            map.put("commented", evaluationRepository.existsByOrderId(order.getOrderId()));

            return map;
        }).collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult getSellerStats(String username) {
        User user = getUserByUsername(username);
        Integer sellerId = user.getUserId().intValue();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSales", orderRepository.sumAmountBySellerId(sellerId));
        stats.put("totalOrders", orderRepository.countBySellerId(sellerId));
        stats.put("pendingOrders", orderRepository.countBySellerIdAndStatus(sellerId, 0)
                + orderRepository.countBySellerIdAndStatus(sellerId, 1));
        // 在售商品数量
        long goodsCount = goodsRepository.countByCreatorId(sellerId.longValue());
        stats.put("totalGoods", goodsCount);

        return ResponseResult.ok(stats);
    }

    @Override
    public ResponseResult getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreateTimeDesc();
        List<Map<String, Object>> result = orders.stream().map(this::buildAdminOrderMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    @Override
    @Transactional
    public ResponseResult createOrder(String username, CreateOrderRequest request) {
        User buyer = getUserByUsername(username);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseResult.fail("订单商品不能为空");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            return ResponseResult.fail("收货地址不能为空");
        }

        // 按卖家分组创建订单（不同卖家的商品拆分为不同订单）
        // 先获取所有商品信息
        Map<Integer, List<CreateOrderRequest.OrderItemDTO>> sellerItemsMap = new LinkedHashMap<>();
        Map<Integer, Goods> goodsMap = new HashMap<>();

        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            // 使用悲观锁查询，防止并发超卖
            Goods goods = goodsRepository.findByIdForUpdate(item.getGoodsId().longValue())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + item.getGoodsId()));
            goodsMap.put(item.getGoodsId(), goods);

            // 校验库存
            int stock = goods.getStock() != null ? goods.getStock() : 0;
            if (stock < item.getQuantity()) {
                return ResponseResult.fail("商品【" + goods.getTitle() + "】库存不足，当前库存: " + stock);
            }

            Integer sellerId = goods.getCreatorId() != null ? goods.getCreatorId().intValue() : 0;
            sellerItemsMap.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(item);
        }

        List<String> orderIds = new ArrayList<>();

        for (Map.Entry<Integer, List<CreateOrderRequest.OrderItemDTO>> entry : sellerItemsMap.entrySet()) {
            Integer sellerId = entry.getKey();
            List<CreateOrderRequest.OrderItemDTO> items = entry.getValue();

            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CreateOrderRequest.OrderItemDTO item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());
                BigDecimal price = goods.getPrice() != null ? BigDecimal.valueOf(goods.getPrice()) : BigDecimal.ZERO;
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // 创建订单
            Order order = new Order();
            String orderId = generateOrderId();
            order.setOrderId(orderId);
            order.setBuyerId(buyer.getUserId().intValue());
            order.setSellerId(sellerId);
            order.setOrderType(1); // 普通订单
            order.setAmount(totalAmount);
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setPayType(request.getPayType());
            order.setRemark(request.getRemark());
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(0); // 待支付

            orderRepository.save(order);

            // 创建订单商品并扣减库存
            for (CreateOrderRequest.OrderItemDTO item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());
                OrderGoods og = new OrderGoods();
                og.setOrderId(orderId);
                og.setGoodsId(item.getGoodsId());
                og.setGoodsName(goods.getTitle());
                og.setPrice(goods.getPrice() != null ? BigDecimal.valueOf(goods.getPrice()) : BigDecimal.ZERO);
                og.setNum(item.getQuantity());
                og.setCreateTime(LocalDateTime.now());
                orderGoodsRepository.save(og);

                // 扣减库存
                int currentStock = goods.getStock() != null ? goods.getStock() : 0;
                goods.setStock(currentStock - item.getQuantity());
                goodsRepository.save(goods);
            }

            orderIds.add(orderId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderIds", orderIds);
        data.put("message", "订单创建成功");
        return ResponseResult.ok(data);
    }

    @Override
    public ResponseResult getOrderDetail(String username, String orderId) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 权限校验：只有买家、卖家可以查看订单详情
        Integer userId = user.getUserId().intValue();
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return ResponseResult.fail("无权查看此订单");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getOrderId());
        map.put("no", order.getOrderId());
        map.put("status", statusToString(order.getStatus()));
        map.put("orderType", order.getOrderType() != null && order.getOrderType() == 2 ? "定制订单" : "普通订单");
        map.put("amount", order.getAmount());
        map.put("deposit", order.getDeposit());
        map.put("balance", order.getBalance());
        map.put("payType", order.getPayType());
        map.put("paymentStatus", paymentStatusToString(order.getPaymentStatus()));
        map.put("address", order.getDeliveryAddress());
        map.put("createTime", order.getCreateTime() != null
                ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        map.put("payTime", order.getPayTime() != null
                ? order.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        map.put("deliveryTime", order.getDeliveryTime() != null
                ? order.getDeliveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        map.put("receiveTime", order.getReceiveTime() != null
                ? order.getReceiveTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        map.put("logisticsInfo", order.getLogisticsInfo());
        map.put("cancelReason", order.getCancelReason());
        map.put("remark", order.getRemark());

        // 买家信息
        if (order.getBuyerId() != null) {
            userRepository.findById(order.getBuyerId().longValue())
                    .ifPresent(buyer -> {
                        map.put("buyerName", buyer.getUserName() != null ? buyer.getUserName() : buyer.getUserAccount());
                        map.put("buyerPhone", buyer.getPhone());
                    });
        }
        // 卖家信息
        if (order.getSellerId() != null) {
            userRepository.findById(order.getSellerId().longValue())
                    .ifPresent(seller -> {
                        map.put("sellerName", seller.getUserName() != null ? seller.getUserName() : seller.getUserAccount());
                        map.put("sellerPhone", seller.getPhone());
                    });
        }

        // 订单商品
        List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(orderId);
        List<Map<String, Object>> items = goodsList.stream().map(og -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("goodsId", og.getGoodsId());
            itemMap.put("name", og.getGoodsName());
            itemMap.put("price", og.getPrice());
            itemMap.put("quantity", og.getNum());
            Optional<Goods> goods = goodsRepository.findById(og.getGoodsId().longValue());
            itemMap.put("image", goods.map(Goods::getImageUrl).orElse(""));
            return itemMap;
        }).collect(Collectors.toList());
        map.put("items", items);

        return ResponseResult.ok(map);
    }

    @Override
    @Transactional
    public ResponseResult payOrder(String username, String orderId, String payType) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return ResponseResult.fail("订单状态不允许支付");
        }

        // 验证支付方式
        if (payType == null || payType.isBlank()) {
            payType = "模拟支付宝"; // 默认
        }
        if (!"模拟支付宝".equals(payType) && !"模拟微信支付".equals(payType)) {
            return ResponseResult.fail("不支持的支付方式，仅支持模拟支付宝或模拟微信支付");
        }

        // 判断是否为定制订单
        BigDecimal payAmount;
        if (order.getOrderType() != null && order.getOrderType() == 2) {
            // 定制订单：先支付定金（≥总额30%）
            BigDecimal deposit = order.getDeposit();
            if (deposit == null || deposit.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseResult.fail("定制订单定金未设置，请联系创作者");
            }
            // 验证定金比例 ≥ 30%
            BigDecimal minDeposit = order.getAmount()
                    .multiply(new BigDecimal("0.3"))
                    .setScale(2, java.math.RoundingMode.CEILING);
            if (deposit.compareTo(minDeposit) < 0) {
                return ResponseResult.fail("定制定金不得低于总金额的30%，请重新设置");
            }
            payAmount = deposit;
            // 计算尾款
            BigDecimal balance = order.getAmount().subtract(deposit);
            order.setBalance(balance);
            order.setPaymentStatus(2); // 定金支付成功
        } else {
            // 普通订单：支付全款
            payAmount = order.getAmount();
            order.setPaymentStatus(2); // 支付成功
        }

        order.setStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        order.setPayType(payType);
        orderRepository.save(order);

        // 返回支付结果详情
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("payAmount", payAmount);
        data.put("payType", payType);
        data.put("payTime", order.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (order.getOrderType() != null && order.getOrderType() == 2) {
            data.put("orderType", "定制订单");
            data.put("deposit", order.getDeposit());
            data.put("balance", order.getBalance());
            data.put("message", "定金支付成功，待创作者完成作品后支付尾款");
        } else {
            data.put("orderType", "普通订单");
            data.put("message", "支付成功，订单已确认");
        }
        return ResponseResult.ok("支付成功", data);
    }

    @Override
    @Transactional
    public ResponseResult payBalance(String username, String orderId, String payType) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        // 定制订单才有尾款
        if (order.getOrderType() == null || order.getOrderType() != 2) {
            return ResponseResult.fail("仅定制订单支持尾款支付");
        }
        // 必须已支付定金（status=1）且创作者已申请尾款（paymentStatus=4）
        if (order.getStatus() != 1 || order.getPaymentStatus() == null || order.getPaymentStatus() != 4) {
            return ResponseResult.fail("当前订单不可支付尾款，请等待创作者完成作品并申请尾款");
        }

        BigDecimal balance = order.getBalance();
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseResult.fail("尾款金额异常");
        }

        // 验证支付方式
        if (payType == null || payType.isBlank()) {
            payType = "模拟支付宝";
        }
        if (!"模拟支付宝".equals(payType) && !"模拟微信支付".equals(payType)) {
            return ResponseResult.fail("不支持的支付方式，仅支持模拟支付宝或模拟微信支付");
        }

        order.setPaymentStatus(5); // 尾款已支付
        order.setStatus(2);        // 待发货
        order.setPayType(payType);
        orderRepository.save(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("payAmount", balance);
        data.put("payType", payType);
        data.put("totalAmount", order.getAmount());
        data.put("message", "尾款支付成功，等待创作者发货");
        return ResponseResult.ok("尾款支付成功", data);
    }

    @Override
    @Transactional
    public ResponseResult requestBalancePayment(String username, String orderId) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getSellerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getOrderType() == null || order.getOrderType() != 2) {
            return ResponseResult.fail("仅定制订单支持申请尾款");
        }
        // 必须已支付定金
        if (order.getStatus() != 1) {
            return ResponseResult.fail("订单状态不允许申请尾款");
        }
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 4) {
            return ResponseResult.fail("已申请尾款支付，请等待消费者支付");
        }

        order.setPaymentStatus(4); // 标记为：已申请尾款
        orderRepository.save(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("balance", order.getBalance());
        data.put("message", "已向消费者发送尾款支付通知");
        return ResponseResult.ok("尾款申请成功", data);
    }

    @Override
    @Transactional
    public ResponseResult reorder(String username, String orderId) {
        User user = getUserByUsername(username);
        Order oldOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!oldOrder.getBuyerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (oldOrder.getStatus() != 5) {
            return ResponseResult.fail("只有已取消的订单可以重新下单");
        }

        // 获取原订单商品
        List<OrderGoods> oldGoodsList = orderGoodsRepository.findByOrderId(orderId);
        if (oldGoodsList.isEmpty()) {
            return ResponseResult.fail("原订单商品信息不存在");
        }

        // 检查商品是否仍然可购买，并校验库存（悲观锁防止并发超卖）
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, Goods> lockedGoodsMap = new HashMap<>();
        for (OrderGoods og : oldGoodsList) {
            Optional<Goods> goodsOpt = goodsRepository.findByIdForUpdate(og.getGoodsId().longValue());
            if (goodsOpt.isEmpty()) {
                return ResponseResult.fail("商品\"" + og.getGoodsName() + "\"已下架，无法重新下单");
            }
            Goods goods = goodsOpt.get();
            int stock = goods.getStock() != null ? goods.getStock() : 0;
            if (stock < og.getNum()) {
                return ResponseResult.fail("商品【" + goods.getTitle() + "】库存不足，当前库存: " + stock);
            }
            lockedGoodsMap.put(og.getGoodsId(), goods);
        }

        // 创建新订单
        Order newOrder = new Order();
        String newOrderId = generateOrderId();
        newOrder.setOrderId(newOrderId);
        newOrder.setBuyerId(oldOrder.getBuyerId());
        newOrder.setSellerId(oldOrder.getSellerId());
        newOrder.setOrderType(oldOrder.getOrderType());
        newOrder.setDeliveryAddress(oldOrder.getDeliveryAddress());
        newOrder.setRemark(oldOrder.getRemark());
        newOrder.setCreateTime(LocalDateTime.now());
        newOrder.setStatus(0); // 待支付

        // 重新计算金额（以当前价格为准，使用已锁定的商品对象）
        for (OrderGoods og : oldGoodsList) {
            Goods goods = lockedGoodsMap.get(og.getGoodsId());
            BigDecimal price = goods.getPrice() != null ? BigDecimal.valueOf(goods.getPrice()) : og.getPrice();
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(og.getNum())));
        }
        newOrder.setAmount(totalAmount);
        orderRepository.save(newOrder);

        // 复制订单商品并扣减库存（使用已锁定的商品对象）
        for (OrderGoods og : oldGoodsList) {
            Goods goods = lockedGoodsMap.get(og.getGoodsId());
            OrderGoods newOg = new OrderGoods();
            newOg.setOrderId(newOrderId);
            newOg.setGoodsId(og.getGoodsId());
            newOg.setGoodsName(goods.getTitle());
            newOg.setPrice(goods.getPrice() != null ? BigDecimal.valueOf(goods.getPrice()) : og.getPrice());
            newOg.setNum(og.getNum());
            newOg.setCreateTime(LocalDateTime.now());
            orderGoodsRepository.save(newOg);

            // 扣减库存
            int currentStock = goods.getStock() != null ? goods.getStock() : 0;
            goods.setStock(currentStock - og.getNum());
            goodsRepository.save(goods);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", newOrderId);
        data.put("amount", totalAmount);
        data.put("message", "重新下单成功，请尽快支付");
        return ResponseResult.ok("重新下单成功", data);
    }

    @Override
    public ResponseResult remindPayment(String username, String orderId) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getSellerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return ResponseResult.fail("只有待支付订单可以提醒支付");
        }

        // 此处模拟发送提醒通知（实际可对接消息推送、站内信等）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("buyerId", order.getBuyerId());
        data.put("amount", order.getAmount());
        data.put("message", "已向消费者发送支付提醒");
        return ResponseResult.ok("提醒发送成功", data);
    }

    @Override
    @Transactional
    public ResponseResult uploadProductImage(String username, String orderId, String imageUrl) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getSellerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getOrderType() == null || order.getOrderType() != 2) {
            return ResponseResult.fail("仅定制订单支持上传实物图");
        }
        if (order.getStatus() != 1) {
            return ResponseResult.fail("订单状态不允许上传实物图（需已支付定金）");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseResult.fail("图片链接不能为空");
        }

        // 将实物图保存到订单备注中（可扩展为独立字段）
        String existingRemark = order.getRemark() != null ? order.getRemark() : "";
        if (existingRemark.contains("[实物图]")) {
            // 追加图片
            order.setRemark(existingRemark + "," + imageUrl);
        } else {
            String prefix = existingRemark.isEmpty() ? "" : existingRemark + " | ";
            order.setRemark(prefix + "[实物图]" + imageUrl);
        }
        orderRepository.save(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("imageUrl", imageUrl);
        data.put("message", "实物图上传成功");
        return ResponseResult.ok("上传成功", data);
    }

    @Override
    @Transactional
    public ResponseResult shipOrder(String username, String orderId, String logisticsInfo) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getSellerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return ResponseResult.fail("订单状态不允许发货");
        }

        order.setStatus(3); // 已发货
        order.setDeliveryTime(LocalDateTime.now());
        if (logisticsInfo != null && !logisticsInfo.isBlank()) {
            order.setLogisticsInfo(logisticsInfo);
        }
        orderRepository.save(order);

        return ResponseResult.ok("发货成功");
    }

    @Override
    @Transactional
    public ResponseResult confirmReceipt(String username, String orderId) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 3) {
            return ResponseResult.fail("订单状态不允许确认收货");
        }

        order.setStatus(4); // 已完成
        order.setReceiveTime(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseResult.ok("确认收货成功");
    }

    @Override
    @Transactional
    public ResponseResult cancelOrder(String username, String orderId, String cancelReason) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Integer userId = user.getUserId().intValue();
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return ResponseResult.fail("只有待支付订单可以取消");
        }

        order.setStatus(5); // 已取消
        order.setPaymentStatus(0); // 未支付
        if (cancelReason != null && !cancelReason.isBlank()) {
            order.setCancelReason(cancelReason);
        }
        orderRepository.save(order);

        // 恢复库存（悲观锁防止并发问题）
        List<OrderGoods> orderGoodsList = orderGoodsRepository.findByOrderId(orderId);
        for (OrderGoods og : orderGoodsList) {
            goodsRepository.findByIdForUpdate(og.getGoodsId().longValue()).ifPresent(goods -> {
                int currentStock = goods.getStock() != null ? goods.getStock() : 0;
                goods.setStock(currentStock + og.getNum());
                goodsRepository.save(goods);
            });
        }

        return ResponseResult.ok("订单已取消");
    }

    @Override
    @Transactional
    public ResponseResult evaluateOrder(String username, String orderId, EvaluationRequest request) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("只有买家可以评价");
        }
        if (order.getStatus() != 4) {
            return ResponseResult.fail("只有已完成的订单可以评价");
        }
        if (evaluationRepository.existsByOrderId(orderId)) {
            return ResponseResult.fail("该订单已评价");
        }

        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            return ResponseResult.fail("评分必须在1-5之间");
        }

        Evaluation eval = new Evaluation();
        eval.setEvalId(String.valueOf(System.currentTimeMillis()));
        eval.setOrderId(orderId);
        eval.setEvaluatorId(user.getUserId().longValue());
        eval.setEvaluatedId(order.getSellerId().longValue());
        eval.setScore(request.getScore());
        eval.setContent(request.getContent());
        eval.setImages(request.getImages());
        eval.setCreateTime(LocalDateTime.now());
        eval.setStatus(0); // 正常

        evaluationRepository.save(eval);

        return ResponseResult.ok("评价成功");
    }

    // ==================== 管理员订单管理 ====================

    @Override
    public ResponseResult searchOrders(String orderNo, Integer buyerId, Integer sellerId,
                                       Integer status, String startTime, String endTime, Integer orderType) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.like(root.get("orderId"), "%" + orderNo + "%"));
            }
            if (buyerId != null) {
                predicates.add(cb.equal(root.get("buyerId"), buyerId));
            }
            if (sellerId != null) {
                predicates.add(cb.equal(root.get("sellerId"), sellerId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (orderType != null) {
                predicates.add(cb.equal(root.get("orderType"), orderType));
            }
            if (startTime != null && !startTime.isBlank()) {
                LocalDateTime start = LocalDate.parse(startTime).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start));
            }
            if (endTime != null && !endTime.isBlank()) {
                LocalDateTime end = LocalDate.parse(endTime).atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), end));
            }

            query.orderBy(cb.desc(root.get("createTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Order> orders = orderRepository.findAll(spec);
        List<Map<String, Object>> result = orders.stream().map(this::buildAdminOrderMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    @Override
    @Transactional
    public ResponseResult handleDispute(String orderId, String disputeResult) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (disputeResult == null || disputeResult.isBlank()) {
            return ResponseResult.fail("纠纷处理结果不能为空");
        }

        order.setDisputeStatus(2); // 2=已处理
        order.setDisputeResult(disputeResult);
        orderRepository.save(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", orderId);
        data.put("disputeStatus", "已处理");
        data.put("disputeResult", disputeResult);
        return ResponseResult.ok("纠纷处理成功", data);
    }

    @Override
    public void exportOrders(Integer status, String startTime, String endTime,
                             Integer orderType, HttpServletResponse response) {
        // 构建查询条件
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (orderType != null) {
                predicates.add(cb.equal(root.get("orderType"), orderType));
            }
            if (startTime != null && !startTime.isBlank()) {
                LocalDateTime start = LocalDate.parse(startTime).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start));
            }
            if (endTime != null && !endTime.isBlank()) {
                LocalDateTime end = LocalDate.parse(endTime).atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), end));
            }
            query.orderBy(cb.desc(root.get("createTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Order> orders = orderRepository.findAll(spec);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单数据");

            // 表头
            String[] headers = {"订单号", "买家", "卖家", "商品名称", "订单类型", "金额",
                    "支付方式", "订单状态", "创建时间", "支付时间", "发货时间", "收货时间",
                    "收货地址", "物流信息", "纠纷状态", "备注"};
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowNum = 1;
            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderId());

                // 买家名称
                String buyerName = "";
                if (order.getBuyerId() != null) {
                    buyerName = userRepository.findById(order.getBuyerId().longValue())
                            .map(u -> u.getUserName() != null ? u.getUserName() : u.getUserAccount())
                            .orElse("");
                }
                row.createCell(1).setCellValue(buyerName);

                // 卖家名称
                String sellerName = "";
                if (order.getSellerId() != null) {
                    sellerName = userRepository.findById(order.getSellerId().longValue())
                            .map(u -> u.getUserName() != null ? u.getUserName() : u.getUserAccount())
                            .orElse("");
                }
                row.createCell(2).setCellValue(sellerName);

                // 商品名称
                List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
                String goodsNames = goodsList.stream()
                        .map(OrderGoods::getGoodsName)
                        .collect(Collectors.joining("、"));
                row.createCell(3).setCellValue(goodsNames);

                row.createCell(4).setCellValue(order.getOrderType() != null && order.getOrderType() == 2 ? "定制订单" : "普通订单");
                row.createCell(5).setCellValue(order.getAmount() != null ? order.getAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(order.getPayType() != null ? order.getPayType() : "");
                row.createCell(7).setCellValue(statusToChinese(order.getStatus()));
                row.createCell(8).setCellValue(order.getCreateTime() != null ? order.getCreateTime().format(dtf) : "");
                row.createCell(9).setCellValue(order.getPayTime() != null ? order.getPayTime().format(dtf) : "");
                row.createCell(10).setCellValue(order.getDeliveryTime() != null ? order.getDeliveryTime().format(dtf) : "");
                row.createCell(11).setCellValue(order.getReceiveTime() != null ? order.getReceiveTime().format(dtf) : "");
                row.createCell(12).setCellValue(order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
                row.createCell(13).setCellValue(order.getLogisticsInfo() != null ? order.getLogisticsInfo() : "");
                row.createCell(14).setCellValue(disputeStatusToChinese(order.getDisputeStatus()));
                row.createCell(15).setCellValue(order.getRemark() != null ? order.getRemark() : "");
            }

            // 自动列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=orders_export.xlsx");
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException("导出订单数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseResult getAdminOrderStats() {
        List<Order> allOrders = orderRepository.findAll();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalOrders", allOrders.size());
        stats.put("pendingOrders", allOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 0).count());
        stats.put("paidOrders", allOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 1).count());
        stats.put("shippedOrders", allOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 3).count());
        stats.put("completedOrders", allOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 4).count());
        stats.put("cancelledOrders", allOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 5).count());
        stats.put("disputeOrders", allOrders.stream().filter(o -> o.getDisputeStatus() != null && o.getDisputeStatus() == 1).count());

        BigDecimal totalAmount = allOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() >= 1 && o.getStatus() <= 4)
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAmount", totalAmount);

        return ResponseResult.ok(stats);
    }

    @Override
    @Transactional
    public ResponseResult applyDispute(String username, String orderId, String reason) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 4) {
            return ResponseResult.fail("只有已完成的订单可以申请维权");
        }
        if (order.getDisputeStatus() != null && order.getDisputeStatus() >= 1) {
            return ResponseResult.fail("该订单已申请维权，请等待管理员处理");
        }
        if (reason == null || reason.isBlank()) {
            return ResponseResult.fail("维权原因不能为空");
        }

        order.setDisputeStatus(1); // 1=维权中
        order.setDisputeTime(LocalDateTime.now());
        // 保留原有备注，追加维权原因
        String existingRemark = order.getRemark() != null ? order.getRemark() : "";
        String prefix = existingRemark.isEmpty() ? "" : existingRemark + " | ";
        order.setRemark(prefix + "维权原因：" + reason);
        orderRepository.save(order);

        return ResponseResult.ok("维权申请已提交，请等待管理员处理");
    }

    // ==================== 统一交易管理 ====================

    @Override
    public ResponseResult getAllUserOrders(String username, Integer status, String role) {
        User user = getUserByUsername(username);
        Integer userId = user.getUserId().intValue();

        List<Map<String, Object>> allOrders = new ArrayList<>();

        // 获取买家订单
        if (role == null || role.isBlank() || "buyer".equals(role)) {
            List<Order> buyerOrders;
            if (status != null) {
                buyerOrders = orderRepository.findByBuyerIdAndStatusOrderByCreateTimeDesc(userId, status);
            } else {
                buyerOrders = orderRepository.findByBuyerIdOrderByCreateTimeDesc(userId);
            }
            for (Order order : buyerOrders) {
                Map<String, Object> map = buildUnifiedOrderMap(order, "buyer");
                allOrders.add(map);
            }
        }

        // 获取卖家订单
        if (role == null || role.isBlank() || "seller".equals(role)) {
            List<Order> sellerOrders;
            if (status != null) {
                sellerOrders = orderRepository.findBySellerIdAndStatusOrderByCreateTimeDesc(userId, status);
            } else {
                sellerOrders = orderRepository.findBySellerIdOrderByCreateTimeDesc(userId);
            }
            for (Order order : sellerOrders) {
                Map<String, Object> map = buildUnifiedOrderMap(order, "seller");
                allOrders.add(map);
            }
        }

        // 按创建时间降序排列
        allOrders.sort((a, b) -> {
            String timeA = (String) a.getOrDefault("time", "");
            String timeB = (String) b.getOrDefault("time", "");
            return timeB.compareTo(timeA);
        });

        return ResponseResult.ok(allOrders);
    }

    @Override
    public ResponseResult getTradeStats(String username) {
        User user = getUserByUsername(username);
        Integer userId = user.getUserId().intValue();

        Map<String, Object> stats = new LinkedHashMap<>();

        // 买家统计
        long buyOrders = orderRepository.countByBuyerId(userId);
        BigDecimal buyAmount = orderRepository.sumAmountByBuyerId(userId);
        stats.put("buyOrders", buyOrders);
        stats.put("buyAmount", buyAmount != null ? buyAmount : BigDecimal.ZERO);

        // 卖家统计
        long sellOrders = orderRepository.countBySellerId(userId);
        BigDecimal sellAmount = orderRepository.sumAmountBySellerId(userId);
        long pendingOrders = orderRepository.countBySellerIdAndStatus(userId, 0)
                + orderRepository.countBySellerIdAndStatus(userId, 1);
        long goodsCount = goodsRepository.countByCreatorId(userId.longValue());
        stats.put("sellOrders", sellOrders);
        stats.put("sellAmount", sellAmount != null ? sellAmount : BigDecimal.ZERO);
        stats.put("pendingOrders", pendingOrders);
        stats.put("totalGoods", goodsCount);

        // 总计
        stats.put("totalOrders", buyOrders + sellOrders);
        stats.put("totalAmount", (buyAmount != null ? buyAmount : BigDecimal.ZERO)
                .add(sellAmount != null ? sellAmount : BigDecimal.ZERO));

        return ResponseResult.ok(stats);
    }

    /** 构建统一订单Map（含角色标识） */
    private Map<String, Object> buildUnifiedOrderMap(Order order, String role) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getOrderId());
        map.put("no", order.getOrderId());
        map.put("role", role); // "buyer" 或 "seller"
        map.put("time", order.getCreateTime() != null
                ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        map.put("status", statusToString(order.getStatus()));
        map.put("statusText", statusToChinese(order.getStatus()));
        map.put("orderType", order.getOrderType() != null && order.getOrderType() == 2 ? "定制订单" : "普通订单");
        map.put("payType", order.getPayType());
        map.put("paymentStatus", paymentStatusToString(order.getPaymentStatus()));
        map.put("total", order.getAmount());
        map.put("amount", order.getAmount());
        map.put("deposit", order.getDeposit());
        map.put("balance", order.getBalance());
        map.put("address", order.getDeliveryAddress());
        map.put("logisticsInfo", order.getLogisticsInfo());
        map.put("cancelReason", order.getCancelReason());
        map.put("remark", order.getRemark());
        map.put("disputeStatus", disputeStatusToChinese(order.getDisputeStatus()));
        map.put("disputeResult", order.getDisputeResult());

        // 对方信息
        if ("buyer".equals(role) && order.getSellerId() != null) {
            userRepository.findById(order.getSellerId().longValue())
                    .ifPresent(seller -> {
                        map.put("otherName", seller.getUserName() != null ? seller.getUserName() : seller.getUserAccount());
                        map.put("otherLabel", "卖家");
                    });
        } else if ("seller".equals(role) && order.getBuyerId() != null) {
            userRepository.findById(order.getBuyerId().longValue())
                    .ifPresent(buyer -> {
                        map.put("otherName", buyer.getUserName() != null ? buyer.getUserName() : buyer.getUserAccount());
                        map.put("otherLabel", "买家");
                        map.put("buyerPhone", buyer.getPhone());
                    });
        }

        // 订单商品
        List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
        if (!goodsList.isEmpty()) {
            OrderGoods firstItem = goodsList.get(0);
            map.put("name", firstItem.getGoodsName());
            map.put("quantity", firstItem.getNum());
            map.put("price", firstItem.getPrice());
            Optional<Goods> goods = goodsRepository.findById(firstItem.getGoodsId().longValue());
            map.put("image", goods.map(Goods::getImageUrl).orElse(""));
        } else {
            map.put("name", "");
            map.put("quantity", 0);
            map.put("price", BigDecimal.ZERO);
            map.put("image", "");
        }

        // 评价状态
        map.put("commented", evaluationRepository.existsByOrderId(order.getOrderId()));

        return map;
    }

    // ==================== 辅助方法 ====================

    private String statusToChinese(Integer status) {
        if (status == null) return "待支付";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "待发货";
            case 3 -> "已发货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知";
        };
    }

    private String disputeStatusToChinese(Integer disputeStatus) {
        if (disputeStatus == null) return "无纠纷";
        return switch (disputeStatus) {
            case 0 -> "无纠纷";
            case 1 -> "维权中";
            case 2 -> "已处理";
            default -> "无纠纷";
        };
    }

    /** 构建管理员订单列表中的单条数据 */
    private Map<String, Object> buildAdminOrderMap(Order order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getOrderId());
        map.put("no", order.getOrderId());
        map.put("amount", order.getAmount());
        map.put("orderType", order.getOrderType() != null && order.getOrderType() == 2 ? "定制订单" : "普通订单");
        map.put("status", statusToString(order.getStatus()));
        map.put("statusText", statusToChinese(order.getStatus()));
        map.put("payType", order.getPayType());
        map.put("disputeStatus", disputeStatusToChinese(order.getDisputeStatus()));
        map.put("time", order.getCreateTime() != null
                ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        map.put("payTime", order.getPayTime() != null
                ? order.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        map.put("address", order.getDeliveryAddress());
        map.put("logisticsInfo", order.getLogisticsInfo());
        map.put("cancelReason", order.getCancelReason());
        map.put("disputeResult", order.getDisputeResult());
        map.put("remark", order.getRemark());

        // 买家信息
        if (order.getBuyerId() != null) {
            map.put("buyerId", order.getBuyerId());
            userRepository.findById(order.getBuyerId().longValue())
                    .ifPresent(buyer -> {
                        map.put("buyer", buyer.getUserName() != null ? buyer.getUserName() : buyer.getUserAccount());
                        map.put("buyerPhone", buyer.getPhone());
                    });
        }
        // 卖家信息
        if (order.getSellerId() != null) {
            map.put("sellerId", order.getSellerId());
            userRepository.findById(order.getSellerId().longValue())
                    .ifPresent(seller -> {
                        map.put("seller", seller.getUserName() != null ? seller.getUserName() : seller.getUserAccount());
                        map.put("sellerPhone", seller.getPhone());
                    });
        }

        // 订单商品
        List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
        List<Map<String, Object>> items = goodsList.stream().map(og -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("goodsId", og.getGoodsId());
            itemMap.put("name", og.getGoodsName());
            itemMap.put("price", og.getPrice());
            itemMap.put("quantity", og.getNum());
            Optional<Goods> goods = goodsRepository.findById(og.getGoodsId().longValue());
            itemMap.put("image", goods.map(Goods::getImageUrl).orElse(""));
            return itemMap;
        }).collect(Collectors.toList());
        map.put("items", items);

        // 评价信息
        map.put("commented", evaluationRepository.existsByOrderId(order.getOrderId()));

        return map;
    }
}
