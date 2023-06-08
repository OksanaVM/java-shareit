package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@Validated({Create.class}) @RequestBody ItemDto itemDto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("post item userId={}, itemDto={}", userId, itemDto);
        return itemClient.postItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable long itemId) {
        log.info("patch item userId={}, itemId= {}, itemDto={}", userId, itemId, itemDto);
        return itemClient.patchItem(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get itemId={}, userId={}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsersItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                   @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get allUserItem userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAllUsersItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam String text, @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                           @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get /search text={}, from={}, size={}", text, from, size);
        return itemClient.getItems(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Validated({Create.class}) @RequestBody CommentDto commentDto) {
        log.info("Post comment userId={}, itemId={}, commentDto={}", userId, itemId, commentDto);
        return itemClient.addComment(itemId, userId, commentDto);
    }
}
