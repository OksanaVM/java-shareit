package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HeaderConstants;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping()
    public List<ItemsDto> getItems(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                                   @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                   @Positive @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItems(ownerId, from, size);
    }

    @GetMapping("/{id}")
    public ItemsDto getItem(@RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                            @PathVariable Long id) {
        return itemService.getItem(id, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItems(@RequestParam(name = "text") String text,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        return itemService.getItems(text, from, size);
    }

    @PostMapping()
    public ItemDto create(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                          @Validated @RequestBody ItemDto item) {
        return itemService.addItem(ownerId, item);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                          @PathVariable Long id,
                          @Validated @RequestBody ItemDto item) {
        return itemService.update(ownerId, id, item);
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@RequestHeader(value = HeaderConstants.OWNER_ID) Long authorId,
                                 @PathVariable Long id,
                                 @Validated @RequestBody CommentDto commentBody) {
        return itemService.addComment(authorId, id, commentBody);
    }


}
