package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static java.lang.Math.toIntExact;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> tester;

    @Test
    void testBookingDto() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("user");
        userDto.setEmail("user@mail.ru");
        ItemDto item = new ItemDto();
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setId(1L);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setBookerId(42L);
        bookingDto.setItemId(item.getId());
        bookingDto.setStatus(Status.APPROVED);
        bookingDto.setStart(LocalDateTime.of(2023, 4, 21, 15, 0));
        bookingDto.setEnd(LocalDateTime.of(2023, 5, 10, 20, 0));
        JsonContent<BookingDto> content = tester.write(bookingDto);

        assertThat(content).extractingJsonPathNumberValue("$.id")
                .isEqualTo(toIntExact(bookingDto.getId()));
        assertThat(content).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(toIntExact(bookingDto.getItemId()));
        assertThat(content).extractingJsonPathNumberValue("$.bookerId")
                .isEqualTo(toIntExact(bookingDto.getBookerId()));
        assertThat(content).extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingDto.getStatus().toString());
        assertThat(content).extractingJsonPathStringValue("$.start")
                .isEqualTo(bookingDto.getStart().toString() + ":00");
        assertThat(content).extractingJsonPathStringValue("$.end")
                .isEqualTo(bookingDto.getEnd().toString() + ":00");
    }
}

//class BookingDtoTest {
//
//    @Autowired
//    private JacksonTester<BookingDto> json;
//
//    @Test
//    void testBookingDto() throws Exception {
//        UserDto user = new UserDto();
//        user.setName("testNameUser");
//        user.setEmail("test@mail.com");
//        user.setId(1L);
//        ItemDto item = new ItemDto();
//        item.setName("testItem");
//        item.setDescription("5");
//        item.setAvailable(true);
//        item.setId(1L);
//
//        BookingDto bookingDto = new BookingDto();
//        bookingDto.setId(1L);
//        bookingDto.setBookerId(999L);
//        bookingDto.setItemId(item.getId());
//        bookingDto.setStatus(Status.APPROVED);
//        bookingDto.setStart(LocalDateTime.of(2022, 1, 15, 10, 11));
//        bookingDto.setEnd(LocalDateTime.of(2022, 3, 15, 10, 11));
//        JsonContent<BookingDto> result = json.write(bookingDto);
//
//        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(Math.toIntExact(bookingDto.getId()));
//        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(Math.toIntExact(bookingDto.getItemId()));
//        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(Math.toIntExact(bookingDto.getBookerId()));
//        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(bookingDto.getStatus().toString());
//        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(bookingDto.getStart().toString() + ":00");
//        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(bookingDto.getEnd().toString() + ":00");
//    }
//}