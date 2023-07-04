package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.WrongRequestException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = BookingController.class)
class BookingControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingServiceMock;

    @Test
    void shouldCreateBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setId(42L);
        bookingDto1.setStart(start);
        bookingDto1.setEnd(end);
        ItemDto itemDto = new ItemDto(1L, "item");
        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setId(1L);
        bookingDto2.setStart(start);
        bookingDto2.setEnd(end);
        bookingDto2.setItem(itemDto);
        when(bookingServiceMock.create(1L, bookingDto1)).thenReturn(bookingDto2);
        String content = mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals(objectMapper.writeValueAsString(bookingDto2), content);
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto itemDto = new ItemDto(1L, "item");
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItem(itemDto);
        when(bookingServiceMock.update(bookingDto.getId(), 1L, true))
                .thenReturn(bookingDto);
        String content = mockMvc.perform(patch("/bookings/{bookingId}", bookingDto.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals(objectMapper.writeValueAsString(bookingDto), content);
    }

    @Test
    void shouldFindBookingById() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto itemDto = new ItemDto(1L, "item");
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItem(itemDto);
        when(bookingServiceMock.findBookingById(bookingDto.getId(), 1L)).thenReturn(bookingDto);
        mockMvc.perform(get("/bookings/{bookingId}", bookingDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingDto)));
    }

    @Test
    void shouldGetAllBookingsByBooker() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto itemDto = new ItemDto(1L, "item");
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItem(itemDto);
        when(bookingServiceMock.getAllBookingsByBooker(1L, "ALL", 0, 10))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingDto))));
    }

    @Test
    void shouldNotGetAllBookingsByBooker() throws Exception {
        when(bookingServiceMock.getAllBookingsByBooker(1L, "available", 0, 1))
                .thenThrow(WrongRequestException.class);
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "available")
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotGetAllBookingsByBookerIfFromIsNegative() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldNotGetAllBookingsByBookerIfSizeIsBigger() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "1")
                        .param("size", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldGetAllBookingsByOwner() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        ItemDto itemDto = new ItemDto(1L, "item");
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setItem(itemDto);
        when(bookingServiceMock.getAllBookingsByOwner(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingDto))));
    }

    @Test
    void shouldNotGetAllBookingsByOwnerIfUnknownState() throws Exception {
        when(bookingServiceMock.getAllBookingsByOwner(1L, "available", 0, 1))
                .thenThrow(WrongRequestException.class);
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "available")
                        .param("from", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotGetAllBookingsByOwnerIfFromIsNegative() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldNotGetAllBookingsByOwnerIfSizeIsBigger() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "1")
                        .param("size", "42")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldDeleteBookingById() throws Exception {
        mockMvc.perform(delete("/bookings/{id}", 1L))
                .andExpect(status().isOk());
        verify(bookingServiceMock).delete(1L);
    }
}

