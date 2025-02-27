package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ItemController.class)
class ItemControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private ItemService itemServiceMock;

    @Test
    void getItemById_itemIdIsCorrect_returnItem() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        when(itemServiceMock.findItemById(any(), any())).thenReturn(item);

        mockMvc.perform(get("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", item.getId()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath(("$.name")).value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()));
        verify(itemServiceMock).findItemById(any(), any());
    }

    @Test
    void getItemById_itemIdIsIncorrect_returnItem() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        when(itemServiceMock.findItemById(any(), any())).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", item.getId()))
                .andExpect(status().isNotFound());
        verify(itemServiceMock).findItemById(any(), any());
    }

    @Test
    void getAllByOwner_isCorrect() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        when(itemServiceMock.getAllOwnerItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(item));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "1")
                        .param("size", "1"))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(item))));
    }

    @Test
    void create_itemIsValid_returnValid() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setAvailable(true);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        item.setComments(new ArrayList<>());
        Long userId = 1L;
        when(itemServiceMock.create(any(), any())).thenReturn(item);

        String response = mockMvc.perform(post("/items")
                        .content(new ObjectMapper().writeValueAsString(item))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))

                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ItemDto actual = objectMapper.readValue(response, ItemDto.class);
        assertEquals(item, actual);
        verify(itemServiceMock).create(any(), any());

    }

    @Test
    void update() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setAvailable(true);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        item.setComments(new ArrayList<>());
        when(itemServiceMock.update(any(), any(), any())).thenReturn(item);

        mockMvc.perform(patch("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", "1")
                        .content(objectMapper.writeValueAsString(item))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath(("$.name")).value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()));
        verify(itemServiceMock).update(any(), any(), any());
    }

    @Test
    void searchAvailableItem() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setAvailable(true);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        item.setComments(new ArrayList<>());
        when(itemServiceMock.findItemForRental(any())).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "Test"))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(item))));
        verify(itemServiceMock).findItemForRental(any());
    }

    @Test
    void createComment_isValid() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setAvailable(true);
        item.setName("itemNameTest");
        item.setDescription("DescriptionTest");
        item.setComments(new ArrayList<>());

        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setAuthorName("authorNameTest");
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setText("commentText");
        when(itemServiceMock.createComment(any(), any(), any())).thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header("X-Sharer-User-Id", "1")
                        .content(objectMapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(commentDto)));
        verify(itemServiceMock).createComment(any(), any(), any());

    }
}