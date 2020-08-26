
package com.sps.app;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.text.*;

public class Event {
    private String userId;
    private String title;
    private String startTime;
    private String endTime;
    private String content;
    private String eventId;
    private String imgUrl;

  /**
   * Creates a new event.
   *
   * @param userId user's Id. Must be non-null.
   * @param title The human-readable name for the event. Must be non-null.
   * @param timestamp The time when the event takes place. Must be non-null.
   * @param content The task content. Can be null.
   */
  public Event(String userId, String title, String startTime, String endTime, String content){
    this.startTime = startTime;
    this.endTime = endTime;
    this.userId = userId;
    this.title = title;
    this.content = content;
  }
  /**
   * Returns the human-readable name for this event.
   */
  public String getTitle() {
    return title;
  }
  public String getuserId() {
    return userId;
  }
  public String getStartTime() {
    return startTime;
  }
  public String getEndTime() {
    return endTime;
  }
  public String getContent() {
    return content;
  }
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }
  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }
}