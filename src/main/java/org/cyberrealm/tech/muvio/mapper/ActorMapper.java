package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.movies.Cast;
import java.util.List;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.ActorDto;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.RoleActor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface ActorMapper {

    @Mapping(target = "photo", expression = "java(cast.getProfilePath() != null ? org.cyberrealm"
            + ".tech.muvio.common.Constants.IMAGE_PATH + cast.getProfilePath() : null)")
    @Mapping(target = "name", source = "name")
    Actor toActorEntity(Cast cast);

    @Mapping(target = "photo", expression = "java(cast.getProfilePath() != null ? org.cyberrealm"
            + ".tech.muvio.common.Constants.IMAGE_PATH + cast.getProfilePath() : null)")
    @Mapping(target = "name", source = "name")
    Actor toActorEntity(info.movito.themoviedbapi.model.tv.core.credits.Cast cast);

    @Named("toActorDto")
    default List<ActorDto> toActorDto(List<RoleActor> actors) {
        return actors.stream().map(roleActor -> new ActorDto(roleActor.getRole(),
                        roleActor.getActor().getName(), roleActor.getActor().getPhoto()))
                .toList();
    }

    @Named("toListActors")
    default List<String> toSetActors(List<RoleActor> actors) {
        return actors.stream().map(roleActor -> roleActor.getActor().getName()).toList();
    }
}
