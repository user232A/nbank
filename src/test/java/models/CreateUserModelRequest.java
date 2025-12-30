package models;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserModelRequest extends BaseModel{

    private String username;
    private String password;
    private String role;

}
