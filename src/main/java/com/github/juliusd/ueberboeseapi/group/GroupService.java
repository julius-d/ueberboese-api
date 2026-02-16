package com.github.juliusd.ueberboeseapi.group;

import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRoleApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupUpdateRequestApiDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
  private final DeviceGroupRepository deviceGroupRepository;

  @Transactional
  public DeviceGroup createGroup(String accountId, GroupRequestApiDto request) {
    log.info(
        "Creating group for account={}, master={}, name={}",
        accountId,
        request.getMasterDeviceId(),
        request.getName());

    List<GroupRoleApiDto> roles = request.getRoles().getGroupRole();

    String leftDeviceId =
        roles.stream()
            .filter(role -> GroupRoleApiDto.RoleEnum.LEFT.equals(role.getRole()))
            .map(GroupRoleApiDto::getDeviceId)
            .findFirst()
            .orElseThrow(() -> new DeviceGroupException("Group must have a device with LEFT role"));

    String rightDeviceId =
        roles.stream()
            .filter(role -> GroupRoleApiDto.RoleEnum.RIGHT.equals(role.getRole()))
            .map(GroupRoleApiDto::getDeviceId)
            .findFirst()
            .orElseThrow(
                () -> new DeviceGroupException("Group must have a device with RIGHT role"));

    // Validate master device is either left or right
    String masterDeviceId = request.getMasterDeviceId();
    if (!masterDeviceId.equals(leftDeviceId) && !masterDeviceId.equals(rightDeviceId)) {
      throw new DeviceGroupException(
          "Master device must be either the LEFT or RIGHT device in the group");
    }

    // Validate master device not already in a group
    Optional<DeviceGroup> masterExistingGroup =
        deviceGroupRepository.findByDeviceId(masterDeviceId);
    if (masterExistingGroup.isPresent()) {
      throw new DeviceAlreadyInGroupException(masterDeviceId);
    }

    // Validate left device not already in a group
    Optional<DeviceGroup> leftExistingGroup = deviceGroupRepository.findByDeviceId(leftDeviceId);
    if (leftExistingGroup.isPresent()) {
      throw new DeviceAlreadyInGroupException(leftDeviceId);
    }

    // Validate right device not already in a group
    Optional<DeviceGroup> rightExistingGroup = deviceGroupRepository.findByDeviceId(rightDeviceId);
    if (rightExistingGroup.isPresent()) {
      throw new DeviceAlreadyInGroupException(rightDeviceId);
    }

    // Create new group
    var now = OffsetDateTime.now().withNano(0);
    DeviceGroup group =
        DeviceGroup.builder()
            .accountId(accountId)
            .masterDeviceId(masterDeviceId)
            .name(request.getName())
            .leftDeviceId(leftDeviceId)
            .rightDeviceId(rightDeviceId)
            .createdOn(now)
            .updatedOn(now)
            .build();

    DeviceGroup saved = deviceGroupRepository.save(group);
    log.info("Created group id={} for account={}", saved.id(), accountId);
    return saved;
  }

  public Optional<DeviceGroup> getGroupByDeviceId(String accountId, String deviceId) {
    return deviceGroupRepository.findByAccountIdAndDeviceId(accountId, deviceId);
  }

  @Transactional
  public void deleteGroup(String accountId, Long groupId) {
    log.info("Deleting group id={} for account={}", groupId, accountId);

    DeviceGroup group =
        deviceGroupRepository
            .findByIdAndAccountId(groupId, accountId)
            .orElseThrow(() -> new GroupNotFoundException(groupId));

    deviceGroupRepository.delete(group);
    log.info("Deleted group id={} for account={}", groupId, accountId);
  }

  @Transactional
  public DeviceGroup updateGroup(String accountId, Long groupId, GroupUpdateRequestApiDto request) {
    log.info(
        "Updating group id={} for account={}, newMaster={}, newName={}",
        groupId,
        accountId,
        request.getMasterDeviceId(),
        request.getName());

    DeviceGroup group =
        deviceGroupRepository
            .findByIdAndAccountId(groupId, accountId)
            .orElseThrow(() -> new GroupNotFoundException(groupId));

    // Validate new master device is either left or right device in the group
    String newMasterDeviceId = request.getMasterDeviceId();
    if (!newMasterDeviceId.equals(group.leftDeviceId())
        && !newMasterDeviceId.equals(group.rightDeviceId())) {
      throw new DeviceGroupException("Master device must be LEFT or RIGHT device in the group");
    }

    // Update group using toBuilder pattern (records are immutable)
    DeviceGroup updatedGroup =
        group.toBuilder()
            .masterDeviceId(newMasterDeviceId)
            .name(request.getName())
            .updatedOn(OffsetDateTime.now().withNano(0))
            .build();

    DeviceGroup saved = deviceGroupRepository.save(updatedGroup);
    log.info("Updated group id={} for account={}", groupId, accountId);
    return saved;
  }
}
