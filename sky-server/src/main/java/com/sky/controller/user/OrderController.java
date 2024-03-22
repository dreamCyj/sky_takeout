package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @ApiOperation("查看历史订单")
    @GetMapping("/historyOrders")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单分页查询：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @ApiOperation("查看订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("根据订单id查询详情");
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) throws Exception {
        log.info("根据订单id取消订单");
        orderService.userCancel(id);
        return Result.success();
    }
    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result<String> repetition(@PathVariable Long id){
        log.info("根据订单id再来一单");
        orderService.repetition(id);
        return Result.success();
    }
    @ApiOperation("客户催单")
    @GetMapping("/reminder/{id}")
    public Result<String> reminder(@PathVariable Long id){
        log.info("客户催单");
        orderService.reminder(id);
        return Result.success();
    }
}
