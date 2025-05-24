package org.cyberrealm.tech.muvio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MainPageInfoDto;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/all")
    @Operation(
            summary = "Get all",
            description = "Retrieve all medias in the format of MediaBaseDto"
    )
    public List<MediaBaseDto> getAll(Pageable pageable) {
        return mediaService.getAll(pageable);
    }

    @GetMapping("/count")
    @Operation(
            summary = "Count",
            description = "Retrieve  number of all medias"
    )
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
            @Valid MediaVibeRequestDto requestDto) {
        return mediaService.getAllMediaByVibe(requestDto);
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
    public Slice<MediaBaseDto> getRecommendations(@RequestParam(defaultValue = "0") int page) {
        return mediaService.getRecommendations(page);
    }

    @GetMapping("/top-list/{topList}")
    @Operation(
            summary = "Get media by top list category",
            description = "Retrieve a paginated list of media items that belong to a specific "
                    + "top list category"
    )
    public Slice<MediaDtoWithCast> getMediaByTopList(@PathVariable String topList,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return mediaService.findMediaByTopLists(topList, page, size);
    }

    @GetMapping("/posters")
    @Operation(
            summary = "Get all media posters",
            description = "Retrieve a paginated list of media posters"
    )
    public List<PosterDto> getRandomPosters(@RequestParam(defaultValue = "70") int size) {
        return mediaService.getRandomPosters(size);
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
    public Slice<MediaBaseDto> getMediaByTitle(@PathVariable String title, Pageable pageable) {
        return mediaService.findByTitle(title, pageable);
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Provide statistical info for the main page",
            description = "Provide statistical info like the amount of media, genres, and actors"
    )
    public MainPageInfoDto getMainPageInfo() {
        return mediaService.getMainPageInfo();
    }
}
