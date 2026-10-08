package ar.outfitmaker.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.outfitmaker.domain.Slot;
import ar.outfitmaker.dto.CategoryDTO;
import ar.outfitmaker.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    private static final Authentication CHER =
            new UsernamePasswordAuthenticationToken("cher@gmail.com", null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getCategories_returnsNameAndSlot() throws Exception {
        when(categoryService.getCategories()).thenReturn(List.of(
                new CategoryDTO("Campera", Slot.OUTERWEAR),
                new CategoryDTO("Remera", Slot.UPPER)));

        mockMvc.perform(get("/categories").principal(CHER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Campera"))
                .andExpect(jsonPath("$[0].slot").value("OUTERWEAR"))
                .andExpect(jsonPath("$[1].name").value("Remera"));
    }
}
