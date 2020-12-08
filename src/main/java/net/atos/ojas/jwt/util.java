package net.atos.ojas.jwt;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class util {

    public static String createToken (List<String> groups) throws Exception {
        Date now = new Date(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.HOUR, 24);
        Date expires = cal.getTime();
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            String token = JWT.create()
                .withIssuer("OJAS")
                .withClaim("groups", groups)
                .withIssuedAt(now)
                .withNotBefore(now)
                .withExpiresAt(expires)
                .sign(algorithm);
            return token;
        } catch (JWTCreationException exception){
            throw exception;
        }
    }

    public static void validateToken (String token) throws Exception {
        try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("OJAS")
                .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException exception){
            throw exception;
        }
    }
    
}

