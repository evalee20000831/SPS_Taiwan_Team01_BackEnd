// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
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
import java.util.Locale;
import java.text.*;
import com.sps.app.Event;
import java.lang.*;

@WebServlet("/")
public class EventServlet extends HttpServlet {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String userId = getParameter(request, "userId","");
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

        Event event = new Event(userId, title, startTime, endTime, content);
        eventList.add(event);
      }
    }

    String json = convertToJsonUsingGson(eventList);
    response.setContentType("application/json; charset=UTF-8");
    response.getWriter().println(json);
  }
    
  private String convertToJsonUsingGson(List<Event> eventList) {
    Gson gson = new Gson();
    String json = gson.toJson(eventList);
    return json;
  }
    
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String userId = getParameter(request, "userId", "");
    String title = getParameter(request, "title", ""); 
    String startTimeString = getParameter(request, "startTime", ""); 
    String endTimeString = getParameter(request, "endTime", "");
    String content = getParameter(request, "content", "");
    
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

    String id = "";
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
      id = String.valueOf(eventEntity.getKey().getId());

      Gson gson = new Gson();
      String retJson = gson.toJson(id);

      response.setContentType("application/json; charset=UTF-8");
      response.getWriter().println(retJson);
    }
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userId = getParameter(request, "userId", "");
    Long eventId = Long.parseLong(request.getParameter("eventId"));
    
    response.setContentType("application/json; charset=UTF-8");

    Filter userIdFilter = new Query.FilterPredicate ("userId", Query.FilterOperator.EQUAL, userId);
    Query query = new Query("Event").setFilter(userIdFilter);
    PreparedQuery results = datastore.prepare(query);
    Key eventKey = null;

    for (Entity entity : results.asIterable()) {
      response.getWriter().println(entity.getKey().getId());
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
        response.setStatus(204);
    } 
  }
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
