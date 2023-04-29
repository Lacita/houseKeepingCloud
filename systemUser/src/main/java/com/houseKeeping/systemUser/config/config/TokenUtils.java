package com.houseKeeping.systemUser.config.config;

import com.houseKeeping.systemUser.pojo.pojo.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;

public class TokenUtils {

    private static final long EXPIRE_TIME = 60 * 60 * 1000;
    private static final String PRIVATE_KEY = "houseKeeping202302041522";  //密钥

    /**
     * 签名生成
     *
     * @param SysUser
     * @return
     */
    public static String sign(SysUser SysUser) {
        String token = null;
        HashMap<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        HashMap<String, Object> claims = new HashMap<>();
        //自定义有效载荷部分
        claims.put("account", SysUser.getUserId());
        token = Jwts.builder()

                //发证人
                .setIssuer("auth")
                //Jwt头
                .setHeader(header)
                //有效载荷
                .setClaims(claims)
                //设定签发时间
                .setIssuedAt(new Date())
                //设定过期时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                //使用HS256算法签名，PRIVATE_KEY为签名密钥
                .signWith(SignatureAlgorithm.HS256, PRIVATE_KEY)
                .compact();
        return token;
    }


    /**
     * 验证 token信息 是否正确
     *
     * @param token 被解析 JWT
     * @return 是否正确
     */
    public static boolean verify(String token) {
        //获取签名密钥
        //String key = userEntity.getUserPassword();
        //获取DefaultJwtParser
        try {
            Jwts.parser()
                    //设置 密钥
                    .setSigningKey(PRIVATE_KEY)
                    //设置需要解析的 token
                    .parseClaimsJws(token).getBody();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析当前token中BODY内容
     *
     * @param token
     * @return
     */
    public static Date getexpiration(String token) {
        Claims body = Jwts.parser()
                .setSigningKey(PRIVATE_KEY)
                .parseClaimsJws(token).getBody();
        HashMap<String, Object> map = new HashMap<>();
        // 获取当前token存活时间
        Date expiration = body.getExpiration();
        // 获取当前用户ID
        // Object account = body.get("account");
        // 获取当前用户数据ID
        System.out.println(expiration);
        map.put("expiration", expiration);
        return expiration;
    }

    /*
        解析token获取实体body值
     */
    public static Claims parserToken(String token) {
        return Jwts.parser()
                .setSigningKey(PRIVATE_KEY)
                .parseClaimsJws(token).getBody();
    }

    /*
    获取token中的用户ID
     */
    public static String getUserTokenID(String token) {
        Claims claims = parserToken(token);
        String account = (String) claims.get("account");
        return account;
    }

    /*
    校验Token有效时间
     */
    public static boolean isValidToken(String token) {
        Date expiration = parserToken(token).getExpiration();
        if (expiration.after(new Date(System.currentTimeMillis()))) {
            return false;
        }
        return true;

    }


}
