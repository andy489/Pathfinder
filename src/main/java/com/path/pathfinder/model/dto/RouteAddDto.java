package com.path.pathfinder.model.dto;

import com.path.pathfinder.model.enumerated.LevelEnum;
import com.path.pathfinder.model.enumerated.RouteCategoryEnum;
import com.path.pathfinder.model.validation.route.CustomFile;
import com.path.pathfinder.model.validation.route.UniqueUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RouteAddDto {

    @NotBlank
    @Size(min = 4, max = 50, message = "{route.name.size}")
    private String name;

    @NotBlank
    @Size(min = 4, message = "{route.description.size}")
    private String description;

    @CustomFile(contentTypes = {"text/xml"})
    private MultipartFile gpxCoordinates;

    @NotNull
    private LevelEnum level;

    //    @EmptyOrExactSizeString(message = "{route.video-url.exact-size}")
    @URL
    @Pattern(regexp = "http(?:s?):\\/\\/(?:www\\.)?youtu(?:be\\.com\\/watch\\?v=|\\.be\\/)([\\w\\-\\_]*)(&(amp;)?‌​[\\w\\?‌​=]*)?",
            message = "Invalid youtube URL provided")
    @UniqueUrl(message = "{route.video-url.unique}")
    private String videoUrl;

    @Size(min = 1, message = "{route.categories.size}")
    private Set<RouteCategoryEnum> categories;

}
