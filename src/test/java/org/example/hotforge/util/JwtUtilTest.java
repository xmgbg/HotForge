package org.example.hotforge.util;

import org.example.hotforge.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=");
        properties.setExpiration(3600);
        jwtUtil = new JwtUtil(properties);
        jwtUtil.init();
    }

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtUtil.generateToken(1L, "13800138000", "USER", 0);

        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
        assertThat(jwtUtil.getPhone(token)).isEqualTo("13800138000");
        assertThat(jwtUtil.getRole(token)).isEqualTo("USER");
        assertThat(jwtUtil.parseToken(token).get("tokenVersion", Integer.class)).isZero();
    }
}
