package org.example.hotforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.hotforge.entity.Course;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}