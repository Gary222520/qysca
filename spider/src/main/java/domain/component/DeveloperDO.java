package domain.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDO {

    private String developerId;

    private String developerName;

    private String developerEmail;
}
