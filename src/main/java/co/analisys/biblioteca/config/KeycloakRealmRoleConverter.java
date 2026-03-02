package co.analisys.biblioteca.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Object realmAccessObj = jwt.getClaims().get("realm_access");

        if (!(realmAccessObj instanceof Map)) {
            return Collections.emptyList();
        }

        Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;

        Object rolesObj = realmAccess.get("roles");

        if (!(rolesObj instanceof List)) {
            return Collections.emptyList();
        }

        List<?> roles = (List<?>) rolesObj;

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}