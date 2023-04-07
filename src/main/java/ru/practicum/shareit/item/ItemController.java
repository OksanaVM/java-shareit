package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utilits.Variables;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping()
    public List<ItemDto> getItems(@RequestHeader(value = Variables.USER_ID) Long ownerId) {
        return itemService.getItems(ownerId);
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable Long id) {
        return itemService.getItem(id);
    }

    @GetMapping("/search")
    public List<ItemDto> getItems(@RequestParam(name = "text") String text) {
        return itemService.getItems(text);
    }

    @PostMapping()
    public ItemDto create(@RequestHeader(value = Variables.USER_ID) Long ownerId,
                          @Valid @RequestBody @NotNull ItemDto item) {
        return itemService.addItem(ownerId, item);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(value = Variables.USER_ID) Long ownerId,
                          @PathVariable Long id,
                          @Valid @RequestBody @NotNull ItemDto item) {
        return itemService.update(ownerId, id, item);
    }

}
