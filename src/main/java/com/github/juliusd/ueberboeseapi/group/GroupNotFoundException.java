package com.github.juliusd.ueberboeseapi.group;

public class GroupNotFoundException extends RuntimeException {
  private final Long groupId;

  public GroupNotFoundException(Long groupId) {
    super("Device Group not found ");
    this.groupId = groupId;
  }

  public Long getGroupId() {
    return groupId;
  }
}
