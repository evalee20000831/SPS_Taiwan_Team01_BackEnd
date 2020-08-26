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
import com.google.appengine.api.datastore.Key; 
import com.google.appengine.api.datastore.KeyFactory;

import java.io.IOException;
import java.io.PrintWriter; // Write 
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.datastore.EntityNotFoundException; 

@WebServlet("/add")
public class AddUserServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String userIdA= request.getParameter("userId"); // target's userId (user A) 
    String usernameB = request.getParameter("friendUsername"); // requester's username (user B) 
    
    String status = addAccess(userIdA, usernameB); 

    if ("400".equals(status)){
      // username is null or repeat userId (already in accessList) or user wants to add itself in accessList 
      response.setStatus(400); 
    } else if ("404".equals(status)){
      // usernameB not in database 
      response.setStatus(404); 
    } else {
      // username is in database 
      response.setStatus(204); 
    }
    response.setHeader("Access-Control-Allow-Origin", "*"); 
    response.setHeader("Access-Control-Allow-Credentials", "true"); 
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS"); 
  }

  private String addAccess (String userIdA, String usernameB) {
    if (userIdA==null || "".equals(userIdA) || usernameB==null || "".equals(usernameB) ){
      return "400"; 
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
    Filter usernameFliter = new Query.FilterPredicate ("username", Query.FilterOperator.EQUAL, usernameB); // target 
    Query query = new Query("userInfo").setFilter(usernameFliter); 
    PreparedQuery results = datastore.prepare(query); 
    
    String nameEntity = null; 
    ArrayList<String> accessList = new ArrayList<String>(); 
    long userIdEntity = 0; 

    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
      accessList = (ArrayList<String>) entity.getProperty("accessList"); 
      userIdEntity = entity.getKey().getId(); 
    }
    String userIdEntityString = Long.toString(userIdEntity); 

    if (nameEntity==null){
      System.out.println("username is not in database"); 
      return "404"; 
    } else if (userIdEntityString.equals(userIdA)){
      System.out.println("User wants to add itself in the accessList"); 
      return "400"; 
    } else if (accessList == null){
      // no friend 
      accessList = new ArrayList<String>(); 
      System.out.println("no friend"); 
    } else if (accessList.contains(userIdA)){
      //repeat userId  
      System.out.println("repeat userid"); 
      return "400"; 
    } 
    // add userIdA to B's accessList 
    System.out.println("Add friend"); 
    System.out.println("accessList = " + accessList); 
    accessList.add(userIdA); 
    Key key = KeyFactory.createKey("userInfo", userIdEntity); 
    System.out.println("key = " + key); 
    try{
      Entity entityChange = datastore.get(key);
      entityChange.setProperty("accessList", accessList);
      datastore.put(entityChange); 
    } catch (EntityNotFoundException e){
      // should never happen 
      System.out.println("EntityNotFoundException"); 
    }
    return "204"; 
  }
  
  @Override 
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String userIdA= request.getParameter("userId"); // target's userId (user B) 
    String usernameB = request.getParameter("friendUsername"); // requester's username (user A) 
    System.out.println("In delete"); 

    String status = deleteAccess(userIdA, usernameB); 
    
    if ("400".equals(status)){
      response.setStatus(400); 
    } else if ("404".equals(status)){
      response.setStatus(404); 
    } else {
      response.setStatus(204); 
    }
    response.setHeader("Access-Control-Allow-Origin", "*"); 
    response.setHeader("Access-Control-Allow-Credentials", "true"); 
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS"); 
  }

  private String deleteAccess(String userIdA, String usernameB){
    if (userIdA==null || "".equals(userIdA) || usernameB==null || "".equals(usernameB) ){
      System.out.println("inputs are null"); 
      return "400"; 
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
    Filter usernameFliter = new Query.FilterPredicate ("username", Query.FilterOperator.EQUAL, usernameB); // target 
    Query query = new Query("userInfo").setFilter(usernameFliter); 
    PreparedQuery results = datastore.prepare(query); 

    String nameEntity = null; 
    ArrayList<String> accessList = new ArrayList<String>(); 
    long userIdEntity = 0; 

    for (Entity entity : results.asIterable()) {
      nameEntity = (String) entity.getProperty("username");
      accessList = (ArrayList<String>) entity.getProperty("accessList"); 
      userIdEntity = entity.getKey().getId(); 
    }

    if (nameEntity == null || accessList == null){
      System.out.println("username is not in database or accessList is null"); 
      return "404"; 
    } else if (!accessList.contains(userIdA)){
      // useridA is not in database 
      System.out.println("useridA is not in accessList"); 
      return "404"; 
    } 

    System.out.println("Delete friend"); 
    Key key = KeyFactory.createKey("userInfo", userIdEntity); 
    System.out.println("key = " + key); 
    accessList.remove(userIdA); 
    
    try{
      Entity entityDelete = datastore.get(key);
      entityDelete.setProperty("accessList", accessList);
      datastore.put(entityDelete); 
    } catch (EntityNotFoundException e){
      // should never happen 
      System.out.println("EntityNotFoundException"); 
    }
    return "204"; 
  }
}


