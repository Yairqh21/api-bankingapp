package com.riaydev.bankingapp.Security;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Obtiene la clave secreta para firmar o validar JWT.
     *
     * @return la clave secreta.
     */
    private Key getSigningKey() {
        byte[] secretBytes = jwtSecret.getBytes();
        return new SecretKeySpec(secretBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Genera un JWT basado en la autenticación del usuario.
     *
     * @param authentication información de autenticación del usuario.
     * @return el token JWT.
     */
    public String generateToken(Authentication authentication) {
        return Jwts.builder()
                .setSubject(authentication.getPrincipal().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();
    }

    /**
     * Genera un JWT con información adicional (claims).
     *
     * @param claims   mapa con información adicional.
     * @param username identificador del usuario.
     * @return el token JWT.
     */
    public String generateTokenWithClaims(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, getSigningKey())
                .compact();
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token el token JWT.
     * @return el nombre de usuario (subject).
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Valida un token JWT asegurándose de que no esté expirado y que el usuario
     * coincida.
     *
     * @param token    el token JWT.
     * @param username el nombre de usuario esperado.
     * @return true si el token es válido, false de lo contrario.
     */
    public boolean validateToken(String token, String username) {
        try {
            return username.equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false; 
        }
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token el token JWT.
     * @return true si está expirado, false de lo contrario.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token JWT.
     *
     * @param token el token JWT.
     * @return la fecha de expiración.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un "claim" específico del token JWT.
     *
     * @param <T>            tipo del "claim".
     * @param token          el token JWT.
     * @param claimsResolver función que procesa los claims.
     * @return el valor del "claim".
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token JWT.
     *
     * @param token el token JWT.
     * @return un objeto Claims con toda la información del token.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();

    }

    /**
     * Extrae el token JWT del encabezado de la solicitud HTTP.
     *
     * @param request la solicitud HTTP que contiene el encabezado de autorización.
     * @return el token JWT si está presente y comienza con "Bearer ", de lo contrario, null.
     */
    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Devuelve un mensaje de error claro basado en la excepción lanzada al procesar
     * un JWT.
     *
     * @param e excepción relacionada con el JWT.
     * @return el mensaje de error.
     */
    public String extractErrorMessage(JwtException e) {
        if (e instanceof ExpiredJwtException) {
            return "The token has expired";
        } else if (e instanceof UnsupportedJwtException) {
            return "The token is not supported";
        } else if (e instanceof MalformedJwtException) {
            return "The token is invalid";
        } else if (e instanceof SignatureException) {
            return "The token signature is invalid";
        } else {
            return "Error processing the token";
        }
    }
}
