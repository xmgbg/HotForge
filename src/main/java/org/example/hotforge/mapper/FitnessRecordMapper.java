package org.example.hotforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.hotforge.entity.FitnessRecord;

@Mapper
public interface FitnessRecordMapper extends BaseMapper<FitnessRecord> {
}