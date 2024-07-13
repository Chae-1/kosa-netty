package websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage implements Serializable {
    private Long roomId;
    private Long userId;
    private String userName;
    private String message;
    private LocalDateTime sendDateTime;
}
