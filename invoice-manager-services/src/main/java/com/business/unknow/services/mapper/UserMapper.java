package com.business.unknow.services.mapper;

import com.business.unknow.model.dto.services.UserDto;
import com.business.unknow.services.entities.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface UserMapper {
  @Mappings({
    @Mapping(target = "menu", ignore = true),
    @Mapping(target = "urlPicture", ignore = true),
    @Mapping(target = "name", ignore = true),
    @Mapping(target = "roles", ignore = true),
  })
  UserDto getUserDtoFromentity(User entity);

  List<UserDto> getUsersDtoFromEntities(List<User> entities);

  @Mappings({
    @Mapping(target = "fechaActualizacion", ignore = true),
    @Mapping(target = "id", ignore = true),
    @Mapping(target = "fechaCreacion", ignore = true)
  })
  User getUserEntityFroDto(UserDto dto);
}
