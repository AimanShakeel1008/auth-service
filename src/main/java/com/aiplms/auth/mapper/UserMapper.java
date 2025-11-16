package com.aiplms.auth.mapper;

import com.aiplms.auth.dto.v1.RegisterRequestDto;
import com.aiplms.auth.dto.v1.UserResponseDto;
import com.aiplms.auth.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert registration request into User entity (password hashing is done in service).
     * ID must be assigned manually in service: user.setId(UUID.randomUUID()).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true) // default roles assigned in service layer
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "failedLoginCount", constant = "0")
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(RegisterRequestDto dto);

    /**
     * Convert User entity to UserResponseDto.
     * We skip `id` because entity uses UUID and DTO expects Long.
     */
    @Mapping(target = "id", ignore = true)
    UserResponseDto toUserResponse(User user);
}


