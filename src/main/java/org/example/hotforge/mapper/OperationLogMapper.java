package org.example.hotforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.hotforge.entity.OperationLog;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}