package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ItemDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDto nextBooking;
    private BookingDto lastBooking;
    private List<CommentDto> comments;
    private Long requestId;

    public ItemDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setNextBooking(Booking booking) {
        this.nextBooking = BookingMapper.toBookingDtoShort(booking);
    }

    public void setLastBooking(Booking booking) {
        this.lastBooking = BookingMapper.toBookingDtoShort(booking);
    }

    public void setComments(List<Comment> comments) {
        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = CommentMapper.toCommentDto(comment);
            commentDtoList.add(commentDto);
        }
        this.comments = commentDtoList;
    }
}
