import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupPojo {
    private String block_reason;
    private String groupId;
    private Role role;
    private String status;
    private Long unblock_date_ms;
    private String userId;
}
