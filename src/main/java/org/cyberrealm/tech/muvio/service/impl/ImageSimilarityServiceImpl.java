package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.BACK_OFF;
import static org.cyberrealm.tech.muvio.common.Constants.MAX_ATTEMPTS;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import javax.imageio.ImageIO;
import org.cyberrealm.tech.muvio.exception.NetworkRequestException;
import org.cyberrealm.tech.muvio.service.ImageSimilarityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class ImageSimilarityServiceImpl implements ImageSimilarityService {
    private final PerceptiveHash perceptiveHash;
    private final double similarityThreshold;

    public ImageSimilarityServiceImpl(PerceptiveHash perceptiveHash,
                                      @Value("${image.similarity.threshold:0.4}")
                                      double similarityThreshold) {
        this.perceptiveHash = perceptiveHash;
        this.similarityThreshold = similarityThreshold;
    }

    @Retryable(retryFor = NetworkRequestException.class, maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACK_OFF))
    @Override
    public void addIfUniqueHash(String imageUrl, Set<Hash> imageHashes, Set<String> filePaths) {
        BufferedImage image;
        try {
            image = ImageIO.read(new URI(imageUrl).toURL());
            if (image == null) {
                return;
            }
        } catch (IOException | URISyntaxException e) {
            throw new NetworkRequestException(
                    "Failed to read image or generate hash from: " + imageUrl, e);
        }
        final Hash hash = perceptiveHash.hash(image);
        final boolean isSimilar = imageHashes.stream().anyMatch(
                existing -> hash.normalizedHammingDistance(existing) < similarityThreshold);
        if (!isSimilar) {
            imageHashes.add(hash);
            filePaths.add(imageUrl);
        }
    }
}
