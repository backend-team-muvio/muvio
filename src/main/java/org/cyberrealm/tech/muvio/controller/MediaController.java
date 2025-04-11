package org.cyberrealm.tech.muvio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Media Management", description = "Endpoints for managing media assets such as movies "
        + "and TV shows")
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get media by ID",
            description = "Retrieve detailed information of a media resource by its "
                    + "unique identifier"
    )
    public MediaDto getMediaById(@PathVariable String id) {
        return mediaService.getMediaById(id);
    }

    @Operation(
            summary = "Get all",
            description = "Retrieve all medias in the format of MediaBaseDto"
    )
    @GetMapping("/all")
    public List<MediaBaseDto> getAll() {
        return mediaService.getAll();
    }

    @Operation(
            summary = "Count",
            description = "Retrieve  number of all medias"
    )
    @GetMapping("/count")
    public long count() {
        return mediaService.count();
    }

    @GetMapping("/vibe")
    @Operation(
            summary = "Get media by vibe criteria",
            description = "Retrieve a paginated slice of media items based on vibe "
                    + "filtering parameters"
    )
    public Slice<MediaDtoWithPoints> getAllMediaByVibe(
            @Valid MediaVibeRequestDto requestDto, Pageable pageable) {
        return mediaService.getAllMediaByVibe(requestDto, pageable);
    }

    @GetMapping("/gallery")
    @Operation(
            summary = "Get media for gallery view",
            description = "Retrieve a paginated slice of media items suitable for gallery display"
    )
    public Slice<MediaBaseDto> getAllForGallery(MediaGalleryRequestDto requestDto,
                                                Pageable pageable) {
        return mediaService.getAllForGallery(requestDto, pageable);
    }

    @GetMapping("/luck/{size}")
    @Operation(
            summary = "Get random media selection",
            description = "Retrieve a set of random media items based on the provided size"
    )
    public Set<MediaDto> getAllLuck(@PathVariable int size) {
        return mediaService.getAllLuck(size);
    }

    @GetMapping("/recommendations")
    @Operation(
            summary = "Get media recommendations",
            description = "Retrieve a paginated list of recommended media items based "
                    + "on media rating"
    )
    public Slice<MediaBaseDto> getRecommendations(Pageable pageable) {
        return mediaService.getRecommendations(pageable);
    }

    @GetMapping("/top-list/{topList}")
    @Operation(
            summary = "Get media by top list category",
            description = "Retrieve a paginated list of media items that belong to a specific "
                    + "top list category"
    )
    public Slice<MediaDtoWithCast> getMediaByTopList(@PathVariable String topList,
                                                     Pageable pageable) {
        return mediaService.findMediaByTopLists(topList, pageable);
    }

    @GetMapping("/posters")
    @Operation(
            summary = "Get all media posters",
            description = "Retrieve a paginated list of media posters"
    )
    public Slice<PosterDto> findAllPosters(Pageable pageable) {
        return mediaService.findAllPosters(pageable);
    }

    @GetMapping("/titles")
    @Operation(
            summary = "Get all media titles",
            description = "Retrieve a paginated list of media titles"
    )
    public Slice<TitleDto> findAllTitle(Pageable pageable) {
        return mediaService.findAllTitles(pageable);
    }

    @GetMapping("/titles/{title}")
    @Operation(
            summary = "Find media by title",
            description = "Retrieve a media resource that matches the provided title"
    )
    public MediaDto findByTitle(@PathVariable String title) {
        return mediaService.findByTitle(title);
    }
}
