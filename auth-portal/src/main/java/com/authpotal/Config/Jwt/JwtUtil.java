package com.authpotal.Config.Jwt;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import com.authpotal.Enum.Role;

import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class JwtUtil {

    @Inject
    JWTParser jwtParser;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.expiration")
    long expiration;

    public String generateToken(String email, Role role) throws Exception {
        try {
            String token = Jwt.issuer(issuer)
                    .subject(email)
                    .groups(Set.of(role.name()))
                    .claim("role", role.name())
                    .expiresAt(Instant.now().plusSeconds(expiration))
                    .sign();
            System.out.println(" Token generated:\n" + token);
            return token;
        } catch (Exception e) {
            System.out.println(" Error generating token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            JsonWebToken jwt = parseToken(token);
            Instant expiry = Instant.ofEpochSecond(jwt.getExpirationTime());
            return Instant.now().isBefore(expiry) && issuer.equals(jwt.getIssuer());
        } catch (Exception e) {
            System.out.println(" Invalid Token: " + e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) throws ParseException {
        return parseToken(token).getSubject();
    }

    public Set<String> extractRoles(String token) throws ParseException {
        return parseToken(token).getGroups();
    }

    private JsonWebToken parseToken(String token) throws ParseException {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtParser.parse(token);
    }

    public String getTokenFromEmail(String email) {
        return Jwt.issuer("authpotal")
                .subject(email)
                .expiresAt(Instant.now().plusSeconds(60)) // expires in 1 min
                .sign();
    }

	Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenInvalidated(String token) {
        return blacklistedTokens.contains(token);
    }


}
