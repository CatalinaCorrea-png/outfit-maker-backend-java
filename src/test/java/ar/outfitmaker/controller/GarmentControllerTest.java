package ar.outfitmaker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentPhotoFiles;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.errors.NotFoundException;
import ar.outfitmaker.service.GarmentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

@WebMvcTest(GarmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class GarmentControllerTest {

    // Con addFilters = false nadie arma el Authentication: el parámetro del controller
    // sale de request.getUserPrincipal(), que solo se llena pasándolo con .principal()
    private static final Authentication CHER =
            new UsernamePasswordAuthenticationToken("cher@gmail.com", null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GarmentService garmentService;

    @Test
    void getFilteredGarments_bindsQueryParamsIntoFilters() throws Exception {
        when(garmentService.getGarments(any(), any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/garments/filtered-garments")
                        .principal(CHER)
                        .param("sortBy", "name")
                        .param("ascending", "false"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> email = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(garmentService).getGarments(any(), email.capture(), pageable.capture());

        // El usuario sale del token, nunca de un parámetro que el cliente pueda tocar
        assertThat(email.getValue()).isEqualTo("cher@gmail.com");
        assertThat(pageable.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getFilteredGarments_serializesThePageResponseTheFrontendExpects() throws Exception {
        GarmentDTO dto = new GarmentDTO("garment-1", "Remera", "Remera rayada", "Zara",
                "#ffffff", "navy", Pattern.STRIPED, "algodón", 2, null, Season.SUMMER,
                "Lavar con agua fría", true, "2026-09-22", "front-thumb.jpg", null);
        when(garmentService.getGarments(any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(dto), 0, 6, 1L, 1));

        mockMvc.perform(get("/garments/filtered-garments").principal(CHER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("garment-1"))
                .andExpect(jsonPath("$.content[0].name").value("Remera rayada"))
                .andExpect(jsonPath("$.content[0].category").value("Remera"))
                .andExpect(jsonPath("$.content[0].season").value("SUMMER"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].frontImageUrl").value("front-thumb.jpg"))
                .andExpect(jsonPath("$.content[0].backImageUrl").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageSize").value(6));
    }

    @Test
    void notFoundException_isTranslatedIntoTheApiErrorFormat() throws Exception {
        when(garmentService.getGarments(any(), any(), any()))
                .thenThrow(new NotFoundException("GARMENT_NOT_FOUND", "No existe la prenda"));

        mockMvc.perform(get("/garments/filtered-garments").principal(CHER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("No existe la prenda"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    // ─── alta atómica (multipart: JSON + archivos) ───────────────────────────

    private static final String VALID_GARMENT = """
            {"category": "Remera", "name": "Remera rayada", "brand": "Zara",
             "primaryColor": "#ffffff", "secondaryColor": "", "pattern": "STRIPED",
             "material": "algodón", "formality": 2, "fit": "REGULAR", "season": "SUMMER",
             "careNotes": null}
            """;

    @Test
    void createGarment_receivesTheDataAndThePhotosInOneRequest() throws Exception {
        when(garmentService.createGarment(any(), any(), any())).thenReturn(new GarmentDTO("garment-1", "Remera",
                "Remera rayada", "Zara", "#ffffff", "", Pattern.STRIPED, "algodón", 2, null, Season.SUMMER,
                null, true, "2026-10-08", "front-thumb.webp", null));

        mockMvc.perform(multipart("/garments/create")
                        .file(garmentPart(VALID_GARMENT))
                        .file(image("front"))
                        .file(image("frontThumb"))
                        .principal(CHER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("garment-1"))
                .andExpect(jsonPath("$.frontImageUrl").value("front-thumb.webp"));

        ArgumentCaptor<GarmentPhotoFiles> photos = ArgumentCaptor.forClass(GarmentPhotoFiles.class);
        verify(garmentService).createGarment(any(), photos.capture(), eq("cher@gmail.com"));
        assertThat(photos.getValue().front()).isNotNull();
        assertThat(photos.getValue().frontThumb()).isNotNull();
        assertThat(photos.getValue().back()).isNull();
    }

    @Test
    void createGarment_withoutTheFrontPhoto_isPhotoRequired() throws Exception {
        mockMvc.perform(multipart("/garments/create")
                        .file(garmentPart(VALID_GARMENT))
                        .principal(CHER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PHOTO_REQUIRED"));

        verify(garmentService, never()).createGarment(any(), any(), any());
    }

    @Test
    void createGarment_withInvalidData_isAValidationErrorAndStoresNothing() throws Exception {
        String withoutName = VALID_GARMENT.replace("\"name\": \"Remera rayada\"", "\"name\": \"\"");

        mockMvc.perform(multipart("/garments/create")
                        .file(garmentPart(withoutName))
                        .file(image("front"))
                        .principal(CHER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // El @Valid corta antes del service: ninguna foto llegó a escribirse
        verify(garmentService, never()).createGarment(any(), any(), any());
    }

    // El JSON de la prenda viaja como una parte más, con su Content-Type
    private static MockMultipartFile garmentPart(String json) {
        return new MockMultipartFile("garment", "", "application/json", json.getBytes(StandardCharsets.UTF_8));
    }

    private static MockMultipartFile image(String part) {
        return new MockMultipartFile(part, part + ".webp", "image/webp", new byte[]{1, 2, 3});
    }

    private static PageResponse<GarmentDTO> emptyPage() {
        return new PageResponse<>(List.of(), 0, 6, 0L, 0);
    }
}
