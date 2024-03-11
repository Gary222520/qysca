package nju.edu.cn.qysca.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@Data
public class JwtConfig {

    private static String secret;

    private static long expire;

    private Key key;

    @Value("${jwt.secret}")
    public void setJwtSecret(String jwtSecret) { //通过set让static方法读取配置文件中的值
        JwtConfig.secret = jwtSecret;
    }

    @Value("${jwt.expire}")
    public void setExpire(long expire) { //通过set让static方法读取配置文件中的值
        JwtConfig.expire = expire;
    }


    /**
     * 签发jwt
     *
     * @param userDO
     * @return
     */
    public String createJWT(UserDO userDO) {
        Date date = new Date();
        Date expireDate = new Date(date.getTime() + expire);

        String jwt = JWT.create()
                //可以将基本信息放到claims中
                .withClaim("uid",userDO.getUid())
                .withClaim("name", userDO.getName())
                .withClaim("email",userDO.getEmail())
                .withClaim("phone",userDO.getPhone())
                .withClaim("role", userDO.getRole())
                //超时设置,设置过期的日期
                .withExpiresAt(expireDate)
                //签发时间
                .withIssuedAt(new Date())
                //SECRET加密
                .sign(Algorithm.HMAC256(secret));
        return jwt;
    }

    /**
     * 解析JWT
     *
     * @param token
     * @return
     */
    public Map<String, Claim> parseJwt(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaims();
        } catch (TokenExpiredException e) {
            throw new PlatformException(403,"用户登录已过期");
        } catch (JWTVerificationException | IllegalArgumentException e) { //解析错误 或者 token写错
            throw new PlatformException(403, "登陆失败");
        }
    }
}