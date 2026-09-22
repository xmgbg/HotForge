package org.example.hotforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data// 提供了 @Getter, @Setter, @RequiredArgsConstructor等方法
@TableName("user")
public class User{
    @TableId(type = IdType.AUTO)// 自动递增主键
    private Long id;

    private String phone;

    private String password;

    private String nickname;

    private String avatar;

    private String role;

    private String trainerStatus;

    private LocalDateTime vipExpireTime;

    private Integer tokenVersion;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
