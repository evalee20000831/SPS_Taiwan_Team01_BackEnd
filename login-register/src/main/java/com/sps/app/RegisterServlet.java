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
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.io.IOException;
import java.io.PrintWriter; // Write 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//UserInfo.java 
import com.sps.app.servlets.UserInfo; 

import java.util.ArrayList;
import java.util.List;

/* 
 * creates page for user register and has following 4 methods  
 * - doPost
 * - checkInput
 * - checkUsername
 * - convertToJson
 */ 
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
  
  public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
  String json = null; // initialization for json 

  /**
   * asks for register  
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
  */ 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String username = request.getParameter("username"); // unique
    String password = request.getParameter("password"); 
    String email = request.getParameter("email").toLowerCase(); // unique, lowercased 
    
    if (checkInput(username, password, email)){
      // true if three inputs are empty 
      System.out.println("three inputs are empty"); 
      response.setStatus(400);
    } else if (checkUsername(request, response, username, email)){
      // true if username and email is registered already  
      System.out.println("username or email already exist in database");  
      response.setStatus(401); 
    } else {
      // store the username 
      Entity taskEntity = new Entity("userInfo");
      taskEntity.setProperty("username", username); 
      taskEntity.setProperty("password", password); 
      taskEntity.setProperty("email", email); 
      datastore.put(taskEntity); 

      // get the user id for Json 
      Query query = new Query("userInfo").setFilter(new Query.FilterPredicate("username", Query.FilterOperator.EQUAL, username));
      PreparedQuery results = datastore.prepare(query);
      long userIdEntity = 0; 
      for (Entity entity : results.asIterable()) {
        userIdEntity = entity.getKey().getId(); // json 
      }
      String userIdEntityString = Long.toString(userIdEntity); 
      
      UserInfo userAccount = new UserInfo (userIdEntityString, email, username); 
      json = convertToJson(userAccount); 
      response.setStatus(201); 
    }
    
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(json); 
  }

  /**
   * check if entered username, password, or email is empty string or null 
   * @param username (String) entered username 
   * @param password (String) entered password 
   * @param email (String) entered email 
  */ 
  private boolean checkInput(String username, String password, String email){
    UserInfo userAccount = new UserInfo (null, null, null); // userIdEntityString, emailEntity, nameEntity
    json = convertToJson(userAccount); // will be reassigned if not equal to null 

    if (username.equals("")||username == null){
      return true; 
    } else if (password.equals("")||password == null){
      return true;
    } else if (email.equals("")||email == null){
      return true;
    }
    return false; 
  }

  /**
   * check if username or email already exist in database 
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
   * @return (boolean) true if either username or email is registered already; false if not registered 
   */ 
  private boolean checkUsername(HttpServletRequest request, HttpServletResponse response, String username, String email)throws IOException{
    Filter usernameFliter = new Query.FilterPredicate ("username", Query.FilterOperator.EQUAL, username); 
    Filter emailFliter = new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email); 
    // combine two fliters together 
    CompositeFilter nameAndGmailFliter = CompositeFilterOperator.or(usernameFliter, emailFliter);
    Query query = new Query("userInfo").setFilter(nameAndGmailFliter); 
    PreparedQuery results = datastore.prepare(query);

    String nameEntity = null; 
    String emailEntity = null; 
    long userIdEntity = 0; // json

    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
      emailEntity = (String) entity.getProperty("email");
      userIdEntity = entity.getKey().getId(); // json 
    }
    // entities contain values in database 
    if ((nameEntity == null)|(emailEntity == null)){
      // not registered 
      System.out.println("Not registered"); 
      return false; 
    }
    // registered 
    System.out.println("Registered"); 
    // For testing (Joey) 
    String userIdEntityString = Long.toString(userIdEntity); 
    UserInfo userAccount = new UserInfo ("userid already exists", emailEntity, nameEntity); 
    json = convertToJson(userAccount);
    return true; 
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


