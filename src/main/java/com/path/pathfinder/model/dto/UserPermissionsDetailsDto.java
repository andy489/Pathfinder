package com.path.pathfinder.model.dto;

import com.path.pathfinder.model.enumerated.LevelEnum;

public record UserPermissionsDetailsDto(Long id,
                                        String email,
                                        String fullName,
                                        String username,
                                        LevelEnum level) {
}
