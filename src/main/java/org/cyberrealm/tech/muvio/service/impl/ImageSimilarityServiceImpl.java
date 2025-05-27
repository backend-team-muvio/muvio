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
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.service.ImageSimilarityService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageSimilarityServiceImpl implements ImageSimilarityService {
    private static final double SIMILARITY_THRESHOLD = 0.49;
    private static final int STATUS_CODE_400 = 400;
    private final PerceptiveHash perceptiveHash;
    private final HttpClient httpClient;

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
                            hash.normalizedHammingDistance(existing) < SIMILARITY_THRESHOLD);
            if (!isSimilar) {
                imageHashes.add(hash);
                filePaths.add(imageUrl);
            }
        } catch (Exception e) {
            throw new NetworkRequestException("Error while hashing image: " + imageUrl, e);
        }
    }
}
