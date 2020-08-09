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
 * creates page for user register and has following 5 methods 
 * - doGet 
 * - doPost
 * - messages
 * - checkInput
 * - checkUsername
 */ 
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
  
  public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 
  private boolean registerCheck = false;  // true if the entered username is registered already
  private boolean inputCheck = false; // true if inputs are empty 

  /**  
   * creates the register form
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
  */ 
  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter writeOut = response.getWriter();
    writeOut.println("<h1>Register</h1>");
    response.setContentType("text/html");
    if (inputCheck){
      messages(response, 1); 
    }
    else if (registerCheck){
      messages(response, 2); 
    } 
    registerCheck = false; 
    inputCheck = false; 
    // ask for register info (Post method) 
    writeOut.println("<form method=\"POST\" action=\"/register\">"); 
    writeOut.println("<p>username = </p>"); 
    writeOut.println("<input name=\"username\"/>"); 
    writeOut.println("<p>password = </p>");
    writeOut.println("<input name=\"password\"/>"); 
    writeOut.println("<p>email = </p>");
    writeOut.println("<input name=\"email\"/>"); 
    writeOut.println("<br/>"); 
    writeOut.println("<button>Submit</button>"); 
    writeOut.println("</form>"); 
    writeOut.println("<p>If you have already registered, please login</p>"); 
    writeOut.println("<p>** You will need to login after you register!**"); 
    writeOut.println("<p>Login <a href=/login>here</a>.</p>"); 
  }

  /**
   * asks for register  
   * @param request (HttpServletRequest)
   * @param response (HttpServletResponse) 
  */ 
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
    String username = request.getParameter("username"); // unique
    String password = request.getParameter("password"); 
    String email = request.getParameter("email"); // unique 
    
    // username, password, and email cannot be empty 
    // true if three inputs are empty 
    inputCheck = checkInput(username, password, email); 

    // check if username or email already exist in database, avoiding replications
    // true if username and email is registered already  
    registerCheck = checkUsername(request, response, username, email); 

    if (inputCheck || registerCheck){
      System.out.println("Entered inputs are empty"); 
      response.sendRedirect("/register"); 
      return;  
    }
    
    // store the username 
    Entity taskEntity = new Entity("userInfo");
    taskEntity.setProperty("username", username); 
    taskEntity.setProperty("password", password); 
    taskEntity.setProperty("email", email); 
    datastore.put(taskEntity); 
    System.out.println("taskEntity is" + taskEntity); 

    // go to login page after register 
    response.sendRedirect("/login"); 
  }
  
  /**
   * outprint indication messages if entered values are invalid 
   * @param response (HttpServletResponse)
   * @param type (int) 1 indicates inputs are empty string; 2 indicates inputs are registered 
  */ 
  private void messages(HttpServletResponse response, int type) throws IOException{
    PrintWriter writeOut = response.getWriter();
    if (type == 1){
      writeOut.println("<p>username, password, and email cannot be empty</p>"); 
    } else if (type == 2){
      writeOut.println("<p>The username or email has already been registered</p>"); 
      writeOut.println("<p>Please try another username/email or press 'login'</p>"); 
    }
  }

  /**
   * check if entered username, password, or email is empty string or null 
   * @param username (String) entered username 
   * @param password (String) entered password 
   * @param email (String) entered email 
  */ 
  private boolean checkInput(String username, String password, String email){
    if (username.equals("")||username == null){
      return true; 
    } else if (password.equals("")||password == null){
      return true;
    } else if (email.equals("")||email == null){
      return true;
    }
    // three inputs are neither null nor empty string 
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
    
    // TEST 
    System.out.println("At checkUsername R username= " + username); 
    System.out.println("At checkUsername R username= " + email); 
    System.out.println("At checkUsername R results= " + results); 

    String nameEntity = null; 
    String emailEntity = null; 

    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("username");
      String emaile = (String) entity.getProperty("email");
      nameEntity = name; 
      emailEntity = emaile; 
      // TEST 
      System.out.println("At checkUsername R entity= " + entity);
    }
    // entities contain values in database 
    if ((nameEntity == null)|(emailEntity == null)){
      // not registered 
      System.out.println("Not registered"); 
      return false; 
    }
    // registered 
    System.out.println("Registered"); 
    return true; 
  }
}


