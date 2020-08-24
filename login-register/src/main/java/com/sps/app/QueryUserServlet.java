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
//UserInfo.java 
import com.sps.app.servlets.UserInfo; 

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import com.google.appengine.api.datastore.EntityNotFoundException; 

@WebServlet("/query")
public class QueryUserServlet extends HttpServlet {
  public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  
  String json = null; // initialization for json 
  
  private class QueryInfo {
    private ArrayList<String> accessListEntity; 
    private ArrayList<String> usernameList; 

    private QueryInfo (ArrayList<String> accessListEntity, ArrayList<String> usernameList){
      this.accessListEntity = accessListEntity; 
      this.usernameList = usernameList; 
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String userIdB = request.getParameter("userId"); // requester's userId 
    
    if (userIdB == null || "".equals(userIdB)){
      // bad request 
      System.out.println("userIdB is null"); 
      json = convertToJson("userIdB is null"); 
      response.setStatus(400);
    } else if ("404".equals(queryAccess(userIdB))){
      // if userId cannot be found 
      json = convertToJson("userId cannot be found"); 
      response.setStatus(404); 
    } else {
      // if userId is found, return accessList 
      response.setStatus(200); 
    }
    
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(json); 
    response.setHeader("Access-Control-Allow-Origin", "*"); 
    response.setHeader("Access-Control-Allow-Credentials", "true"); 
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS"); 
  }

  private String queryAccess (String userIdB){
    long userIdBLong = Long.parseLong(userIdB);
    Key key = KeyFactory.createKey("userInfo", userIdBLong); // kind, num identifier 
    System.out.println("(queryAccess) key = " + key);  
    try{
      Entity entity = datastore.get(key);
      ArrayList<String> accessListEntity = (ArrayList<String>) entity.getProperty("accessList"); 
      // find all username with given userId in accessList 
      ArrayList<String> usernameList = new ArrayList<String>(); 
      if (accessListEntity==null){
        accessListEntity = new ArrayList<String>(); 
        System.out.println("AccessList is empty"); 
      } else {
        for (int i=accessListEntity.size()-1; i>=0; i--){
          String usernames = findUsername(accessListEntity.get(i)); 
          if ("404".equals(usernames)){
            accessListEntity.remove(accessListEntity.get(i)); 
          } else {
            usernameList.add(usernames); 
          }
        }
      }
      Collections.reverse(usernameList);
      QueryInfo userIdAndUsername = new QueryInfo(accessListEntity, usernameList); 
      json = convertToJson(userIdAndUsername); 
      return "200"; 
    } catch (EntityNotFoundException e){
      // should never happen 
      System.out.println("queryAccess: EntityNotFoundException"); 
      return "404"; 
    }
  }

  public String findUsername(String userIds){
    long userIdsLong = Long.parseLong(userIds);
    Key key = KeyFactory.createKey("userInfo", userIdsLong); // kind, num identifier 
    System.out.println("(findUsername) key = " + key); 
    try{
      Entity entity = datastore.get(key);
      String username = (String) entity.getProperty("username"); 
      return username; 
    } catch (EntityNotFoundException e){
      // should never happen 
      System.out.println("findusername: EntityNotFoundException"); 
      return "404"; 
    }
  }

  private String convertToJson(QueryInfo userIdAndUsername) {
    Gson gson = new Gson();
    String json = gson.toJson(userIdAndUsername);
    return json;
  }  

  // for Joey's testing 
  private String convertToJson(String detail) {
    Gson gson = new Gson();
    String json = gson.toJson(detail);
    return json;
  }  
}


