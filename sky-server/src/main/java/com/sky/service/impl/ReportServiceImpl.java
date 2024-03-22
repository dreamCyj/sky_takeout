package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 对getTurnoverStatistics、getOrdersStatistics、getSalesTop10相同代码片段进行了抽取
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //营业额为 已完成订单 的金额总和
        List<LocalDate> dateList = getDateList(begin, end);
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询当天的营业额
            // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            Map<Object, Object> map = getMapByDate(date, null, Orders.COMPLETED);
            Double turnover = orderMapper.getAmountByDate(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));//取出每个 用, 连接
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //存放每天新增用户数量 --根据注册时间 select count(id) from user where create_time >= begin and create_time <= end
        List<Integer> newUserList = new ArrayList<>();
        //存放总用户数量 select count(id) from user where create_time <= end
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询当天的营业额
            // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<Object, Object> map = new HashMap<>();
            map.put("end", endTime);
            Integer totalUser = userMapper.getUserCountByDate(map);
            map.put("begin", beginTime);
            Integer newUser = userMapper.getUserCountByDate(map);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList, ","));//取出每个 用, 连接
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));
        return userReportVO;
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        //存放每天订单数量 select count(id) from orders where order_time >= begin and order_time <= end
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天有效订单 select count(id) from user where create_time <= end
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询当天的有效订单数量
            // select count(id) from orders where order_time > ? and order_time < ? and status = 5
            Map<Object, Object> map = getMapByDate(date, null, null);
            Integer orderCount = orderMapper.getCountByDate(map);//先查全部的订单 得到 每日订单数
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.getCountByDate(map);//加入条件后 查有效订单 得到 每日有效订单数
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();//订单总数
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get(); //有效订单总数
        double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = totalValidOrderCount.doubleValue()/totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //要查已完成订单的对应菜品或套餐销量 查orders 和 order_detail
        //select od.name,sum(od.number) number from order_detail od, orders o where od.order_id = o.id and o.status = 5 and o.order_time > ? and o.order_time < ? group by od.name order by number desc limit 0,10
        //写出sql 问题就解决了 转为mapper.xml
        Map<Object, Object> map = getMapByDate(begin, end, Orders.COMPLETED);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(map);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
        return   SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    public List<LocalDate> getDateList(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();//存放begin到end的每个日期
        dateList.add(begin);
        while (!begin.equals(end)){ //用==会导致内存溢出
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
    public Map<Object, Object> getMapByDate(LocalDate begin, LocalDate end, Integer status){
        LocalDateTime  beginTime;
        LocalDateTime  endTime;
        if(end == null){ //代表只传了一个date
            beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            endTime = LocalDateTime.of(begin, LocalTime.MAX);
        }else {//代表传的是begin和end
            beginTime = LocalDateTime.of(begin, LocalTime.MIN);
            endTime = LocalDateTime.of(end, LocalTime.MAX);
        }
        Map<Object, Object> map = new HashMap<>();
        map.put("end", endTime);
        map.put("begin", beginTime);
        map.put("status", status);
        return map;
    }
}
