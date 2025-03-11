package nju.edu.cn.qysca.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@Data
public final class JwtUtil {

    private static final String SECRET="6e12d69c-af21-4cc2-b32d-3fa949b50106";

    private static final long EXPIRE=60*60*1000L;

    private JwtUtil(){}

    /**
     * 签发jwt
     *
     * @param uid
     * @return
     */
    public static String createJWT(String uid) {
        Date date = new Date();
        Date expireDate = new Date(date.getTime() + EXPIRE);

        String jwt = JWT.create()
                //可以将基本信息放到claims中
                .withClaim("uid",uid)
                //超时设置,设置过期的日期
                .withExpiresAt(expireDate)
                //签发时间
                .withIssuedAt(new Date())
                //SECRET加密
                .sign(Algorithm.HMAC256(SECRET));
        return jwt;
    }

    /**
     * 解析JWT
     *
     * @param token
     * @return
     */
    public static String parseJwt(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaims().get("uid").as(String.class);
        } catch (TokenExpiredException e) {
            throw new RuntimeException();
        } catch (JWTVerificationException | IllegalArgumentException e) { //解析错误 或者 token写错
            throw new RuntimeException();
        }
    }
}