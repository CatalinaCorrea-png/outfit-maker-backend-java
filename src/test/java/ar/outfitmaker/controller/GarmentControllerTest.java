package ar.outfitmaker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;
import ar.outfitmaker.dto.GarmentDTO;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(GarmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class GarmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GarmentService garmentService;

    @Test
    void getFilteredGarments_bindsQueryParamsIntoFilters() throws Exception {
        when(garmentService.getGarments(any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/garments/filtered-garments")
                        .param("sortBy", "name")
                        .param("ascending", "false"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(garmentService).getGarments(any(), pageable.capture());

        assertThat(pageable.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getFilteredGarments_serializesThePageResponseTheFrontendExpects() throws Exception {
        GarmentDTO dto = new GarmentDTO("garment-1", "Remera", "Remera rayada", "Zara",
                "#ffffff", "navy", Pattern.STRIPED, "algodón", 2, null, Season.SUMMER,
                "Lavar con agua fría", true, "2026-09-22", null);
        when(garmentService.getGarments(any(), any()))
                .thenReturn(new PageResponse<>(List.of(dto), 0, 6, 1L, 1));

        mockMvc.perform(get("/garments/filtered-garments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("garment-1"))
                .andExpect(jsonPath("$.content[0].name").value("Remera rayada"))
                .andExpect(jsonPath("$.content[0].category").value("Remera"))
                .andExpect(jsonPath("$.content[0].season").value("SUMMER"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].imageUrl").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageSize").value(6));
    }

    @Test
    void notFoundException_isTranslatedIntoTheApiErrorFormat() throws Exception {
        when(garmentService.getGarments(any(), any()))
                .thenThrow(new NotFoundException("GARMENT_NOT_FOUND", "No existe la prenda"));

        mockMvc.perform(get("/garments/filtered-garments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("GARMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("No existe la prenda"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    private static PageResponse<GarmentDTO> emptyPage() {
        return new PageResponse<>(List.of(), 0, 6, 0L, 0);
    }
}
