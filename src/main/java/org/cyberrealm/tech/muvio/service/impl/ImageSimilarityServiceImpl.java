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
            @Value("${image.similarity.threshold:0.4}") double similarityThreshold) {
        this.perceptiveHash = Objects.requireNonNull(perceptiveHash,
                "PerceptiveHash must not be null");
        this.similarityThreshold = similarityThreshold;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    @Retryable(retryFor = NetworkRequestException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public void addIfUniqueHash(String imageUrl, Set<Hash> imageHashes, Set<String> filePaths) {
        BufferedImage image;
        try {
            final URI uri = new URI(imageUrl);
            final HttpRequest request = HttpRequest.newBuilder().uri(uri)
                    .timeout(Duration.ofSeconds(FIVE)).GET().build();
            final HttpResponse<InputStream> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NetworkRequestException(
                        "Interrupted while fetching image: " + imageUrl, e);
            }
            if (response.statusCode() >= STATUS_CODE_400) {
                return;
            }
            image = ImageIO.read(response.body());
            if (image == null) {
                return;
            }
        } catch (IOException | URISyntaxException e) {
            throw new NetworkRequestException(
                    "Failed to read image or generate hash from: " + imageUrl, e);
        }
        try {
            final Hash hash = perceptiveHash.hash(image);
            final int algorithmId = hash.getAlgorithmId();
            final boolean isSimilar = imageHashes.stream()
                    .filter(h -> h.getAlgorithmId() == algorithmId)
                    .anyMatch(existing ->
                            hash.normalizedHammingDistance(existing) < similarityThreshold);
            if (!isSimilar) {
                imageHashes.add(hash);
                filePaths.add(imageUrl);
            }
        } catch (Exception e) {
            throw new NetworkRequestException("Error while hashing image: " + imageUrl, e);
        }
    }
}
