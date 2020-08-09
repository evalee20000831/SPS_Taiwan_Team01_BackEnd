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
 * creates page for user login and has following 7 methods 
 * - doGet
 * - doPost
 * - messageAfterLogin
 * - checkExistUser
 * - getUsername
 * - messageForPasswordError
 * - convertToJson
 */ 
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 
  private boolean passwordCheck = false; 
  // initialization for json 
  String json = null; 

  /**  
   * creates the login form
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
  */ 
  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter writeOut = response.getWriter(); 
    // create the form for login 
    writeOut.println("<h1>Home</h1>");
    response.setContentType("text/html");

    // check correctness of password 
    if (passwordCheck == true){
      messageForPasswordError(response); 
    }
    passwordCheck = false; 

    // ask for login info (Post method) 
    writeOut.println("<form method=\"POST\" action=\"/login\">");
    writeOut.println("<p>username = </p>"); 
    writeOut.println("<input name=\"username\"/>"); // username 
    writeOut.println("<p>password = </p>");
    writeOut.println("<input name=\"password\"/>"); // password 
    writeOut.println("<br/>");
    writeOut.println("<button>Submit</button>");
    writeOut.println("</form>"); 
    writeOut.println("<p>Register <a href=/register>here</a>.</p>");
  }
  
  /**
   * ask user to login 
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
  */ 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String username = request.getParameter("username"); // request for username 
    String password = request.getParameter("password"); // request for password 
    
    // TEST: System.out.println("At doPost Your username is " + username); 
    // TEST: System.out.println("At doPost Your username is " + password); 

    if (checkExistUser(request, username)){
      // username cannot find in database  
      response.sendRedirect("/register"); 
    } else if (getUsername(request, username, password)){
      // if password is incorrect 
      System.out.println("PasswordError"); 
      passwordCheck = true; 
      response.sendRedirect("/login"); 
    }
    
    // TEST: messageAfterLogin(response, username);
    // TEST: will need to remove "response.setContentType("application/json;"); 
    
    // login successfully  
    // Send the JSON as the response 
    response.setContentType("application/json;");
    response.getWriter().println(json); 
  }

  /**
   * (NOT USED)supportive testing method - puts message to user after login 
   * @param response (HttpServletResponse)
   * @param username (String) login username 
  */ 
  private void messageAfterLogin (HttpServletResponse response, String username) throws IOException {
    PrintWriter writeOut = response.getWriter(); 
    response.setContentType("text/html");
    writeOut.println("<p>You have login! " + username + "</p>");
    writeOut.println("<p>Please logout <a href=/login>here</a>.</p>");
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

    //TEST 
    System.out.println("At getUsername username= " + username); 

    String nameEntity = null; 

    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
    }
    if (nameEntity == null) {
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
  private boolean getUsername(HttpServletRequest request, String username, String password){
    Filter usernameFliter = new Query.FilterPredicate ("username", Query.FilterOperator.EQUAL, username); 
    Filter passwordFliter = new Query.FilterPredicate("password", Query.FilterOperator.EQUAL, password); 
    // combine two fliters together 
    CompositeFilter nameAndPasswordFliter = CompositeFilterOperator.and(usernameFliter, passwordFliter);
    Query query = new Query("userInfo").setFilter(nameAndPasswordFliter); 
    PreparedQuery results = datastore.prepare(query);
    
    //TEST 
    System.out.println("At getUsername username= " + username); 
    System.out.println("At getUsername username= " + password); 
    System.out.println("At getUsername L results= " + results); 

    String nameEntity = null; 
    String passwordEntity = null; 
    long userIdEntity = 0; // json // can only do type long
    String emailEntity = null; // json

    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
      passwordEntity = (String) entity.getProperty("password");

      // prepare for json. 
      // add to collection only when password and username are checked (below if-else)
      userIdEntity = entity.getKey().getId(); 
      emailEntity = (String) entity.getProperty("email"); 

      // TEST 
      System.out.println("At getUsername L entity= " + entity);
      System.out.println("userIdEntity = " + userIdEntity); 
    }

    // nothing will be given if entity does not match to anything 
    if (passwordEntity == null){
      // given password does not match the password in database 
      return true; 
    }
    // password is correct  
    UserInfo userAccount = new UserInfo (userIdEntity, emailEntity, nameEntity); 
    // convert accountInfo to json string 
    json = convertToJson(userAccount); 
    return false; 
  }

  /**
   * prints messages to page when the given password does not match the password in database 
   * @param response (HttpServletResponse) 
  */ 
  private void messageForPasswordError(HttpServletResponse response) throws IOException{
    PrintWriter writeOut = response.getWriter();
    writeOut.println("<p>Password does not match the username</p>"); 
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
}


