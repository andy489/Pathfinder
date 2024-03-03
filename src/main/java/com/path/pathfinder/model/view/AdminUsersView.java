package com.path.pathfinder.model.view;

import com.path.pathfinder.model.dto.UserPermissionsDetailsDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class AdminUsersView {

    private Integer count;

    private Map<String, List<UserPermissionsDetailsDto>> usersDetail;
}
