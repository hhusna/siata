package com.siata.client.session;

import com.siata.client.dto.PegawaiDto;

public class LoginSession {
    private static String jwt;
    private static String role;
    private static String originalRole; // Role from backend database (never changes after login)
    private static String username;
    private static PegawaiDto pegawaiDto;

    public static PegawaiDto getPegawaiDto() {
        return pegawaiDto;
    }

    public static void setPegawaiDto(PegawaiDto pegawaiDto) {
        LoginSession.pegawaiDto = pegawaiDto;
    }

    public LoginSession() {
    }

    public static String getJwt() {
        return jwt;
    }

    public static void setJwt(String jwt) {
        LoginSession.jwt = jwt;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        LoginSession.role = role;
    }
    
    /**
     * Get the original role from backend database.
     * This never changes after login, used to check if user is DEV.
     */
    public static String getOriginalRole() {
        return originalRole;
    }
    
    /**
     * Set the original role from backend database.
     * Should only be called once when user logs in.
     */
    public static void setOriginalRole(String originalRole) {
        LoginSession.originalRole = originalRole;
    }
    
    /**
     * Check if the user's original role (from database) is DEV.
     */
    public static boolean isOriginallyDev() {
        return "DEV".equals(originalRole);
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        LoginSession.username = username;
    }
}
