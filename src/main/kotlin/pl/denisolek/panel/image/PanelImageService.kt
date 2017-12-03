package pl.denisolek.panel.image

import net.coobird.thumbnailator.Thumbnails
import org.apache.tomcat.util.http.fileupload.FileUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.denisolek.Exception.ServiceException
import pl.denisolek.core.image.ImageService
import pl.denisolek.core.image.StorageProperties
import pl.denisolek.core.restaurant.Restaurant
import pl.denisolek.infrastructure.FULL_SIZE
import pl.denisolek.infrastructure.THUMBNAIL
import pl.denisolek.infrastructure.TMP
import pl.denisolek.infrastructure.util.isImageType
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class PanelImageService(private val imageService: ImageService,
                        private val storageProperties: StorageProperties,
                        private val thumbnailLocation: Path = Paths.get(storageProperties.thumbnailLocation),
                        private val fullSizeLocation: Path = Paths.get(storageProperties.fullSizeLocation)) {
    fun uploadImage(restaurant: Restaurant, imageType: String, image: MultipartFile) {
        if (image.isEmpty) throw ServiceException(HttpStatus.BAD_REQUEST, "Image is empty.")
        if (image.isImageType()) throw ServiceException(HttpStatus.BAD_REQUEST, "Wrong image type.")

        //todo remove old avatar
        val uuidName = imageService.generateUUID()
        var thumbnail = File("/$TMP/${THUMBNAIL}_$uuidName.png")
        var fullsize = File("/$TMP/${FULL_SIZE}_$uuidName.png")
        imageService.resize(image, thumbnail, 300, 300)
        Files.copy(thumbnail.inputStream(), thumbnailLocation.resolve("$uuidName.png"))
        Files.copy(fullsize.inputStream(), fullSizeLocation.resolve("$uuidName.png"))

    }


}