package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断商品在购物车中是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);//口味数据也查了 不同口味也算新数据
        //若存在 只进行数量+1
        if(list != null && !list.isEmpty()){
            /*dto里面要么是dish要么是setmeal 所以只能查到一条数据 写list是为了通用复用
            * 例如查看购物车或确认订单时 会将购物车中的数据全部罗列出来 这时候也会调用list方法
            * 并且传的参数shoppingCart只会有userId一个属性 查出该用户的所有购物车数据*/
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        }else {
            // 不存在则插入
            //判断是菜品还是套餐 从而去对应的表获得name等属性
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null){
                //添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else {
                //添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                                    .userId(BaseContext.getCurrentId())
                                    .build();
        return shoppingCartMapper.list(shoppingCart);

    }

    @Override
    public void deleteShoppingCart() {
        shoppingCartMapper.delete(BaseContext.getCurrentId());
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断商品在购物车中是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);//口味数据也查了 不同口味也算新数据
        if(list != null && !list.isEmpty()){
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if(number == 1){
                //数量为1直接删除
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else {
                shoppingCart.setNumber(number - 1);
                shoppingCartMapper.update(shoppingCart);
            }
        }
    }
}
