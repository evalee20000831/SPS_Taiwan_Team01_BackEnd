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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String userid = getParameter(request, "userid","");
    if(userid == null){
        throw new IllegalArgumentException("userid cannot be empty");
    }
    List<Event> eventList = new ArrayList<>();
    Query query = new Query("event");
    // .addSort("timestamp", SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      if(entity.getProperty("userid") == userid){
        String title = (String) entity.getProperty("title");
        Date startTime = (Date) entity.getProperty("startTime");
        Date endTime = (Date) entity.getProperty("endTime");
        String content = (String) entity.getProperty("content");

        Event event = new Event(userid, title, startTime, endTime, content);
        eventList.add(event);
      }
    }

    String json = convertToJsonUsingGson(eventList);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
    
  private String convertToJsonUsingGson(List<Event> eventList) {
    Gson gson = new Gson();
    String json = gson.toJson(eventList);
    return json;
  }
    
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String userid = getParameter(request, "userid", "");
    String title = getParameter(request, "title", ""); 
    String startTimeString = getParameter(request, "startTime", ""); 
    String endTimeString = getParameter(request, "endTime", "");
    String content = getParameter(request, "content", "");
    
    Event event = new Event(userid, title, startTimeString, endTimeString, content);

    Entity eventEntity = new Entity("Event");
    eventEntity.setProperty("userid", event.getUserid());
    eventEntity.setProperty("title", event.getTitle());
    eventEntity.setProperty("startTime", event.getStartTime());
    eventEntity.setProperty("endTime", event.getEndTime());
    eventEntity.setProperty("content", event.getContent());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);

    int id = (int) eventEntity.getKey().getId();

    Gson gson = new Gson();
    String retJson = gson.toJson(id);

    response.setContentType("application/json;");
    response.getWriter().println(retJson);
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userid = getParameter(request, "userid", "");
    int eventid = Integer.parseInt(getParameter(request, "eventid", ""));
    
    Key eventKey = KeyFactory.createKey("Event", eventid);
    datastore.delete(eventKey);

  }
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
