package alg.coyote001.dto;

import jakarta.validation.constraints.NotNull;

public class TelegramAuthDto {
    @NotNull
    private Long id;
    
    private String first_name;
    private String last_name;
    private String username;
    private String photo_url;
    
    @NotNull
    private Long auth_date;
    
    @NotNull
    private String hash;

    // Constructors
    public TelegramAuthDto() {}

    public TelegramAuthDto(Long id, String first_name, String last_name, String username, 
                          String photo_url, Long auth_date, String hash) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.username = username;
        this.photo_url = photo_url;
        this.auth_date = auth_date;
        this.hash = hash;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }
    
    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPhoto_url() { return photo_url; }
    public void setPhoto_url(String photo_url) { this.photo_url = photo_url; }
    
    public Long getAuth_date() { return auth_date; }
    public void setAuth_date(Long auth_date) { this.auth_date = auth_date; }
    
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
} 