package org.cyberrealm.tech.muvio.controller;

import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping("/{id}")
    public MediaDto getMediaById(@PathVariable String id) {
        return mediaService.getMediaById(id);
    }

    @PostMapping
    public Media saveMedia(@RequestBody Media media) {
        return mediaService.saveMedia(media);
    }

    @DeleteMapping("/{id}")
    public void deleteMediaById(@PathVariable String id) {
        mediaService.deleteMediaById(id);
    }

    @PutMapping("/{id}")
    public Media updateMedia(@PathVariable String id, @RequestBody Media media) {
        return mediaService.updateMedia(id, media);
    }

    @GetMapping("/vibe")
    public Slice<MediaBaseDtoWithPoints> getAllMediaByVibe(
            @RequestBody @Valid MediaVibeRequestDto requestDto, Pageable pageable) {
        return mediaService.getAllMediaByVibe(requestDto, pageable);
    }

    @GetMapping("/gallery")
    public Slice<MediaBaseDto> getAllForGallery(@RequestBody MediaGalleryRequestDto requestDto,
                                                Pageable pageable) {
        return mediaService.getAllForGallery(requestDto, pageable);
    }

    @GetMapping("/luck/{size}")
    public Set<MediaBaseDto> getAllLuck(@PathVariable int size) {
        return mediaService.getAllLuck(size);
    }

    @GetMapping("/recommendations")
    public Slice<MediaBaseDto> getRecommendations(Pageable pageable) {
        return mediaService.getRecommendations(pageable);
    }

    @GetMapping("/top-list/{topList}")
    public Slice<MediaDtoWithCast> getMediaByTopList(@PathVariable String topList,
                                                     Pageable pageable) {
        return mediaService.findMediaByTopLists(topList, pageable);
    }

    @GetMapping("/posters")
    public Slice<PosterDto> findAllPosters(Pageable pageable) {
        return mediaService.findAllPosters(pageable);
    }

    @GetMapping("/titles")
    public Slice<TitleDto> findAllTitle(Pageable pageable) {
        return mediaService.findAllTitles(pageable);
    }

    @GetMapping("/titles/{title}")
    public MediaDto findByTitle(@PathVariable String title) {
        return mediaService.findByTitle(title);
    }
}
