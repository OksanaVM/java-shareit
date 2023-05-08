package ru.practicum.shareit.user.utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.util.HeaderConstants;

@ExtendWith(MockitoExtension.class)
public class VariableUnitTest {
    @Test
    public void shouldNotModifyVariables() {
        Assertions.assertNotNull(HeaderConstants.OWNER_ID);
        Assertions.assertEquals(HeaderConstants.OWNER_ID, "X-Sharer-User-Id");
    }
}
