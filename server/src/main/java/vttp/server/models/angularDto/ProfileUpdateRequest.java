package vttp.server.models.angularDto;

public class ProfileUpdateRequest {
    private String phoneNumber;
    private String name;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
