package com.aiplms.auth.mapper;

import com.aiplms.auth.config.MapStructConfig;
import com.aiplms.auth.dto.v1.RegisterRequestDto;
import com.aiplms.auth.dto.v1.UserResponseDto;
import com.aiplms.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(config = MapStructConfig.class, componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // map RegisterRequestDto -> User (do not map password -> passwordHash directly here;
    // prefer service to hash and set passwordHash; if you must, MapStruct can map raw password to passwordHash via custom methods)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "emailVerified", constant = "false")
    User toUser(RegisterRequestDto dto);

    // map User -> UserResponseDto
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "emailVerified", source = "emailVerified")
    UserResponseDto toUserResponseDto(User user);
}

