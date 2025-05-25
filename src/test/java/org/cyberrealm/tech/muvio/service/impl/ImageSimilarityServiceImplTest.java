package org.cyberrealm.tech.muvio.service.impl;

import static org.cyberrealm.tech.muvio.common.Constants.IMAGE_PATH;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.util.TestConstants.PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ImageSimilarityServiceImplTest {

    @Mock
    private PerceptiveHash perceptiveHash;
    @Mock
    private Hash mockHash;
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
        final MockedStatic<ImageIO> mockedStatic = mockStatic(ImageIO.class);
        mockedStatic.when(() -> ImageIO.read(new URI(imageUrl).toURL()))
                .thenReturn(image);
        when(perceptiveHash.hash(image)).thenReturn(mockHash);
        imageSimilarityService.addIfUniqueHash(imageUrl, imageHashes, filePaths);
        assertEquals(ONE, imageHashes.size());
        assertEquals(ONE, filePaths.size());
    }
}