//@WebMvcTest(value = BookingController.class)
//class BookingControllerTest {
//    @Autowired
//    MockMvc mockMvc;
//    @Autowired
//    ObjectMapper objectMapper;
//    @MockBean
//    private BookingService bookingServiceMock;
//
//    @Test
//    void getBookingById() throws Exception {
//        LocalDateTime start = LocalDateTime.now();
//        LocalDateTime end = start.plusDays(1);
//
//        ItemDto itemDto = new ItemDto(1L, "itemNameTest");
//
//        BookingDto bookingResponseDto = new BookingDto();
//        bookingResponseDto.setId(1L);
//        bookingResponseDto.setStart(start);
//        bookingResponseDto.setEnd(end);
//        bookingResponseDto.setItem(itemDto);
//        when(bookingServiceMock.findBookingById(1L, bookingResponseDto.getId())).thenReturn(bookingResponseDto);
//
//        mockMvc.perform(get("/bookings/{bookingId}", bookingResponseDto.getId())
//                        .header("X-Sharer-User-Id", 1L))
//
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(bookingResponseDto)));
//    }
//
//    @SneakyThrows
//    @Test
//    void create() {
//        LocalDateTime start = LocalDateTime.now();
//        LocalDateTime end = start.plusDays(1);
//
//        BookingDto bookingDto = new BookingDto();
//        bookingDto.setId(123123L);
//        bookingDto.setStart(start);
//        bookingDto.setEnd(end);
//
//        ItemDto itemDto = new ItemDto(1L, "itemNameTest");
//
//        BookingDto bookingResponseDto = new BookingDto();
//        bookingResponseDto.setId(1L);
//        bookingResponseDto.setStart(start);
//        bookingResponseDto.setEnd(end);
//        bookingResponseDto.setItem(itemDto);
//        when(bookingServiceMock.create(1L, bookingDto)).thenReturn(bookingResponseDto);
//
//        String result = mockMvc.perform(post("/bookings")
//                        .header("X-Sharer-User-Id", 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingDto)))
//
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);
//    }
//
//    @SneakyThrows
//    @Test
//    void approvingByOwner() {
//        LocalDateTime start = LocalDateTime.now();
//        LocalDateTime end = start.plusDays(1);
//
//
//        ItemDto itemDto = new ItemDto(1L, "itemNameTest");
//
//        BookingDto bookingResponseDto = new BookingDto();
//        bookingResponseDto.setId(1L);
//        bookingResponseDto.setStart(start);
//        bookingResponseDto.setEnd(end);
//        bookingResponseDto.setItem(itemDto);
//
//        when(bookingServiceMock.update(1L, bookingResponseDto.getId(), true)).thenReturn(bookingResponseDto);
//
//        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingResponseDto.getId())
//                        .param("approved", "true")
//                        .header("X-Sharer-User-Id", 1L)
//                        .contentType(MediaType.APPLICATION_JSON))
//
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        assertEquals(objectMapper.writeValueAsString(bookingResponseDto), result);
//    }
//
//    @SneakyThrows
//    @Test
//    void getAllByBooker_isValid() {
//        LocalDateTime start = LocalDateTime.now();
//        LocalDateTime end = start.plusDays(1);
//
//        ItemDto itemDto = new ItemDto(1L, "itemNameTest");
//
//        BookingDto bookingResponseDto = new BookingDto();
//        bookingResponseDto.setId(1L);
//        bookingResponseDto.setStart(start);
//        bookingResponseDto.setEnd(end);
//        bookingResponseDto.setItem(itemDto);
//        when(bookingServiceMock.getAllBookingsByBooker(1L, "ALL", 0, 10)).thenReturn(List.of(bookingResponseDto));
//
//        mockMvc.perform(get("/bookings")
//                        .header("X-Sharer-User-Id", 1L)
//                        .param("state", "ALL")
//                        .param("from", "0")
//                        .param("size", "10")
//                        .contentType(MediaType.APPLICATION_JSON))
//
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto))));
//    }
//
//    @SneakyThrows
//    @Test
//    void getAllByBooker_StateIsInvalid_ConstraintViolationException() {
//        when(bookingServiceMock.getAllBookingsByBooker(1L, "someString", 0, 1)).thenThrow(WrongRequestException.class);
//
//        mockMvc.perform(get("/bookings")
//                        .header("X-Sharer-User-Id", 1L)
//                        .param("state", "someString")
//                        .param("from", "0")
//                        .param("size", "1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//
////    @Test
////    void getAllByBooker_FromIsInvalid_ConstraintViolationException() {
////        mockMvc.perform(get("/bookings")
////                        .header("X-Sharer-User-Id", 1L)
////                        .param("state", "ALL")
////                        .param("from", "-1")
////                        .param("size", "10")
////                        .contentType(MediaType.APPLICATION_JSON)
////                        .accept(MediaType.APPLICATION_JSON))
////
////                .andExpect(status().isInternalServerError());
////    }
//
////    @Test
////    void getAllByBooker_SizeIsInvalid_ConstraintViolationException() {
////        mockMvc.perform(get("/bookings")
////                        .header("X-Sharer-User-Id", 1L)
////                        .param("state", "ALL")
////                        .param("from", "1")
////                        .param("size", "9999")
////                        .contentType(MediaType.APPLICATION_JSON)
////                        .accept(MediaType.APPLICATION_JSON))
////
////                .andExpect(status().isInternalServerError());
////    }
//
//    @SneakyThrows
//    @Test
//    void getAllByOwner_isCorrect() {
//        LocalDateTime start = LocalDateTime.now();
//        LocalDateTime end = start.plusDays(1);
//
//        ItemDto itemDto = new ItemDto(1L, "itemNameTest");
//
//        BookingDto bookingResponseDto = new BookingDto();
//        bookingResponseDto.setId(1L);
//        bookingResponseDto.setStart(start);
//        bookingResponseDto.setEnd(end);
//        bookingResponseDto.setItem(itemDto);
//        when(bookingServiceMock.getAllBookingsByOwner(any(), any(), anyInt(), anyInt()))
//                .thenReturn(List.of(bookingResponseDto));
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header("X-Sharer-User-Id", 1L)
//                        .param("state", "ALL")
//                        .param("from", "0")
//                        .param("size", "1")
//                        .contentType(MediaType.APPLICATION_JSON))
//
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(List.of(bookingResponseDto))));
//    }
//
//    @SneakyThrows
//    @Test
//    void getAllByOwner_StateIsInvalid_ConstraintViolationException() {
//        when(bookingServiceMock.getAllBookingsByOwner(1L, "someString", 0, 1)).thenThrow(WrongRequestException.class);
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header("X-Sharer-User-Id", 1L)
//                        .param("state", "someString")
//                        .param("from", "0")
//                        .param("size", "1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }
//
////    @Test
////    void getAllByOwner_FromIsInvalid_ConstraintViolationException() {
////        mockMvc.perform(get("/bookings/owner")
////                        .header("X-Sharer-User-Id", 1L)
////                        .param("state", "ALL")
////                        .param("from", "-1")
////                        .param("size", "1")
////                        .contentType(MediaType.APPLICATION_JSON))
////                .andExpect(status().isInternalServerError());
////    }
//
////    @Test
////    void getAllByOwner_SizeIsInvalid_ConstraintViolationException() {
////        mockMvc.perform(get("/bookings/owner")
////                        .header("X-Sharer-User-Id", 1L)
////                        .param("state", "ALL")
////                        .param("from", "1")
////                        .param("size", "9999")
////                        .contentType(MediaType.APPLICATION_JSON))
////                .andExpect(status().isInternalServerError());
////    }
//
//    @SneakyThrows
//    @Test
//    void deleteBookingById() {
//        mockMvc.perform(delete("/bookings/{id}", 1L))
//                .andExpect(status().isOk());
//        verify(bookingServiceMock).delete(1L);
//    }
//}