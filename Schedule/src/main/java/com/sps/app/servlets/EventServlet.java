package com.sps.app.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter; 
import com.google.appengine.api.datastore.Query.FilterPredicate; 
import com.google.appengine.api.datastore.Query.FilterOperator;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.text.*;
import com.sps.app.Event;
import java.lang.*;

@WebServlet("/event")
public class EventServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String userId = request.getParameter("userId");
    if(userId == null){
      response.setStatus(400);
      response.getWriter().println("userId cannot be empty");
      return;
    }

    List<Event> eventList = new ArrayList<>();
    Query query = new Query("Event");
    PreparedQuery results = datastore.prepare(query);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    for (Entity entity : results.asIterable()) {
      String entityUser = (String) entity.getProperty("userId");
      if(entityUser.equals(userId)){
        String title = (String) entity.getProperty("title");
        String startTime = (String)entity.getProperty("startTime");
        String endTime = (String)entity.getProperty("endTime");
        String content = (String) entity.getProperty("content");
        String imgUrl = (String) entity.getProperty("imgUrl");

        Event event = new Event(userId, title, startTime, endTime, content);
        event.setEventId(String.valueOf(entity.getKey().getId()));
        event.setImgUrl(imgUrl);

        eventList.add(event);
      }
    }

    String json = convertToJsonUsingGson(eventList);
    response.setContentType("application/json; charset=UTF-8");
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
    response.getWriter().println(json);

  }
    
  private String convertToJsonUsingGson(List<Event> eventList) {
    Gson gson = new Gson();
    String json = gson.toJson(eventList);
    return json;
  }
    
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String userId = request.getParameter("userId");
    String title = request.getParameter("title"); 
    String startTimeString = request.getParameter("startTime"); 
    String endTimeString = request.getParameter("endTime");
    String content = request.getParameter("content");
    
    if (userId == "") {
      response.setStatus(400);
      response.getWriter().println("userId cannot be null");
      return;
    }
    else if (title == "") {
      response.setStatus(400);
      response.getWriter().println("title cannot be null");
      return;
    }
    else if (startTimeString == "" || endTimeString == ""){
      response.setStatus(406);
      response.getWriter().println("Time cannot be null");
      return;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    Date startTime = new Date();
    Date endTime = new Date();
    try{
      startTime = formatter.parse(startTimeString); 
      endTime = formatter.parse(endTimeString);
    }
    catch(Exception e){
      response.setStatus(406);
      response.getWriter().println("Date format error");
      return;
    }
    
    if (startTime.after(endTime)) {
      response.setStatus(406);
      response.getWriter().println("endTime cannot be before startTime");
      return;
    }
    else{
      Event event = new Event(userId, title, startTimeString, endTimeString, content);
      Entity eventEntity = new Entity("Event");
      eventEntity.setProperty("userId", event.getuserId());
      eventEntity.setProperty("title", event.getTitle());
      eventEntity.setProperty("startTime", event.getStartTime());
      eventEntity.setProperty("endTime", event.getEndTime());
      eventEntity.setProperty("content", event.getContent());

      datastore.put(eventEntity);
      String id = String.valueOf(eventEntity.getKey().getId());
      event.setEventId(id);

      Gson gson = new Gson();
      String retJson = gson.toJson(id);

      response.setContentType("application/json; charset=UTF-8");
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Allow-Credentials", "true");
      response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
      response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
      response.getWriter().println(retJson);
    }
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userId = request.getParameter("userId");
    Long eventId = Long.parseLong(request.getParameter("eventId"));
    
    response.setContentType("application/json; charset=UTF-8");
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Headers", "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
    response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");

    Filter userIdFilter = new Query.FilterPredicate ("userId", Query.FilterOperator.EQUAL, userId);
    Query query = new Query("Event").setFilter(userIdFilter);
    PreparedQuery results = datastore.prepare(query);
    Key eventKey = null;

    for (Entity entity : results.asIterable()) {
      if(eventId == entity.getKey().getId()){
        eventKey = entity.getKey();
      }
    }
    if(eventKey == null){
      response.getWriter().println("no eventId exists");
      response.setStatus(400);
    }
    else{
        datastore.delete(eventKey);
        response.setStatus(200);
    } 
  }
  
}
