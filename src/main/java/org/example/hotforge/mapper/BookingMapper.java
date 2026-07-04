package org.example.hotforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.hotforge.entity.Booking;

@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}