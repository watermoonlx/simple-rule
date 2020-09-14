package quickstart;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class Order {
    private String buyer;
    private int amount;
    private LocalDateTime createTime;
}
