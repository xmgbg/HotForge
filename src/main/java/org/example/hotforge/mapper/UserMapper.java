package org.example.hotforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.hotforge.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User>{
    // 继承 BaseMapper，默认实现CRUD操作方法

}
