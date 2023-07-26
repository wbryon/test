package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = RequestController.class)
class RequestControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private RequestService requestServiceMock;

    @Test
    void shouldFindById_requestIsCorrect_returnRequest() throws Exception {
        RequestDto expected = new RequestDto();
        expected.setId(1L);
        expected.setDescription("description");
        expected.setCreated(LocalDateTime.now());

        when(requestServiceMock.findByUserIdAndRequestId(any(), any())).thenReturn(expected);

        String response = mockMvc.perform(get("/requests/{requestId}", expected.getId())
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        RequestDto actual = objectMapper.readValue(response, RequestDto.class);
        assertEquals(expected, actual);
        verify(requestServiceMock).findByUserIdAndRequestId(any(), any());
    }

    @Test
    void shouldFindById_requestIsInCorrect_returnNotFoundException() throws Exception {
        Long requestId = 1L;
        Long userId = 1L;
        when(requestServiceMock.findByUserIdAndRequestId(userId, requestId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        verify(requestServiceMock).findByUserIdAndRequestId(userId, requestId);
    }

    @Test
    void shouldGetAllByRequestor_userIsCorrect_returnRequestList() throws Exception {
        RequestDto expected = new RequestDto();
        expected.setId(1L);
        expected.setDescription("description");
        expected.setCreated(LocalDateTime.now());
        when(requestServiceMock.getAllRequestsByRequestor(anyLong())).thenReturn(List.of(expected));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(expected))));
        verify(requestServiceMock).getAllRequestsByRequestor(1L);
    }

    @Test
    void shouldGetAll() throws Exception {
        RequestDto expected = new RequestDto();
        expected.setId(1L);
        expected.setDescription("description");
        expected.setCreated(LocalDateTime.now());
        when(requestServiceMock.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(expected));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(expected))));
        verify(requestServiceMock).getAllRequests(1L, 0, 10);
    }

    @Test
    void shouldCreateRequestIsValid_returnRequest() throws Exception {
        RequestDto expected = new RequestDto();
        expected.setId(1L);
        expected.setDescription("description");
        expected.setCreated(LocalDateTime.now());

        when(requestServiceMock.create(any(), any())).thenReturn(expected);

        String result = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expected)))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)))
                .andReturn().getResponse().getContentAsString();
        assertEquals(result, objectMapper.writeValueAsString(expected));
        verify(requestServiceMock).create(any(), any());
    }

}