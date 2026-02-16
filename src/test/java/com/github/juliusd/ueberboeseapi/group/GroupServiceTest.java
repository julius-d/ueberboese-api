package com.github.juliusd.ueberboeseapi.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRoleApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRolesApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupUpdateRequestApiDto;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GroupServiceTest extends TestBase {

  @Autowired private GroupService groupService;
  @Autowired private DeviceGroupRepository deviceGroupRepository;

  @Test
  void createGroup_shouldCreateNewGroup() {
    // Given
    String accountId = "6921042";
    String masterDeviceId = "587A628A4042";
    String leftDeviceId = "587A628A4042";
    String rightDeviceId = "44EAD8A18888";

    var request =
        new GroupRequestApiDto()
            .masterDeviceId(masterDeviceId)
            .name("Living Room Stereo")
            .roles(
                new GroupRolesApiDto()
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId(leftDeviceId)
                            .role(GroupRoleApiDto.RoleEnum.LEFT))
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId(rightDeviceId)
                            .role(GroupRoleApiDto.RoleEnum.RIGHT)));

    // When
    DeviceGroup result = groupService.createGroup(accountId, request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.id()).isNotNull();
    assertThat(result.accountId()).isEqualTo(accountId);
    assertThat(result.masterDeviceId()).isEqualTo(masterDeviceId);
    assertThat(result.name()).isEqualTo("Living Room Stereo");
    assertThat(result.leftDeviceId()).isEqualTo(leftDeviceId);
    assertThat(result.rightDeviceId()).isEqualTo(rightDeviceId);
    assertThat(result.createdOn()).isAfter(OffsetDateTime.parse("2026-01-01T10:15:30+01:00"));
    assertThat(result.updatedOn()).isEqualTo(result.createdOn());
  }

  @Test
  void createGroup_shouldThrowException_whenMasterDeviceAlreadyInGroup() {
    // Given - existing group
    String accountId = "6921042";
    String masterDeviceId = "587A628A4042";
    givenExistingGroup(accountId, masterDeviceId, "587A628A4042", "44EAD8A18888");

    // When/Then - try to create another group with same master device
    var request =
        new GroupRequestApiDto()
            .masterDeviceId(masterDeviceId)
            .name("Another Group")
            .roles(
                new GroupRolesApiDto()
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId(masterDeviceId)
                            .role(GroupRoleApiDto.RoleEnum.LEFT))
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId("DEVICE456")
                            .role(GroupRoleApiDto.RoleEnum.RIGHT)));

    assertThatThrownBy(() -> groupService.createGroup(accountId, request))
        .isInstanceOf(DeviceAlreadyInGroupException.class)
        .hasMessageContaining("587A628A4042");
  }

  @Test
  void createGroup_shouldThrowException_whenLeftDeviceAlreadyInGroup() {
    // Given - existing group
    String accountId = "6921042";
    givenExistingGroup(accountId, "LEFT1", "LEFT1", "RIGHT1");

    // When/Then - try to create another group with same left device
    var request =
        new GroupRequestApiDto()
            .masterDeviceId("RIGHT2")
            .name("Another Group")
            .roles(
                new GroupRolesApiDto()
                    .addGroupRoleItem(
                        new GroupRoleApiDto().deviceId("LEFT1").role(GroupRoleApiDto.RoleEnum.LEFT))
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId("RIGHT2")
                            .role(GroupRoleApiDto.RoleEnum.RIGHT)));

    assertThatThrownBy(() -> groupService.createGroup(accountId, request))
        .isInstanceOf(DeviceAlreadyInGroupException.class)
        .hasMessageContaining("LEFT1");
  }

  @Test
  void createGroup_shouldThrowException_whenRightDeviceAlreadyInGroup() {
    // Given - existing group
    String accountId = "6921042";
    givenExistingGroup(accountId, "LEFT1", "LEFT1", "RIGHT1");

    // When/Then - try to create another group with same right device
    var request =
        new GroupRequestApiDto()
            .masterDeviceId("LEFT2")
            .name("Another Group")
            .roles(
                new GroupRolesApiDto()
                    .addGroupRoleItem(
                        new GroupRoleApiDto().deviceId("LEFT2").role(GroupRoleApiDto.RoleEnum.LEFT))
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId("RIGHT1")
                            .role(GroupRoleApiDto.RoleEnum.RIGHT)));

    assertThatThrownBy(() -> groupService.createGroup(accountId, request))
        .isInstanceOf(DeviceAlreadyInGroupException.class)
        .hasMessageContaining("RIGHT1");
  }

  @Test
  void getGroupByDeviceId_shouldReturnGroup_whenDeviceIsLeftInGroup() {
    // Given
    String accountId = "6921042";
    String leftDeviceId = "LEFT1";
    DeviceGroup existing = givenExistingGroup(accountId, "MASTER1", leftDeviceId, "RIGHT1");

    // When
    Optional<DeviceGroup> result = groupService.getGroupByDeviceId(accountId, leftDeviceId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(existing.id());
  }

  @Test
  void getGroupByDeviceId_shouldReturnGroup_whenDeviceIsRightInGroup() {
    // Given
    String accountId = "6921042";
    String rightDeviceId = "RIGHT1";
    DeviceGroup existing = givenExistingGroup(accountId, "MASTER1", "LEFT1", rightDeviceId);

    // When
    Optional<DeviceGroup> result = groupService.getGroupByDeviceId(accountId, rightDeviceId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo(existing.id());
  }

  @Test
  void getGroupByDeviceId_shouldReturnEmpty_whenDeviceNotInGroup() {
    // When
    Optional<DeviceGroup> result = groupService.getGroupByDeviceId("6921042", "UNKNOWN123");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void createGroup_shouldThrowException_whenMasterDeviceIsNotLeftOrRight() {
    // Given
    var request =
        new GroupRequestApiDto()
            .masterDeviceId("DIFFERENT_MASTER")
            .name("Invalid Group")
            .roles(
                new GroupRolesApiDto()
                    .addGroupRoleItem(
                        new GroupRoleApiDto().deviceId("LEFT1").role(GroupRoleApiDto.RoleEnum.LEFT))
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId("RIGHT1")
                            .role(GroupRoleApiDto.RoleEnum.RIGHT)));

    // When/Then
    assertThatThrownBy(() -> groupService.createGroup("6921042", request))
        .isInstanceOf(DeviceGroupException.class)
        .hasMessageContaining("Master device must be either the LEFT or RIGHT device");
  }

  @Test
  void createGroup_shouldThrowException_whenDeviceAlreadyInGroupInDifferentAccount() {
    // Given - group in account 1
    givenExistingGroup("account1", "LEFT1", "LEFT1", "RIGHT1");

    // When/Then - trying to create group in account 2 with same device IDs should fail
    // because devices can only belong to ONE group globally (not per account)
    var request =
        new GroupRequestApiDto()
            .masterDeviceId("RIGHT2")
            .name("Account 2 Group")
            .roles(
                new GroupRolesApiDto()
                    .addGroupRoleItem(
                        new GroupRoleApiDto().deviceId("LEFT1").role(GroupRoleApiDto.RoleEnum.LEFT))
                    .addGroupRoleItem(
                        new GroupRoleApiDto()
                            .deviceId("RIGHT2")
                            .role(GroupRoleApiDto.RoleEnum.RIGHT)));

    assertThatThrownBy(() -> groupService.createGroup("account2", request))
        .isInstanceOf(DeviceAlreadyInGroupException.class)
        .hasMessageContaining("LEFT1");
  }

  @Test
  void deleteGroup_shouldDeleteExistingGroup() {
    // Given
    String accountId = "6921042";
    DeviceGroup existing = givenExistingGroup(accountId, "MASTER1", "LEFT1", "RIGHT1");

    // When
    groupService.deleteGroup(accountId, existing.id());

    // Then
    assertThat(deviceGroupRepository.findById(existing.id())).isEmpty();
  }

  @Test
  void deleteGroup_shouldThrowException_whenGroupNotFound() {
    // When/Then
    assertThatThrownBy(() -> groupService.deleteGroup("6921042", 999999L))
        .isInstanceOf(GroupNotFoundException.class);
  }

  @Test
  void deleteGroup_shouldThrowException_whenGroupBelongsToDifferentAccount() {
    // Given
    DeviceGroup existing = givenExistingGroup("account1", "MASTER1", "LEFT1", "RIGHT1");

    // When/Then
    assertThatThrownBy(() -> groupService.deleteGroup("account2", existing.id()))
        .isInstanceOf(GroupNotFoundException.class);
  }

  @Test
  void updateGroup_shouldUpdateNameAndMaster() {
    // Given
    String accountId = "6921042";
    DeviceGroup existing = givenExistingGroup(accountId, "LEFT1", "LEFT1", "RIGHT1");
    OffsetDateTime originalUpdatedOn = existing.updatedOn();

    var request = new GroupUpdateRequestApiDto().masterDeviceId("RIGHT1").name("Updated Stereo");

    // When
    DeviceGroup result = groupService.updateGroup(accountId, existing.id(), request);

    // Then
    assertThat(result.id()).isEqualTo(existing.id());
    assertThat(result.masterDeviceId()).isEqualTo("RIGHT1");
    assertThat(result.name()).isEqualTo("Updated Stereo");
    assertThat(result.leftDeviceId()).isEqualTo("LEFT1");
    assertThat(result.rightDeviceId()).isEqualTo("RIGHT1");
    assertThat(result.updatedOn()).isAfterOrEqualTo(originalUpdatedOn);
    assertThat(result.createdOn()).isEqualTo(existing.createdOn());
  }

  @Test
  void updateGroup_shouldUpdateOnlyName() {
    // Given
    String accountId = "6921042";
    DeviceGroup existing = givenExistingGroup(accountId, "LEFT1", "LEFT1", "RIGHT1");

    var request = new GroupUpdateRequestApiDto().masterDeviceId("LEFT1").name("New Name");

    // When
    DeviceGroup result = groupService.updateGroup(accountId, existing.id(), request);

    // Then
    assertThat(result.masterDeviceId()).isEqualTo("LEFT1");
    assertThat(result.name()).isEqualTo("New Name");
  }

  @Test
  void updateGroup_shouldThrowException_whenGroupNotFound() {
    // Given
    var request = new GroupUpdateRequestApiDto().masterDeviceId("LEFT1").name("New Name");

    // When/Then
    assertThatThrownBy(() -> groupService.updateGroup("6921042", 999999L, request))
        .isInstanceOf(GroupNotFoundException.class);
  }

  @Test
  void updateGroup_shouldThrowException_whenMasterDeviceInvalid() {
    // Given
    String accountId = "6921042";
    DeviceGroup existing = givenExistingGroup(accountId, "LEFT1", "LEFT1", "RIGHT1");

    var request = new GroupUpdateRequestApiDto().masterDeviceId("INVALID_DEVICE").name("New Name");

    // When/Then
    assertThatThrownBy(() -> groupService.updateGroup(accountId, existing.id(), request))
        .isInstanceOf(DeviceGroupException.class)
        .hasMessageContaining("Master device must be LEFT or RIGHT device");
  }

  @Test
  void updateGroup_shouldThrowException_whenGroupBelongsToDifferentAccount() {
    // Given
    DeviceGroup existing = givenExistingGroup("account1", "LEFT1", "LEFT1", "RIGHT1");

    var request = new GroupUpdateRequestApiDto().masterDeviceId("RIGHT1").name("New Name");

    // When/Then
    assertThatThrownBy(() -> groupService.updateGroup("account2", existing.id(), request))
        .isInstanceOf(GroupNotFoundException.class);
  }

  private DeviceGroup givenExistingGroup(
      String accountId, String masterDeviceId, String leftDeviceId, String rightDeviceId) {
    var now = OffsetDateTime.now().withNano(0);
    DeviceGroup group =
        DeviceGroup.builder()
            .accountId(accountId)
            .masterDeviceId(masterDeviceId)
            .name("Existing Group")
            .leftDeviceId(leftDeviceId)
            .rightDeviceId(rightDeviceId)
            .createdOn(now)
            .updatedOn(now)
            .build();
    return deviceGroupRepository.save(group);
  }
}
