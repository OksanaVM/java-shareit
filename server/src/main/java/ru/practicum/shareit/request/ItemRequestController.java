package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;

@RestController

@RequestMapping(path = "/requests")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                 @RequestBody ItemRequestDto itemRequestDto) {
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
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", required = false,
                                                       defaultValue = "10") int size) {
        return requestService.getAllRequest(userId, from, size);
    }
}
