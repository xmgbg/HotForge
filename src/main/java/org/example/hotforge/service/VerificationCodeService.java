package org.example.hotforge.service;

import lombok.RequiredArgsConstructor;
import org.example.hotforge.common.exception.ClientException;
import org.example.hotforge.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_INTERVAL = Duration.ofSeconds(60);
    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local code=redis.call('GET',KEYS[1]); " +
            "if not code then return 0 end; " +
            "if code==ARGV[1] then redis.call('DEL',KEYS[1],KEYS[2]); return 1 end; " +
            "local remaining=redis.call('DECR',KEYS[2]); " +
            "if remaining<=0 then redis.call('DEL',KEYS[1],KEYS[2]) end; return 0",
            Long.class);

    private final StringRedisTemplate redis;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.auth.simulated-code-enabled:false}")
    private boolean enabled;

    public String issue(String phone, String purpose) {
        if (!enabled) {
            throw new ClientException(ResultCode.FORBIDDEN, "模拟验证码未启用");
        }
        String prefix = key(phone, purpose);
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(prefix + ":send", "1", SEND_INTERVAL))) {
            throw new ClientException(ResultCode.BAD_REQUEST, "请稍后再获取验证码");
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        redis.opsForValue().set(prefix + ":code", code, CODE_TTL);
        redis.opsForValue().set(prefix + ":attempts", "5", CODE_TTL);
        return code;
    }

    public void consume(String phone, String purpose, String code) {
        if (!enabled) {
            throw new ClientException(ResultCode.FORBIDDEN, "模拟验证码未启用");
        }
        String prefix = key(phone, purpose);
        Long valid = redis.execute(CONSUME_SCRIPT,
                List.of(prefix + ":code", prefix + ":attempts"), code);
        if (!Long.valueOf(1).equals(valid)) {
            throw new ClientException(ResultCode.CODE_INVALID);
        }
    }

    private String key(String phone, String purpose) {
        return "hotforge:verification:" + purpose + ":" + phone;
    }
}
