package com.pathfinder.model.dto;

import com.pathfinder.model.enumerated.LevelEnum;

public record UserPermissionsDetailsDto(Long id,
                                        String email,
                                        String fullName,
                                        String username,
                                        LevelEnum level) {
}
