package com.dapm.security_service.config;

import com.dapm.security_service.models.User;
import com.dapm.security_service.models.UserRoleAssignment;
import com.dapm.security_service.repositories.UserRoleAssignmentRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private static final String SECRET_KEY= "5A7134743777397A24432646294A404E635266556A586E3272357538782F4125";
    @Autowired
    public UserRoleAssignmentRepository userRoleAssignmentRepository;

    public String extractUserName( String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
        Map<String, Object> extraClaims,
                UserDetails userDetails
    ){
        return  Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000*60*24))
                .signWith(getSingInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSingInKey(){
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);

    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims= extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username= extractUserName(token);
        return (username.equals((userDetails.getUsername()))) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }


    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSingInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        if (user.getOrgRole() != null) {
            claims.put("orgRole", user.getOrgRole().getName());
        }

        if (user.getOrganization() != null) {
            claims.put("organizationId", user.getOrganization().getId().toString());
            claims.put("organizationName", user.getOrganization().getName());
        }


        List<UserRoleAssignment> assignments = userRoleAssignmentRepository.findByUser(user);
        if (assignments != null && !assignments.isEmpty()) {
            List<Map<String, String>> projectRoles = assignments.stream()
                    .map(assignment -> {
                        Map<String, String> roleInfo = new HashMap<>();
                        roleInfo.put("project", assignment.getProject().getName());
                        roleInfo.put("role", assignment.getRole().getName());
                        return roleInfo;
                    })
                    .collect(Collectors.toList());

            claims.put("projectRoles", projectRoles);
        }

        return generateToken(claims, user);
    }



    public String extractOrgRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("orgRole", String.class);
    }




}
