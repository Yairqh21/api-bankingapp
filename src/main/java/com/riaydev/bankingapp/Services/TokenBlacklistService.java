package com.riaydev.bankingapp.Services;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final CacheManager cacheManager;

    public TokenBlacklistService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void addToBlacklist(String token, long expirationTimeMillis) {
        Cache cache = cacheManager.getCache("blacklist");
        if (cache != null) {
            long duration = expirationTimeMillis - System.currentTimeMillis();
            if (duration > 0) {
                cache.put(token, System.currentTimeMillis() + duration);
            }
        }
    }

    public boolean isTokenBlacklisted(String token) {
        Cache cache = cacheManager.getCache("blacklist");
        if (cache != null) {
            Long expirationTime = cache.get(token, Long.class);
            if (expirationTime != null && System.currentTimeMillis() < expirationTime) {
                return true;
            }
        }
        return false;
    }
}
