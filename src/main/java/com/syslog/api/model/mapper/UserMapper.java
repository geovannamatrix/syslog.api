package com.syslog.api.model.mapper;

import com.syslog.api.model.dtos.CredentialDTO;
import com.syslog.api.model.dtos.UserRequestDTO;
import com.syslog.api.model.dtos.UserUpdateRequestDTO;
import com.syslog.api.model.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    User toEntity(CredentialDTO credential);

    CredentialDTO toModel(User user);

    User toEntity(UserRequestDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User toUpdateEntity(@MappingTarget User user, UserRequestDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User toUpdateEntity(@MappingTarget User user, UserUpdateRequestDTO request);
}
