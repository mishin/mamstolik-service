package pl.denisolek.panel.image

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import pl.denisolek.Exception.ServiceException
import pl.denisolek.core.image.Image
import pl.denisolek.core.image.ImageService
import pl.denisolek.core.image.StorageProperties
import pl.denisolek.core.restaurant.Restaurant
import pl.denisolek.core.restaurant.RestaurantService
import pl.denisolek.infrastructure.FULL_SIZE
import pl.denisolek.infrastructure.THUMBNAIL
import pl.denisolek.infrastructure.TMP
import pl.denisolek.infrastructure.util.isImageType
import pl.denisolek.panel.image.DTO.ImageDTO
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class PanelImageService(private val imageService: ImageService,
                        storageProperties: StorageProperties,
                        private val restaurantService: RestaurantService) {

    private val thumbnailLocation: Path = Paths.get(storageProperties.thumbnailLocation)
    private val fullSizeLocation: Path = Paths.get(storageProperties.fullSizeLocation)

    fun uploadImage(restaurant: Restaurant, imageType: String, image: MultipartFile): ImageDTO {
        validateImage(image)
        val uuidName = imageService.generateUUID()
        saveFullSizeThumbnail(uuidName, image)
        val newImage = saveImage(image, uuidName, restaurant)
        assignMainImage(imageType, restaurant, newImage)
        return ImageDTO.fromImage(newImage)
    }

    private fun assignMainImage(imageType: String, restaurant: Restaurant, newImage: Image) {
        when (imageType) {
            "main" -> {
                restaurant.mainImage = newImage
                restaurant.images.add(newImage)
                restaurantService.save(restaurant)
            }
        }
    }

    private fun saveImage(image: MultipartFile, uuidName: String, restaurant: Restaurant): Image {
        return imageService.save(Image(
                fileName = image.originalFilename,
                uuid = uuidName,
                restaurant = restaurant
        ))
    }

    private fun saveFullSizeThumbnail(uuidName: String, image: MultipartFile) {
        val thumbnail = File("/$TMP/${THUMBNAIL}_$uuidName.png")
        val fullSize = File("/$TMP/${FULL_SIZE}_$uuidName.png")
        imageService.resize(image, thumbnail, 300, 300)
        imageService.toFile(image, fullSize)
        Files.copy(thumbnail.inputStream(), thumbnailLocation.resolve("$uuidName.png"))
        Files.copy(fullSize.inputStream(), fullSizeLocation.resolve("$uuidName.png"))
    }

    private fun validateImage(image: MultipartFile) {
        if (image.isEmpty) throw ServiceException(HttpStatus.BAD_REQUEST, "Image is empty.")
        if (!image.isImageType()) throw ServiceException(HttpStatus.BAD_REQUEST, "Wrong image type.")
    }
}