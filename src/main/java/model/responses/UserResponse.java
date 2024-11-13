package model.responses;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String username;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}