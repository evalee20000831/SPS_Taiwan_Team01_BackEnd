package com.sps.app.servlets;

// JSON 
import com.google.gson.Gson; 

// API: datastore 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query; // 
import com.google.appengine.api.datastore.Query.Filter; 
import com.google.appengine.api.datastore.Query.FilterPredicate; 
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Key; 
// java 
import java.io.IOException;
import java.io.PrintWriter; 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//UserInfo.java 
import com.sps.app.servlets.UserInfo; 

import java.util.ArrayList;
import java.util.List;

/*
 * creates page for user login and has following 4 methods 
 * - doPost 
 * - checkExistUser
 * - checkPassword 
 * - convertToJson
 * - doDelete
 */ 
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 
  String json = null; // initialization for json 
  Key userId = null; // for deletion 
  
  /**
   * ask user to login 
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
  */ 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String username = request.getParameter("username"); // request for username 
    String password = request.getParameter("password"); // request for password 
    
    if (checkExistUser(request, username)){
      // username cannot find in database  
      System.out.println("User does not exist"); 
      // 404 indicates user not found 
      response.setStatus(404); 
      
    } else if (checkPassword(request, username, password)){
      // if password is incorrect 
      System.out.println("PasswordError"); 
      // 400 indicates the request sent by the client was syntactically incorrect.
      response.setStatus(401); 
    }

    // login successfully  
    // Send the JSON as the response 
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(json); 
    response.setHeader("Access-Control-Allow-Origin", "*"); 
    response.setHeader("Access-Control-Allow-Credentials", "true"); 
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS"); 
  }

  /**
   * checks if entered username exists in database 
   * @param request (HttpServletRequest) 
   * @param username (String) entered username 
   * @return (boolean) true if the user does not exist; false if the user does exist  
  */ 
  private boolean checkExistUser (HttpServletRequest request, String username){ 
    Filter usernameFliter = new Query.FilterPredicate ("username", Query.FilterOperator.EQUAL, username);
    Query query = new Query("userInfo").setFilter(usernameFliter); 
    PreparedQuery results = datastore.prepare(query);

    String nameEntity = null; 

    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
    }
    if (nameEntity == null) {
      // Testing (for Jeoy) 
      String testing = "The entered username is " + username; 
      UserInfo userAccount = new UserInfo (null, testing , "Username does not exist"); 
      json = convertToJson(userAccount);
      return true; 
    } 
    return false; 
  }

  /** 
   * check if the entered password matches the username and set up the json if they match
   * @param request (HttpServletRequest) 
   * @param username (String) the entered username  
   * @param password (String) entered password 
   * @return (boolean) false if the password does not match & true if they match; 
   */
  private boolean checkPassword(HttpServletRequest request, String username, String password){
    Filter usernameFliter = new Query.FilterPredicate ("username", Query.FilterOperator.EQUAL, username); 
    Filter passwordFliter = new Query.FilterPredicate("password", Query.FilterOperator.EQUAL, password); 
    // combine two fliters together 
    CompositeFilter nameAndPasswordFliter = CompositeFilterOperator.and(usernameFliter, passwordFliter);
    Query query = new Query("userInfo").setFilter(nameAndPasswordFliter); 
    PreparedQuery results = datastore.prepare(query);

    String nameEntity = null; 
    String passwordEntity = null; 
    long userIdEntity = 0; // json 
    String emailEntity = null; // json
    
    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
      passwordEntity = (String) entity.getProperty("password");

      // prepare for json 
      // add to collection only when password and username are checked (below if-else)
      userId = entity.getKey(); // for deletion 
      userIdEntity = userId.getId(); 
      emailEntity = (String) entity.getProperty("email"); 
    }
    
    // nothing will be given if entity does not match to anything 
    if (passwordEntity == null){
      // given password does not match the password in database 
      // Testing for Joey 
      UserInfo userAccount = new UserInfo ("userId exists", "email exists", "username exists"); 
      json = convertToJson(userAccount); 
      return true; 
    }
    // password is correct  
    String userIdEntityString = Long.toString(userIdEntity); 
    UserInfo userAccount = new UserInfo (userIdEntityString, emailEntity, nameEntity); 
    // convert accountInfo to json string 
    json = convertToJson(userAccount); 
    return false; 
  }

  /** 
   * Converts a UserInfo into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   * @param userInfo (UserInfo) user information convert into json 
   */
  private String convertToJson(UserInfo userInfo) {
    Gson gson = new Gson();
    String json = gson.toJson(userInfo);
    return json;
  }

  /**
   * delete a user 
   * @param request (HttpServletRequest) 
   * @param response (HttpServletResponse) 
   */ 
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException{  
    String username = request.getParameter("username"); // request for username 
    String password = request.getParameter("password"); // request for password 
    response.setContentType("application/json; charset=UTF-8");
    
    if (checkExistUser(request, username)){
      // username cannot find in database  
      System.out.println("user does not exist"); 
      response.getWriter().println(json); 
      // 404 indicates user not found 
      response.setStatus(404); 
    } else if (checkPassword(request, username, password)){
      // if password is incorrect 
      System.out.println("PasswordError"); 
      response.getWriter().println(json); 
      // 400 indicates the request sent by the client was syntactically incorrect.
      response.setStatus(401); 
    } else { 
      datastore.delete(userId); 
      System.out.println("userId " + userId + " is deleted"); 
      // 204 indicates the server successfully processed the request, and is not returning any content. 
      response.setStatus(204); 
    }
    response.setHeader("Access-Control-Allow-Origin", "*"); 
    response.setHeader("Access-Control-Allow-Credentials", "true"); 
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS"); 
  }
}


