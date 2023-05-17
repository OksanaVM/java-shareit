package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.HeaderConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                 @Valid @RequestBody @NotNull ItemRequestDto itemRequestDto) {
        return requestService.create(userId, itemRequestDto);
    }

    @GetMapping("{id}")
    public ItemRequestDto getRequestById(@RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                         @PathVariable Long id) {
        return requestService.getById(userId, id);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserRequest(
            @RequestHeader(value = HeaderConstants.OWNER_ID) Long userId) {
        return requestService.getAllUserRequest(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                               @PositiveOrZero
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @Positive @RequestParam(name = "size", required = false,
                                                       defaultValue = "10") int size) {
        return requestService.getAllRequest(userId, from, size);
    }
}
