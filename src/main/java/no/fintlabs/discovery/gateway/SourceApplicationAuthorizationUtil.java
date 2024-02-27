package no.fintlabs.discovery.gateway;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SourceApplicationAuthorizationUtil {

    public Long getSourceApplicationId(Authentication authentication) {
        return no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationUtil.getSourceApplicationId(authentication);
    }
}
