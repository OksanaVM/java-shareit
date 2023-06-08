package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.OutputItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping()
    public List<OutputItemDto> getItems(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItems(ownerId, from, size);
    }

    @GetMapping("/{id}")
    public OutputItemDto getItem(@RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                 @PathVariable Long id) {
        return itemService.getItem(id, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") long userId, @RequestParam(name = "text") String text,
                                  @RequestParam(defaultValue = "0") Integer from,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItems(userId, text, from, size);
    }

    @PostMapping()
    public ItemDto create(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                          @RequestBody ItemDto item) {
        return itemService.addItem(ownerId, item);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                          @PathVariable Long id,
                          @RequestBody ItemDto item) {
        return itemService.update(ownerId, id, item);
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@RequestHeader(value = HeaderConstants.OWNER_ID) Long authorId,
                                 @PathVariable Long id,
                                 @RequestBody CommentDto commentBody) {
        return itemService.addComment(authorId, id, commentBody);
    }


}
