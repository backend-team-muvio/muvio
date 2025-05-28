package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.BACK_OFF;
import static org.cyberrealm.tech.muvio.common.Constants.FIVE;
import static org.cyberrealm.tech.muvio.common.Constants.MAX_ATTEMPTS;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.service.ImageSimilarityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImageSimilarityServiceImpl implements ImageSimilarityService {
    private static final int STATUS_CODE_400 = 400;
    private static final int TIMEOUT_SECONDS = FIVE;

    private final double similarityThreshold;
    private final PerceptiveHash perceptiveHash;
    private final HttpClient httpClient;

    public ImageSimilarityServiceImpl(
            PerceptiveHash perceptiveHash,
            HttpClient httpClient,
            @Value("${image.similarity.threshold:0.4}") double similarityThreshold) {
        this.perceptiveHash = Objects.requireNonNull(perceptiveHash,
                "PerceptiveHash must not be null");
        this.httpClient = Objects.requireNonNull(httpClient,
                "HttpClient must not be null");
        this.similarityThreshold = similarityThreshold;
    }

    @Retryable(retryFor = NetworkRequestException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public void addIfUniqueHash(String imageUrl, Set<Hash> imageHashes, Set<String> filePaths) {
        log.debug("Processing image from URL: {}", imageUrl);

        BufferedImage image = fetchImage(imageUrl);
        if (image == null) {
            log.warn("Could not process image from URL: {}", imageUrl);
            return;
        }

        processImageHash(image, imageUrl, imageHashes, filePaths);
    }

    private BufferedImage fetchImage(String imageUrl) {
        try {
            HttpResponse<InputStream> response = sendHttpRequest(imageUrl);

            if (response.statusCode() >= STATUS_CODE_400) {
                log.warn("Received error status code {} for URL: {}",
                        response.statusCode(), imageUrl);
                return null;
            }

            BufferedImage image = ImageIO.read(response.body());
            if (image == null) {
                log.warn("Could not read image data from URL: {}", imageUrl);
            }
            return image;

        } catch (IOException | URISyntaxException e) {
            throw new NetworkRequestException(
                    "Failed to read image from: " + imageUrl, e);
        }
    }

    private HttpResponse<InputStream> sendHttpRequest(String imageUrl)
            throws URISyntaxException, IOException {
        final URI uri = new URI(imageUrl);
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(FIVE))
                .GET()
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkRequestException("Interrupted while fetching image: " + imageUrl, e);
        }
    }

    private void processImageHash(BufferedImage image, String imageUrl,
                                  Set<Hash> imageHashes, Set<String> filePaths) {
        try {
            Hash hash = perceptiveHash.hash(image);
            if (isHashUnique(hash, imageHashes)) {
                log.debug("Adding unique hash for image: {}", imageUrl);
                imageHashes.add(hash);
                filePaths.add(imageUrl);
            } else {
                log.debug("Skipping similar image: {}", imageUrl);
            }
        } catch (Exception e) {
            throw new NetworkRequestException("Error while hashing image: " + imageUrl, e);
        }
    }

    private boolean isHashUnique(Hash hash, Set<Hash> imageHashes) {
        final int algorithmId = hash.getAlgorithmId();
        return imageHashes.stream()
                .filter(h -> h.getAlgorithmId() == algorithmId)
                .noneMatch(existing ->
                        hash.normalizedHammingDistance(existing) < similarityThreshold);
    }
}
