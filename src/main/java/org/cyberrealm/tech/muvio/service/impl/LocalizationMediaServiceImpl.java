package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.SIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.mapper.LocalizationMediaMapper;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.repository.LocalizationMediaRepository;
import org.cyberrealm.tech.muvio.repository.MediaRepository;
import org.cyberrealm.tech.muvio.service.LocalizationMediaService;
import org.cyberrealm.tech.muvio.service.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalizationMediaServiceImpl implements LocalizationMediaService {
    private final LocalizationMediaRepository localizationMediaRepository;
    private final LocalizationMediaMapper localizationMediaMapper;
    private final PaginationUtil paginationUtil;
    private final MediaRepository mediaRepository;

    @Override
    public MediaDto findById(String id, Media media) {
        return localizationMediaMapper.toMediaDto(localizationMediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Localization media not found: " + id)), media);
    }

    @Override
    public Slice<MediaBaseDto> findByTitle(String title, Pageable pageable, String lang) {
        final List<LocalizationMedia> mediaBaseDtoSlice = localizationMediaRepository
                .findByTitle(title, pageable, lang);
        final Map<String, Media> mediaMap = mediaRepository.findAllById(mediaBaseDtoSlice.stream()
                        .map(mediaBaseDto -> mediaBaseDto.getId().substring(lang.length()))
                        .collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Media::getId, Function.identity()));
        final List<MediaBaseDto> mediaBaseDtoList = mediaBaseDtoSlice.stream()
                .map(localizationMedia -> localizationMediaMapper.toMediaBaseDto(mediaMap
                                .get(localizationMedia.getId().substring(lang.length())),
                        localizationMedia)).toList();
        return new SliceImpl<>(mediaBaseDtoList, pageable, !mediaBaseDtoList.isEmpty());
    }

    @Override
    public Slice<MediaBaseDto> getAllForGalleryLocalization(
            List<Media> medias, String title, Pageable pageable,
            String lang) {
        final Map<String, LocalizationMedia> localizationMediaMap =
                localizationMediaRepository
                        .getAllForGalleryLocalization(getMediaIds(medias, lang, Media::getId),
                                title).stream()
                        .collect(Collectors.toMap(LocalizationMedia::getId, Function.identity()));
        final List<MediaBaseDto> listMedias = new ArrayList<>();
        medias.forEach(media -> {
            LocalizationMedia localizationMedia = localizationMediaMap.get(lang + media.getId());
            if (localizationMedia != null) {
                listMedias.add(localizationMediaMapper
                        .toMediaBaseDto(media, localizationMedia));
            }
        });
        return paginationUtil.paginateList(pageable, listMedias);
    }

    @Override
    public List<PosterDto> getRandomPosters(int size, String lang) {
        return localizationMediaRepository.getRandomPosters(size, lang);
    }

    @Override
    public List<MediaDtoWithPoints> getAllMediaByVibe(
            List<Media> mediaList, Set<String> categories, String lang) {
        final Map<String, LocalizationMedia> localizationMediaMap =
                getLocalizationMediaMap(getMediaIds(mediaList, lang, Media::getId));
        final List<MediaDtoWithPoints> mediasWithPoints = new ArrayList<>();
        mediaList.forEach(media -> mediasWithPoints.add(localizationMediaMapper
                .toMediaDtoWithPoints(media, categories,
                        localizationMediaMap.get(lang + media.getId()))));
        return mediasWithPoints;
    }

    @Override
    public Set<MediaDto> getAllLuck(Set<MediaDtoFromDb> allLuck, String lang) {
        final Map<String, LocalizationMedia> localizationMediaMap =
                getLocalizationMediaMap(getMediaIds(allLuck, lang, MediaDtoFromDb::id));
        return allLuck.stream().map(mediaDtoFromDb -> localizationMediaMapper
                .toMediaDtoFromDto(localizationMediaMap.get(lang + mediaDtoFromDb.id()),
                        mediaDtoFromDb)).collect(Collectors.toSet());
    }

    @Override
    public Slice<MediaBaseDto> getRecommendations(
            Page<MediaBaseDto> mediaPage, PageRequest pageRequest, String lang) {
        final Map<String, LocalizationMedia> localizationMap =
                     getLocalizationMediaMap(getMediaIds(mediaPage.getContent(),
                             lang, MediaBaseDto::getId));
        final List<MediaBaseDto> localizedDtoList = new ArrayList<>();
        mediaPage.forEach(mediaBaseDto -> localizedDtoList.add(localizationMediaMapper
                .toLocalizateMediaBaseDto(localizationMap
                        .get(lang + mediaBaseDto.getId()), mediaBaseDto)));
        return localizedDtoList.size() < SIX ? null
                : new SliceImpl<>(localizedDtoList, pageRequest, true);
    }

    @Override
    public List<MediaDtoWithCast> findMediaByTopLists(
            Slice<MediaDtoWithCastFromDb> media, String lang) {
        final Map<String, LocalizationMedia> localizationMediaMap = getLocalizationMediaMap(
                getMediaIds(media.getContent(), lang, MediaDtoWithCastFromDb::id));
        return media.stream().map(mediaDtoFromDb -> localizationMediaMapper.toMediaDtoWithCast(
                                localizationMediaMap.get(lang + mediaDtoFromDb.id()),
                        mediaDtoFromDb)).toList();
    }

    @Override
    public Slice<TitleDto> findAllTitles(Pageable pageable, String lang) {
        return localizationMediaRepository.findAllTitles(pageable, lang);
    }

    @Override
    public List<MediaBaseDto> getAll(List<MediaBaseDto> mediaBaseList, String lang) {
        final Map<String, LocalizationMedia> localizationMediaMap =
                    getLocalizationMediaMap(getMediaIds(mediaBaseList, lang, MediaBaseDto::getId));
        return mediaBaseList.stream().map(mediaBaseDto -> localizationMediaMapper
                    .toLocalizateMediaBaseDto(localizationMediaMap
                            .get(lang + mediaBaseDto.getId()), mediaBaseDto)).toList();
    }

    private Map<String, LocalizationMedia> getLocalizationMediaMap(List<String> mediaIds) {
        return localizationMediaRepository.findAllById(mediaIds)
                .stream().collect(Collectors.toMap(LocalizationMedia::getId, Function.identity()));
    }

    private <T> List<String> getMediaIds(
            Collection<T> items, String lang, Function<T, String> idExtractor) {
        return items.stream().map(item -> lang + idExtractor.apply(item)).toList();
    }
}
