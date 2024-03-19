package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 批量删除 等价于for循环deleteByDishId
     * @param dishIds
     */
    void deleteByIds(List<Long> dishIds);



    void update();

    /**
     * 根据dishId查询口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
}
