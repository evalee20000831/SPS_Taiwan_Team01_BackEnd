package com.sps.app.servlets;

public class UserInfo {
    private long userId;
    private String email; 
    private String username;
  
    public UserInfo(long userId, String email, String username){
      this.userId = userId;
      this.email = email;
      this.username = username;
    }
  }