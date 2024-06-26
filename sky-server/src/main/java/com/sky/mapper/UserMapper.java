package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openId}")
    User getByOpenId(String openId);

    //@Insert("insert into user(openid) VALUES (#{openid})")
    //需要插入表后返回id，因此使用xml
    void insert(User user1);

    @Select("select * from user where id = #{id}")
    User getById(Long id);
    Integer getUserCountByDate(Map<Object, Object> map);
}
