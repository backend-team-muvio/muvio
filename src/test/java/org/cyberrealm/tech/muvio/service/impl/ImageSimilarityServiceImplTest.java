package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.IMAGE_PATH;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.util.TestConstants.PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImageSimilarityServiceImplTest {
    private static final String FORMAT_NAME = "jpg";
    private static final int STATUS_200 = 200;

    @Mock
    private PerceptiveHash perceptiveHash;
    @Mock
    private Hash mockHash;
    @Mock
    private HttpClient httpClient;
    @Mock
    private HttpResponse<InputStream> httpResponse;
    @InjectMocks
    private ImageSimilarityServiceImpl imageSimilarityService;

    @Test
    @SneakyThrows
    @DisplayName("Verify addIfUniqueHash() method works")
    public void addIfUniqueHash_ValidResponse_addedPath() {
        final String imageUrl = IMAGE_PATH + PATH;
        final Set<Hash> imageHashes = new HashSet<>();
        final Set<String> filePaths = new HashSet<>();
        final BufferedImage image = new BufferedImage(TEN, TEN, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT_NAME, os);
        final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        when(httpClient.send(any(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(STATUS_200);
        when(httpResponse.body()).thenReturn(is);
        when(perceptiveHash.hash(any(BufferedImage.class))).thenReturn(mockHash);
        when(mockHash.getAlgorithmId()).thenReturn(ONE);
        imageSimilarityService.addIfUniqueHash(imageUrl, imageHashes, filePaths);
        assertEquals(ONE, imageHashes.size());
        assertEquals(ONE, filePaths.size());
        assertEquals(Set.of(imageUrl), filePaths);
        verify(perceptiveHash).hash(any(BufferedImage.class));
    }
}
