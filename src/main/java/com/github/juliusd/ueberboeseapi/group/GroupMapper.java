package com.github.juliusd.ueberboeseapi.group;

import com.github.juliusd.ueberboeseapi.generated.dtos.GroupResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRoleApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRolesApiDto;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

  public GroupResponseApiDto toResponseDto(DeviceGroup group) {
    var response =
        new GroupResponseApiDto()
            .id(String.valueOf(group.id()))
            .masterDeviceId(group.masterDeviceId())
            .name(group.name());

    // Build roles from left and right device fields
    var roles = new GroupRolesApiDto();

    var leftRole =
        new GroupRoleApiDto().deviceId(group.leftDeviceId()).role(GroupRoleApiDto.RoleEnum.LEFT);
    roles.addGroupRoleItem(leftRole);

    var rightRole =
        new GroupRoleApiDto().deviceId(group.rightDeviceId()).role(GroupRoleApiDto.RoleEnum.RIGHT);
    roles.addGroupRoleItem(rightRole);

    response.setRoles(roles);
    return response;
  }
}
