package vttp.server.models.angularDto;

import java.time.LocalDate;

public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String name;
    private LocalDate birthDate;
    private String phoneNumber;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    @Override
    public String toString() {
        return "RegisterRequest [username=" + username + ", email=" + email + ", password=" + password + ", name="
                + name + ", birthDate=" + birthDate + ", phoneNumber=" + phoneNumber + "]";
    }

    
    
}
