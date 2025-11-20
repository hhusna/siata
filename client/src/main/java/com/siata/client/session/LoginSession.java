package com.siata.client.session;

import com.siata.client.dto.PegawaiDto;

public class LoginSession {
    private static String jwt;
    private static String role;
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

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        LoginSession.username = username;
    }
}
