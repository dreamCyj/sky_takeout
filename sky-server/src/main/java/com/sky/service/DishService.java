package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    void saveWithFlavors(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteByIds(List<Long> ids);
    /**
     * 启用、禁用菜品
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);


    void updateWithFlavor(DishDTO dishDTO);

    DishVO getByIdWithFlavor(Long id);
}
